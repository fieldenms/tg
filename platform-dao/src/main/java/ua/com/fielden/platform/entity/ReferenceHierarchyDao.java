package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.TYPE;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator;
import ua.com.fielden.platform.error.Result;
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

    @Inject
    public ReferenceHierarchyDao(final IFilter filter, final IEntityAggregatesOperations coAggregates, final IApplicationDomainProvider applicationDomainProvider) {
        super(filter);
        this.coAggregates = coAggregates;
        this.dependenciesMetadata = DataDependencyQueriesGenerator.produceDependenciesMetadata(applicationDomainProvider.entityTypes());
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
        return super.save(entity);
    }

    private List<InstanceLevelHierarchyEntry> generateInstanceLevelHierarchy(final ReferenceHierarchy entity) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<TypeLevelHierarchyEntry> generateTypeLevelHierarchy(final ReferenceHierarchy entity) {
        try {
            final List<EntityAggregates> result = coAggregates.getAllEntities(DataDependencyQueriesGenerator.queryForDependentTypesSummary(dependenciesMetadata, entity.getRefEntityId(), entity.getRefEntityClass()));
            if (result.isEmpty()) {
                final Pair<String, String> titleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(entity.getRefEntityClass());
                throw Result.failuref("The %s entity with %s id is not referenced yet", titleAndDesc.getKey(), entity.getRefEntityId());
            }
            return createTypeHierarchyEntries(result);
        } catch (final ClassNotFoundException e) {
            throw Result.failuref("The entity type: %s can not be found", entity.getRefEntityType());
        }
    }

    private List<TypeLevelHierarchyEntry> createTypeHierarchyEntries(final List<EntityAggregates> typeAggregates) {
        return typeAggregates.stream().map(typeAggregate -> createTypeHierarchyEntry(typeAggregate)).collect(toList());
    }

    @SuppressWarnings("unchecked")
    private TypeLevelHierarchyEntry createTypeHierarchyEntry(final EntityAggregates typeAggregate) {
        try {
            final TypeLevelHierarchyEntry typeEntry = new TypeLevelHierarchyEntry();
            final Pair<String, String> titleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc((Class<? extends AbstractEntity<?>>) Class.forName(typeAggregate.get("type")));
            typeEntry.setKey(titleAndDesc.getKey());
            typeEntry.setDesc(titleAndDesc.getValue());
            typeEntry.setNumberOfEntities(typeAggregate.get("qty"));
            typeEntry.setHierarchyLevel(TYPE);
            typeEntry.setHasChildren(true);
            return typeEntry;
        } catch (final ClassNotFoundException e) {
            throw Result.failuref("The entity type: %s can not be found", typeAggregate.get("type"));
        }
    }
}