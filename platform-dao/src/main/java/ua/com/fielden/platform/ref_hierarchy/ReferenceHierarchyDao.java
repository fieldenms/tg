package ua.com.fielden.platform.ref_hierarchy;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies.DomainEntityDependency;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypeDetails;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypesSummary;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.PREDICATE_ACTIVATABLE_AND_PERSISTENT_ENTITY_TYPE;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.entityDependencyMap;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyActions.EDIT;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyActions.REFERENCE_HIERARCHY;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/// DAO implementation for companion object [IReferenceHierarchy].
///
/// @author TG Team
///
@EntityType(ReferenceHierarchy.class)
public class ReferenceHierarchyDao extends CommonEntityDao<ReferenceHierarchy> implements IReferenceHierarchy {

    public static final String ERR_ENTITY_TYPE_NOT_FOUND = "Entity type [%s] could not be found.";
    public static final String ERR_REFERENCE_ENTITY_SHOULD_EXIST = "Please select at least one entity to open reference hierarchy.";
    public static final String ERR_NO_ASSOCIATED_GENERATOR_FUNCTION = "There is no associated generator function for level: %s";
    public static final String ERR_COULD_NOT_FIND_ENTITY_TYPE = "Could not find type %s.";

    private static final Set<Class<? extends AbstractEntity<?>>> EXCLUDED_TYPES = ImmutableSet.of(User.class);

    private final IEntityAggregatesOperations coAggregates;
    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    ReferenceHierarchyDao(final IEntityAggregatesOperations coAggregates, final IApplicationDomainProvider applicationDomainProvider) {
        this.coAggregates = coAggregates;
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    @SessionRequired
    public ReferenceHierarchy save(final ReferenceHierarchy action) {
        action.isValid().ifFailure(Result::throwRuntime);
        if (action.getRefEntityId() == null) {
            throw failure(ERR_REFERENCE_ENTITY_SHOULD_EXIST);
        } else {
            final var referencedEntityType = action.getRefEntityClass().orElseThrow(() -> failure(ERR_COULD_NOT_FIND_ENTITY_TYPE.formatted(action.getRefEntityType())));
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependencies = new HashMap<>();
            if (!action.isActiveOnly() || !isActivatableEntityType(referencedEntityType)) {
                dependencies.putAll(DataDependencyQueriesGenerator.produceDependenciesMetadata(applicationDomainProvider.entityTypes()));
            }
            // In case of active only we need to build the dependency graph in the same way as in ActivePropertyValidator.
            // This is needed to keep a direct correspondence between the result produced by this validator and the reference hierarchy for users to easily navigate through the dependencies.
            // One implication of this approach is that the reference hierarchy becomes flatter due to removal of nodes pertaining to deactivatable dependencies.
            else {
                final var domainDependencies = entityDependencyMap(applicationDomainProvider.entityTypes(), PREDICATE_ACTIVATABLE_AND_PERSISTENT_ENTITY_TYPE);
                final var domainEntityDependencies = domainDependencies.get(referencedEntityType);
                // The result of getAllDependenciesThatCanPreventDeactivation needs to be inverted to make it suitable for the reference hierarchy.
                final var invertedDependencies = domainEntityDependencies.getAllDependenciesThatCanPreventDeactivation(domainDependencies)
                        .collect(groupingBy(DomainEntityDependency::entityType,
                                            mapping(DomainEntityDependency::propPath, toSet())));
                // Populate dependencies based on the inverted dependencies.
                // For example, for Person, we get:
                // WorkActivity -> {Person -> [referredTo, manager.person, returnedToServiceBy.person, technician.person, authoriser]}, which includes the relevant property paths.
                invertedDependencies.forEach((referencingEntityType, properties) -> dependencies.put(referencingEntityType, Map.of(referencedEntityType, properties)));
            }
            final var nextLevel = action.getLoadedHierarchyLevel().nextLevel();
            switch (nextLevel) {
                case REFERENCE_GROUP -> action.setGeneratedHierarchy(generateReferenceGroup(action, dependencies));
                case REFERENCE_BY_INSTANCE -> action.setGeneratedHierarchy(generateReferenceByInstanceLevelHierarchy(action, dependencies));
                default -> throw failuref(ERR_NO_ASSOCIATED_GENERATOR_FUNCTION, nextLevel);
            }
        }
        return action.setResetFilter(false);
    }

    private List<ReferenceHierarchyEntry> generateReferenceGroup(
            final ReferenceHierarchy action,
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependencies)
    {
        return List.of(createReferenceGroup(generateReferences(action)), createReferencedByGroup(generateTypeLevelHierarchy(action, dependencies)));
    }

    private ReferenceHierarchyEntry createReferencedByGroup(final List<TypeLevelHierarchyEntry> types) {
        final Integer totalCount = types.stream().reduce(0, (prev, curr) -> prev + curr.getNumberOfEntities(), Integer::sum);
        final ReferenceHierarchyEntry referencedByEntry = new ReferenceHierarchyEntry();
        referencedByEntry.setKey("Referenced By");
        referencedByEntry.setChildren(types);
        referencedByEntry.setHierarchyLevel(ReferenceHierarchyLevel.REFERENCED_BY);
        referencedByEntry.setDesc(totalCount.toString());
        return referencedByEntry;
    }

    private ReferenceHierarchyEntry createReferenceGroup(final List<ReferenceLevelHierarchyEntry> references) {
        final ReferenceHierarchyEntry referenceEntry = new ReferenceHierarchyEntry();
        referenceEntry.setKey("References");
        referenceEntry.setDesc(String.valueOf(references.size()));
        referenceEntry.setChildren(references);
        referenceEntry.setHierarchyLevel(REFERENCES);
        return referenceEntry;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<ReferenceLevelHierarchyEntry> generateReferences(final ReferenceHierarchy action) {
        final Class<? extends AbstractEntity<?>> entityType = action.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, action.getRefEntityType()));
        final List<Field> entityFields = getReferenceProperties(entityType);
        final fetch fetchModel = generateReferenceFetchModel(entityType, entityFields);
        final Optional<? extends AbstractEntity<?>> optionalEntity = co(entityType).findByIdOptional(action.getRefEntityId(), fetchModel);
        return optionalEntity.map(refEntity -> generateReferencesFor(refEntity, getExistentReferenceProperties(refEntity, entityFields))).orElse(emptyList());
    }

