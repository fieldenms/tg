package ua.com.fielden.platform.ref_hierarchy;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypeDetails;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypesSummary;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyActions.EDIT;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyActions.REFERENCE_HIERARCHY;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCES;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCE_BY_INSTANCE;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCE_GROUP;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.TYPE;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
/**
 * DAO implementation for companion object {@link IReferenceHierarchy}.
 *
 * @author TG Team
 *
 */
@EntityType(ReferenceHierarchy.class)
public class ReferenceHierarchyDao extends CommonEntityDao<ReferenceHierarchy> implements IReferenceHierarchy {
    public static final String ERR_ENTITY_TYPE_NOT_FOUND = "Entity type [%s] could not be found.";

    private final IEntityAggregatesOperations coAggregates;
    private final Map<Class<? extends AbstractEntity<?>>, Map<Class<? extends AbstractEntity<?>>, Set<String>>> dependenciesMetadata;
    private final Map<ReferenceHierarchyLevel, Function<ReferenceHierarchy, List<? extends AbstractEntity<?>>>> generateFunctions;
    private final Set<Class<? extends AbstractEntity<?>>> systemTypesToExclude;

    @Inject
    public ReferenceHierarchyDao(final IEntityAggregatesOperations coAggregates, final IApplicationDomainProvider applicationDomainProvider) {
        this.coAggregates = coAggregates;
        this.dependenciesMetadata = DataDependencyQueriesGenerator.produceDependenciesMetadata(applicationDomainProvider.entityTypes());
        this.systemTypesToExclude = new HashSet<>();
        this.systemTypesToExclude.add(User.class);
        this.generateFunctions = new HashMap<>();
        this.generateFunctions.put(REFERENCE_GROUP, this::generateReferenceGroup);
        this.generateFunctions.put(REFERENCE_BY_INSTANCE, this::generateReferenceByInstanceLevelHierarchy);
    }

    @Override
    @SessionRequired
    public ReferenceHierarchy save(final ReferenceHierarchy entity) {
        entity.isValid().ifFailure(Result::throwRuntime);
        if (entity.getRefEntityId() == null) {
            throw failuref("Please select at least one entity to open reference hierarchy.");
        } else {
            final ReferenceHierarchyLevel nextLevel = entity.getLoadedHierarchyLevel().nextLevel();
            if (this.generateFunctions.containsKey(nextLevel)) {
                entity.setGeneratedHierarchy(this.generateFunctions.get(nextLevel).apply(entity));
            } else {
                throw failuref("There is no associated generator function for level: %s", nextLevel);
            }
        }
        entity.setResetFilter(false);
        return entity;
    }

    private List<ReferenceHierarchyEntry> generateReferenceGroup(final ReferenceHierarchy entity) {
        return listOf(createReferenceGroup(generateReferences(entity)), createReferencedByGroup(generateTypeLevelHierarchy(entity)));
    }

