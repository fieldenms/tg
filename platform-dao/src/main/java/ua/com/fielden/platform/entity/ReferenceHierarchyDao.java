package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.ReferenceHierarchyActions.EDIT;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.master.IMasterInfoProvider;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
/**
 * DAO implementation for companion object {@link IReferenceHierarchy}.
 *
 * @author Developers
 *
 */
@EntityType(ReferenceHierarchy.class)
public class ReferenceHierarchyDao extends CommonEntityDao<ReferenceHierarchy> implements IReferenceHierarchy {

    private final IEntityAggregatesOperations coAggregates;
    private final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata;
    private final IMasterInfoProvider masterInfoProvider;

    @Inject
    public ReferenceHierarchyDao(final IFilter filter, final IEntityAggregatesOperations coAggregates, final IApplicationDomainProvider applicationDomainProvider, final IMasterInfoProvider masterInfoProvider) {
        super(filter);
        this.coAggregates = coAggregates;
        this.dependenciesMetadata = DataDependencyQueriesGenerator.produceDependenciesMetadata(applicationDomainProvider.entityTypes());
        this.masterInfoProvider = masterInfoProvider;
    }

    @Override
    @SessionRequired
    public ReferenceHierarchy save(final ReferenceHierarchy entity) {
        if (entity.getRefEntityId() == null) {
            throw Result.failuref("Please select at least one entity to open reference hierarchy.");
        } else if (entity.getEntityType() == null) {
            entity.setGeneratedHierarchy(generateTypeLevelHierarchy(entity));
        } else {
            entity.setGeneratedHierarchy(generateInstanceLevelHierarchy(entity));
        }
        entity.setResetFilter(false);
        return entity;
    }

    private List<InstanceLevelHierarchyEntry> generateInstanceLevelHierarchy(final ReferenceHierarchy entity) {
        try {
            final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> qem = DataDependencyQueriesGenerator.queryForDependentTypeDetails(dependenciesMetadata, entity.getRefEntityId(), entity.getRefEntityClass(), entity.getEntityClass());
            final IPage<EntityAggregates> loadedPage;
            if (entity.getPageNumber() == 0) {
                loadedPage = coAggregates.firstPage(qem, entity.getPageSize());
            } else {
                loadedPage = coAggregates.getPage(qem, entity.getPageNumber(), entity.getPageSize());
            }
            entity.setPageCount(loadedPage.numberOfPages());
            entity.setPageNumber(loadedPage.no());
            return createInstanceHierarchy(loadedPage.data());
        } catch (final ClassNotFoundException e) {
            throw Result.failuref("The entity types: %s or %s can not be found", entity.getRefEntityType(), entity.getEntityType());
        }
    }

    private List<InstanceLevelHierarchyEntry> createInstanceHierarchy(final List<EntityAggregates> instanceAggregates) {
        return instanceAggregates.stream().map(instanceAggregate -> createInstanceHierarchyEntry(instanceAggregate)).collect(toList());
    }

    private InstanceLevelHierarchyEntry createInstanceHierarchyEntry(final EntityAggregates instanceAggregate) {
        final InstanceLevelHierarchyEntry instanceEntry = new InstanceLevelHierarchyEntry();
        final Class<? extends AbstractEntity<?>> entityType = ((AbstractEntity<?>)instanceAggregate.get("entity")).getType();
        instanceEntry.setId(((AbstractEntity<?>)instanceAggregate.get("entity")).getId());
        instanceEntry.setKey(instanceAggregate.get("entity").toString());
        instanceEntry.setDesc(instanceAggregate.get("entity.desc"));
        instanceEntry.setEntity(instanceAggregate.get("entity"));
        instanceEntry.setHierarchyLevel(ReferenceHierarchyLevel.INSTANCE);
        instanceEntry.setHasChildren("Y".equals(instanceAggregate.get("hasDependencies")));
        instanceEntry.setMasterInfo(masterInfoProvider.getMasterInfo(entityType));
        final List<ReferenceHierarchyActions> actions = new ArrayList<>();
        if (instanceEntry.getMasterInfo() != null) {
            actions.add(EDIT);
        }
        instanceEntry.setHierarchyActions(actions.toArray(new ReferenceHierarchyActions[0]));
        return instanceEntry;
    }

    private List<TypeLevelHierarchyEntry> generateTypeLevelHierarchy(final ReferenceHierarchy entity) {
        try {
            final List<EntityAggregates> result = coAggregates.getAllEntities(DataDependencyQueriesGenerator.queryForDependentTypesSummary(dependenciesMetadata, entity.getRefEntityId(), entity.getRefEntityClass()));
            if (result.isEmpty()) {
                final Pair<String, String> titleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(entity.getRefEntityClass());
                throw Result.failuref("The %s entity with %s id is not referenced yet", titleAndDesc.getKey(), entity.getRefEntityId());
            }
            return createTypeHierarchyEntries(entity, result);
        } catch (final ClassNotFoundException e) {
            throw Result.failuref("The entity type: %s can not be found", entity.getRefEntityType());
        }
    }

    private List<TypeLevelHierarchyEntry> createTypeHierarchyEntries(final ReferenceHierarchy entity, final List<EntityAggregates> typeAggregates) {
        return typeAggregates.stream().map(typeAggregate -> createTypeHierarchyEntry(entity, typeAggregate)).collect(toList());
    }

    @SuppressWarnings("unchecked")
    private TypeLevelHierarchyEntry createTypeHierarchyEntry(final ReferenceHierarchy entity, final EntityAggregates typeAggregate) {
        try {
            final TypeLevelHierarchyEntry typeEntry = new TypeLevelHierarchyEntry();
            final String entityType = typeAggregate.get("type");
            final Class<? extends AbstractEntity<?>> entityTypeClass = (Class<? extends AbstractEntity<?>>) Class.forName(entityType);
            final Pair<String, String> titleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(entityTypeClass);
            typeEntry.setKey(titleAndDesc.getKey());
            typeEntry.setDesc(titleAndDesc.getValue());
            typeEntry.setEntityType(entityType);
            typeEntry.setRefEntityId(entity.getRefEntityId());
            typeEntry.setRefEntityType(entity.getRefEntityType());
            typeEntry.setNumberOfEntities(typeAggregate.get("qty"));
            typeEntry.setHierarchyLevel(TYPE);
            typeEntry.setHasChildren(true);
            return typeEntry;
        } catch (final ClassNotFoundException e) {
            throw Result.failuref("The entity type: %s can not be found", typeAggregate.get("type"));
        }
    }
}