    private List<Field> getExistentReferenceProperties(final AbstractEntity<?> refEntity, final List<Field> entityFields) {
        return entityFields.stream().filter(entityField -> refEntity.get(entityField.getName()) != null).toList();
    }

    @SuppressWarnings("unchecked")
    private fetch<? extends AbstractEntity<?>> generateReferenceFetchModel(final Class<? extends AbstractEntity<?>> entityType, final List<Field> entityFields) {
        fetch<? extends AbstractEntity<?>> fetch = fetchKeyAndDescOnly(entityType);
        fetch = hasDescProperty(entityType) ? fetch.with("desc") : fetch;
        for (final Field propField: entityFields) {
            final Class<? extends AbstractEntity<?>> propertyType = (Class<? extends AbstractEntity<?>>) propField.getType();
            final List<Field> innerProps = new ArrayList<>();
            if (AbstractUnionEntity.class.isAssignableFrom(propertyType)) {
                innerProps.addAll(unionProperties((Class<? extends AbstractUnionEntity>) propertyType));
            }
            final fetch<? extends AbstractEntity<?>> innerFetchModel = generateReferenceFetchModel(propertyType, innerProps);
            fetch = fetch.with(propField.getName(), innerFetchModel);
        }
        return fetch;
    }

    private List<Field> getReferenceProperties(final Class<? extends AbstractEntity<?>> entityType) {
        return Finder.findPropertiesThatAreEntities(entityType).stream()
                .filter(propField -> propField.isAnnotationPresent(MapTo.class) && !PropertyDescriptor.class.isAssignableFrom(propField.getType()) && !EXCLUDED_TYPES.contains(propField.getType()))
                .toList();
    }

    private List<ReferenceLevelHierarchyEntry> generateReferencesFor(final AbstractEntity<?> entity, final List<Field> entityFields) {
        return entityFields.stream().map(propField -> generateReferenceFor(entity, propField)).toList();
    }

    private ReferenceLevelHierarchyEntry generateReferenceFor(final AbstractEntity<?> entity, final Field propField) {
        final String propTitle = getTitleAndDesc(propField.getName(), entity.getType()).getKey();
        final AbstractEntity<?> value = entity.get(propField.getName());
        final ReferenceLevelHierarchyEntry entry = new ReferenceLevelHierarchyEntry();
        entry.set(ID, value.getId());
        entry.setKey(propTitle + ":" + value.toString());
        if (EntityUtils.hasDescProperty(value.getType())) {
            entry.setDesc(value.getDesc());
        }
        entry.setPropertyTitle(propTitle);
        entry.setEntity(AbstractUnionEntity.class.isAssignableFrom(value.getType()) ? ((AbstractUnionEntity)value).activeEntity() : value);
        entry.setHierarchyLevel(ReferenceHierarchyLevel.REFERENCE_INSTANCE);
        entry.setHasChildren(false);
        entry.setHierarchyActions(EDIT, REFERENCE_HIERARCHY);
        return entry;
    }

