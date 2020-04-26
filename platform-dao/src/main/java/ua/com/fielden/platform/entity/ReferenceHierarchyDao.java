package ua.com.fielden.platform.entity;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.ReferenceHierarchyActions.EDIT;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.TYPE;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypeDetails;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypesSummary;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import ua.com.fielden.platform.master.IMasterInfoProvider;
import ua.com.fielden.platform.master.MasterInfo;
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
    public static final String ERR_ENTITY_TYPE_NOT_FOUND = "Entity type [%s] could not be found.";

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
            throw failuref("Please select at least one entity to open reference hierarchy.");
        } else if (entity.getEntityType() == null) {
            entity.setGeneratedHierarchy(generateTypeLevelHierarchy(entity));
        } else {
            entity.setGeneratedHierarchy(generateInstanceLevelHierarchy(entity));
        }
        entity.setResetFilter(false);
        return entity;
    }

    private List<InstanceLevelHierarchyEntry> generateInstanceLevelHierarchy(final ReferenceHierarchy entity) {
        final Class<? extends AbstractEntity<?>> entityClass = entity.getEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getEntityType()));
        final Class<? extends AbstractEntity<?>> refEntityClass = entity.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getRefEntityType()));
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> qem = queryForDependentTypeDetails(dependenciesMetadata, entity.getRefEntityId(), refEntityClass, entityClass);
        final IPage<EntityAggregates> loadedPage;
        if (entity.getPageNumber() == 0) {
            loadedPage = coAggregates.firstPage(qem, entity.getPageSize());
        } else {
            loadedPage = coAggregates.getPage(qem, entity.getPageNumber(), entity.getPageSize());
        }
        entity.setPageCount(loadedPage.numberOfPages());
        entity.setPageNumber(loadedPage.no());
        return createInstanceHierarchy(loadedPage.data(), masterInfoProvider.getMasterInfo(entityClass));
    }

    private List<InstanceLevelHierarchyEntry> createInstanceHierarchy(final List<EntityAggregates> instanceAggregates, final MasterInfo masterInfo) {
        return instanceAggregates.stream().map(instanceAggregate -> createInstanceHierarchyEntry(instanceAggregate, masterInfo)).collect(toList());
    }

    private InstanceLevelHierarchyEntry createInstanceHierarchyEntry(final EntityAggregates instanceAggregate, final MasterInfo masterInfo) {
        final InstanceLevelHierarchyEntry instanceEntry = new InstanceLevelHierarchyEntry();
        final Class<? extends AbstractEntity<?>> entityType = ((AbstractEntity<?>)instanceAggregate.get("entity")).getType();
        instanceEntry.setId(((AbstractEntity<?>)instanceAggregate.get("entity")).getId());
        instanceEntry.setKey(instanceAggregate.get("entity").toString());
        instanceEntry.setDesc(instanceAggregate.get("entity.desc"));
        instanceEntry.setEntity(instanceAggregate.get("entity"));
        instanceEntry.setHierarchyLevel(ReferenceHierarchyLevel.INSTANCE);
        instanceEntry.setHasChildren("Y".equals(instanceAggregate.get("hasDependencies")));
        instanceEntry.setMasterInfo(masterInfo);
        final List<ReferenceHierarchyActions> actions = new ArrayList<>();
        if (instanceEntry.getMasterInfo() != null) {
            actions.add(EDIT);
        }
        instanceEntry.setHierarchyActions(actions.toArray(new ReferenceHierarchyActions[0]));
        return instanceEntry;
    }

    private List<TypeLevelHierarchyEntry> generateTypeLevelHierarchy(final ReferenceHierarchy entity) {
        final Class<? extends AbstractEntity<?>> entityType = entity.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getRefEntityType()));
        final Optional<QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>> model = queryForDependentTypesSummary(dependenciesMetadata, entity.getRefEntityId(), entityType);
        final List<EntityAggregates> result = model.map(qem -> coAggregates.getAllEntities(qem)).orElse(emptyList());
        if (result.isEmpty()) {
            throw failure("This entity has no references.");
        }
        return createTypeHierarchyEntries(entity, result);

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
            throw failuref(ERR_ENTITY_TYPE_NOT_FOUND, typeAggregate.get("type"));
        }
    }
}