    private ReferenceHierarchyEntry createReferencedByGroup(final List<TypeLevelHierarchyEntry> types) {
        final Integer totalCount = types.stream().reduce(0, (prev, curr) -> prev + curr.getNumberOfEntities(), (prev, curr) -> prev + curr);
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
    private List<ReferenceLevelHierarchyEntry> generateReferences(final ReferenceHierarchy entity) {
        final Class<? extends AbstractEntity<?>> entityType = entity.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getRefEntityType()));
        final List<Field> entityFields = getReferenceProperties(entityType);
        final fetch fetchModel = generateReferenceFetchModel(entityType, entityFields);
        final Optional<? extends AbstractEntity<?>> optionalEntity = co(entityType).findByIdOptional(entity.getRefEntityId(), fetchModel);
        return optionalEntity.map(refEntity -> generateReferencesFor(refEntity, getExistentReferenceProperties(refEntity, entityFields))).orElse(emptyList());
    }

    private List<Field> getExistentReferenceProperties(final AbstractEntity<?> refEntity, final List<Field> entityFields) {
        return entityFields.stream().filter(entityField -> refEntity.get(entityField.getName()) != null).collect(toList());
    }

    @SuppressWarnings("unchecked")
    private fetch<? extends AbstractEntity<?>> generateReferenceFetchModel(final Class<? extends AbstractEntity<?>> entityType, final List<Field> entityFields) {
        fetch<? extends AbstractEntity<?>> fetch = fetchKeyAndDescOnly(entityType);
        fetch = hasDescProperty(entityType) ? fetch.with("desc") : fetch;
        for (final Field propField: entityFields) {
            final Class<? extends AbstractEntity<?>> propertyType = (Class<? extends AbstractEntity<?>>)propField.getType();
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
                .filter(propField -> propField.isAnnotationPresent(MapTo.class) && !PropertyDescriptor.class.isAssignableFrom(propField.getType()) && !systemTypesToExclude.contains(propField.getType()))
                .collect(toList());
    }

    private List<ReferenceLevelHierarchyEntry> generateReferencesFor(final AbstractEntity<?> entity, final List<Field> entityFields) {
        return entityFields.stream().map(propField -> generateReferenceFor(entity, propField)).collect(toList());
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

    private List<ReferencedByLevelHierarchyEntry> generateReferenceByInstanceLevelHierarchy(final ReferenceHierarchy entity) {
        final Class<? extends AbstractEntity<?>> entityClass = entity.getEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getEntityType()));
        final Class<? extends AbstractEntity<?>> refEntityClass = entity.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getRefEntityType()));
        final List<Field> propFields = getReferenceProperties(entityClass);
        final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> qem = queryForDependentTypeDetails(dependenciesMetadata, entity.getRefEntityId(), refEntityClass, entityClass, generateReferenceFetchModel(entityClass, propFields));
        final IPage<EntityAggregates> loadedPage;
        if (entity.getPageNumber() == 0) {
            loadedPage = coAggregates.firstPage(qem, entity.getPageSize());
        } else {
            loadedPage = coAggregates.getPage(qem, entity.getPageNumber(), entity.getPageSize());
        }
        entity.setPageCount(loadedPage.numberOfPages());
        entity.setPageNumber(loadedPage.no());

        return createInstanceHierarchy(loadedPage.data(), propFields);
    }

    private List<ReferencedByLevelHierarchyEntry> createInstanceHierarchy(final List<EntityAggregates> instanceAggregates, final List<Field> propFields) {
        return instanceAggregates.stream().map(instanceAggregate -> createInstanceHierarchyEntry(instanceAggregate, propFields)).collect(toList());
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

    private List<TypeLevelHierarchyEntry> generateTypeLevelHierarchy(final ReferenceHierarchy entity) {
        final Class<? extends AbstractEntity<?>> entityType = entity.getRefEntityClass().orElseThrow(() -> failuref(ERR_ENTITY_TYPE_NOT_FOUND, entity.getRefEntityType()));
        final Optional<QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>> model = queryForDependentTypesSummary(dependenciesMetadata, entity.getRefEntityId(), entityType);
        final List<EntityAggregates> result = model.map(qem -> coAggregates.getAllEntities(qem)).orElse(emptyList());
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
            final Pair<String, String> titleAndDesc = getEntityTitleAndDesc(entityTypeClass);
            typeEntry.setKey(titleAndDesc.getKey());
            typeEntry.setDesc(titleAndDesc.getValue());
            typeEntry.setEntityType(entityType);
            typeEntry.setRefEntityId(entity.getRefEntityId());
            typeEntry.setRefEntityType(entity.getRefEntityType());
            typeEntry.setNumberOfEntities(Optional.ofNullable(typeAggregate.get("qty")).map(v -> Integer.valueOf(v.toString())).orElse(0));
            typeEntry.setHierarchyLevel(TYPE);
            typeEntry.setHasChildren(true);
            return typeEntry;
        } catch (final ClassNotFoundException e) {
            throw failuref(ERR_ENTITY_TYPE_NOT_FOUND, typeAggregate.get("type"));
        }
    }
}
