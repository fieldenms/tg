package ua.com.fielden.platform.entity;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.ReferenceHierarchyActions.EDIT;
import static ua.com.fielden.platform.entity.ReferenceHierarchyActions.REFERENCE_HIERARCHY;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.REFERENCES;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.REFERENCE_BY_INSTANCE;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.REFERENCE_GROUP;
import static ua.com.fielden.platform.entity.ReferenceHierarchyLevel.TYPE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypeDetails;
import static ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator.queryForDependentTypesSummary;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DataDependencyQueriesGenerator;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.EntityUtils;
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
    private final Map<ReferenceHierarchyLevel, Function<ReferenceHierarchy, List<? extends AbstractEntity<?>>>> generateFunctions;

    @Inject
    public ReferenceHierarchyDao(final IFilter filter, final IEntityAggregatesOperations coAggregates, final IApplicationDomainProvider applicationDomainProvider) {
        super(filter);
        this.coAggregates = coAggregates;
        this.dependenciesMetadata = DataDependencyQueriesGenerator.produceDependenciesMetadata(applicationDomainProvider.entityTypes());
        this.generateFunctions = new HashMap<>();
        this.generateFunctions.put(REFERENCE_GROUP, this::generateReferenceGroup);
        this.generateFunctions.put(REFERENCE_BY_INSTANCE, this::generateReferenceByInstanceLevelHierarchy);
    }

    @Override
    @SessionRequired
    public ReferenceHierarchy save(final ReferenceHierarchy entity) {
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
        final List<ReferenceLevelHierarchyEntry> references = generateReferences(entity);
        final List<TypeLevelHierarchyEntry> types = generateTypeLevelHierarchy(entity);
        if (references.isEmpty() && types.isEmpty()) {
            throw failure("This entity has no references.");
        }
        final List<ReferenceHierarchyEntry> res = new ArrayList<>();
        if (!references.isEmpty()) {
            res.add(createReferenceGroup(references));
        }
        if (!types.isEmpty()) {
            res.add(createReferencedByGroup(types));
        }
        return res;
    }

    private ReferenceHierarchyEntry createReferencedByGroup(final List<TypeLevelHierarchyEntry> types) {
        final Integer totalCount = types.stream().reduce(Integer.valueOf(0), (prev, curr) -> Integer.valueOf(prev.intValue() + curr.getNumberOfEntities().intValue()), (prev, curr) -> Integer.valueOf(prev.intValue() + curr.intValue()));
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
        return optionalEntity.map(refEntity -> generateReferencesFor(refEntity, getExistentReferenceProperties(refEntity, entityFields))).orElse(new ArrayList<>());
    }

    private List<Field> getExistentReferenceProperties(final AbstractEntity<?> refEntity, final List<Field> entityFields) {
        return entityFields.stream().filter(entityField -> refEntity.get(entityField.getName()) != null).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private fetch<? extends AbstractEntity<?>> generateReferenceFetchModel(final Class<? extends AbstractEntity<?>> entityType, final List<Field> entityFields) {
        fetch<? extends AbstractEntity<?>> fetch = fetchKeyAndDescOnly(entityType);
        for (final Field propField: entityFields) {
            final Class<? extends AbstractEntity<?>> propertyType = (Class<? extends AbstractEntity<?>>)propField.getType();
            final fetch<? extends AbstractEntity<?>> innerFetchModel = fetchKeyAndDescOnly(propertyType);
            fetch = fetch.with(propField.getName(), hasDescProperty(propertyType) ? innerFetchModel.with("desc") : innerFetchModel);
        }
        return fetch;
    }

    private List<Field> getReferenceProperties(final Class<? extends AbstractEntity<?>> entityType) {
        return Finder.findPropertiesThatAreEntities(entityType).stream()
                .filter(propField -> propField.isAnnotationPresent(MapTo.class) && !PropertyDescriptor.class.isAssignableFrom(propField.getType()))
                .collect(Collectors.toList());
    }

    private List<ReferenceLevelHierarchyEntry> generateReferencesFor(final AbstractEntity<?> entity, final List<Field> entityFields) {
        return entityFields.stream().map(propField -> generateReferenceFor(entity, propField)).collect(toList());
    }

    private ReferenceLevelHierarchyEntry generateReferenceFor(final AbstractEntity<?> entity, final Field propField) {
        final String propTitle = TitlesDescsGetter.getTitleAndDesc(propField.getName(), entity.getType()).getKey();
        final AbstractEntity<?> value = entity.get(propField.getName());
        final ReferenceLevelHierarchyEntry entry = new ReferenceLevelHierarchyEntry();
        entry.setId(value.getId());
        entry.setKey(propTitle + ":" + value.toString());
        if (EntityUtils.hasDescProperty(value.getType())) {
            entry.setDesc(value.getDesc());
        }
        entry.setPropertyTitle(propTitle);
        entry.setEntity(value);
        entry.setHierarchyLevel(ReferenceHierarchyLevel.REFERENCE_INSTANCE);
        entry.setHasChildren(false);
        final List<ReferenceHierarchyActions> actions = new ArrayList<>();
        actions.add(EDIT);
        actions.add(REFERENCE_HIERARCHY);
        entry.setHierarchyActions(actions.toArray(new ReferenceHierarchyActions[0]));
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
        instanceEntry.setId(((AbstractEntity<?>)instanceAggregate.get("entity")).getId());
        instanceEntry.setKey(instanceAggregate.get("entity").toString());
        instanceEntry.setDesc(instanceAggregate.get("entity.desc"));
        instanceEntry.setEntity(instanceAggregate.get("entity"));
        instanceEntry.setHierarchyLevel(REFERENCE_BY_INSTANCE);
        instanceEntry.setHasChildren("Y".equals(instanceAggregate.get("hasDependencies")) || !getExistentReferenceProperties(instanceEntry.getEntity(), propFields).isEmpty());
        final List<ReferenceHierarchyActions> actions = new ArrayList<>();
        actions.add(EDIT);
        actions.add(REFERENCE_HIERARCHY);
        instanceEntry.setHierarchyActions(actions.toArray(new ReferenceHierarchyActions[0]));
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