    private List<ReferencedByLevelHierarchyEntry> generateReferenceByInstanceLevelHierarchy(
            final ReferenceHierarchy action,
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependencies)
    {
        final var entityClass = action.getEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, action.getEntityType()));
        final var refEntityClass = action.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, action.getRefEntityType()));
        final var propFields = getReferenceProperties(entityClass);
        final var fetchModel = generateReferenceFetchModel(entityClass, propFields);
        final var qem = queryForDependentTypeDetails(dependencies, action.getRefEntityId(), refEntityClass, entityClass, fetchModel, action.isActiveOnly());
        final IPage<EntityAggregates> loadedPage;
        if (action.getPageNumber() == 0) {
            loadedPage = coAggregates.firstPage(qem, action.getPageSize());
        } else {
            loadedPage = coAggregates.getPage(qem, action.getPageNumber(), action.getPageSize());
        }
        action.setPageCount(loadedPage.numberOfPages());
        action.setPageNumber(loadedPage.no());

        return createInstanceHierarchy(loadedPage.data(), propFields);
    }

    private List<ReferencedByLevelHierarchyEntry> createInstanceHierarchy(final List<EntityAggregates> instanceAggregates, final List<Field> propFields) {
        return instanceAggregates.stream().map(instanceAggregate -> createInstanceHierarchyEntry(instanceAggregate, propFields)).toList();
    }

    private ReferencedByLevelHierarchyEntry createInstanceHierarchyEntry(final EntityAggregates instanceAggregate, final List<Field> propFields) {
        final ReferencedByLevelHierarchyEntry instanceEntry = new ReferencedByLevelHierarchyEntry();
        instanceEntry.set(ID, ((AbstractEntity<?>)instanceAggregate.get("entity")).getId());
        instanceEntry.setKey(instanceAggregate.get("entity").toString());
        instanceEntry.setDesc(instanceAggregate.get("entity.desc"));
        final AbstractEntity<?> entity = instanceAggregate.get("entity");
        instanceEntry.setEntity(AbstractUnionEntity.class.isAssignableFrom(entity.getType()) ? ((AbstractUnionEntity)entity).activeEntity() : entity);
        instanceEntry.setHierarchyLevel(REFERENCE_BY_INSTANCE);
        instanceEntry.setHasChildren("Y".equals(instanceAggregate.get("hasDependencies")) || !getExistentReferenceProperties(instanceEntry.getEntity(), propFields).isEmpty());
        instanceEntry.setHierarchyActions(EDIT, REFERENCE_HIERARCHY);
        return instanceEntry;
    }

    private List<TypeLevelHierarchyEntry> generateTypeLevelHierarchy(
            final ReferenceHierarchy action,
            final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependencies)
    {
        final var entityType = action.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, action.getRefEntityType()));
        final var model = queryForDependentTypesSummary(dependencies, action.getRefEntityId(), entityType, action.isActiveOnly());
        final var result = model.map(coAggregates::getAllEntities).orElse(emptyList());
        return createTypeHierarchyEntries(action, result);

    }

    private List<TypeLevelHierarchyEntry> createTypeHierarchyEntries(final ReferenceHierarchy action, final List<EntityAggregates> typeAggregates) {
        return typeAggregates.stream().map(typeAggregate -> createTypeHierarchyEntry(action, typeAggregate)).toList();
    }

    @SuppressWarnings("unchecked")
    private TypeLevelHierarchyEntry createTypeHierarchyEntry(final ReferenceHierarchy action, final EntityAggregates typeAggregate) {
        try {
            final TypeLevelHierarchyEntry typeEntry = new TypeLevelHierarchyEntry();
            final String entityType = typeAggregate.get("type");
            final Class<? extends AbstractEntity<?>> entityTypeClass = (Class<? extends AbstractEntity<?>>) Class.forName(entityType);
            final Pair<String, String> titleAndDesc = getEntityTitleAndDesc(entityTypeClass);
            typeEntry.setKey(titleAndDesc.getKey());
            typeEntry.setDesc(titleAndDesc.getValue());
            typeEntry.setEntityType(entityType);
            typeEntry.setRefEntityId(action.getRefEntityId());
            typeEntry.setRefEntityType(action.getRefEntityType());
            typeEntry.setNumberOfEntities(Optional.ofNullable(typeAggregate.get("qty")).map(v -> Integer.valueOf(v.toString())).orElse(0));
            typeEntry.setHierarchyLevel(TYPE);
            typeEntry.setHasChildren(true);
            return typeEntry;
        } catch (final ClassNotFoundException e) {
            throw failuref(ERR_ENTITY_TYPE_NOT_FOUND, typeAggregate.get("type"));
        }
    }
}
