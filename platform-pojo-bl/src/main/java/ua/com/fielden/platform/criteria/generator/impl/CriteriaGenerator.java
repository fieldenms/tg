package ua.com.fielden.platform.criteria.generator.impl;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.getCriteriaTitleAndDesc;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.applySnapshot;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.entity.annotation.factory.DateAnnotations.newDateOnlyAnnotation;
import static ua.com.fielden.platform.entity.annotation.factory.DateAnnotations.newTimeOnlyAnnotation;
import static ua.com.fielden.platform.entity.annotation.factory.DateAnnotations.newUtcAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.entity.annotation.factory.AfterChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CriteriaPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.EntityTypeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.FirstParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.SecondParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.SkipEntityExistsValidationAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.ui.menu.MiType;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.SaveAsNameAnnotation;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * The implementation of the {@link ICriteriaGenerator} that generates {@link EntityQueryCriteria} with criteria properties.
 *
 * @author TG Team
 *
 */
public class CriteriaGenerator implements ICriteriaGenerator {
    private static final String ERR_CRIT_TYPE_GEN_CENTRE_MANAGER_MISSING = "Criteria type for [%s; %s; %s] config identifier could not be generated -- centreManager must not be null.";
    private static final String ERR_CRIT_TYPE_GEN_WITH_EMPTY_KEY_PART = "Criteria type for [%s; %s; %s] config identifier could not be generated -- all parts must not be null.";
    private static final String ERR_CRIT_TYPE_GEN = "Criteria type for [%s; %s; %s] config identifier could not be generated.";
    private static final Logger LOGGER = getLogger(CriteriaGenerator.class);

    private final EntityFactory entityFactory;

    private final ICompanionObjectFinder coFinder;

    private final Cache<T3<User, Class<? extends MiWithConfigurationSupport<?>>, Optional<String>>, Class<?>> generatedClasses;

    @Inject
    public CriteriaGenerator(final EntityFactory entityFactory, final ICompanionObjectFinder controllerProvider) {
        this.entityFactory = entityFactory;
        this.coFinder = controllerProvider;
        this.generatedClasses = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).maximumSize(2000).concurrencyLevel(50).build();
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(
        final Supplier<T2<Class<? extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>, Class<T>>> queryCriteriaClassAndRootSupplier,
        final ICentreDomainTreeManagerAndEnhancer cdtme
    ) {
        try {
            final var queryCriteriaClassAndRoot = queryCriteriaClassAndRootSupplier.get();

            final DefaultEntityProducerWithContext<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> criteriaEntityProducer = new DefaultEntityProducerWithContext<>(entityFactory, (Class<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>) queryCriteriaClassAndRoot._1, coFinder);
            final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteriaEntity = criteriaEntityProducer.newEntity();
            criteriaEntity.beginInitialising();
            criteriaEntity.setKey("not required");
            criteriaEntity.endInitialising();

            //Set dao for generated entity query criteria.
            final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
            final boolean isDaoAccessable = daoField.isAccessible(); // canAccess(criteriaEntity) is not applicable here as we are returning back whole field accessibility, not field accessibility for concrete instance
            daoField.setAccessible(true);
            daoField.set(criteriaEntity, coFinder.find(queryCriteriaClassAndRoot._2));
            daoField.setAccessible(isDaoAccessable);

            //Set domain tree manager for entity query criteria.
            final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "cdtme");
            final boolean isCdtmeAccessable = dtmField.isAccessible(); // canAccess(criteriaEntity) is not applicable here as we are returning back whole field accessibility, not field accessibility for concrete instance
            dtmField.setAccessible(true);
            dtmField.set(criteriaEntity, cdtme);
            dtmField.setAccessible(isCdtmeAccessable);

            //Add change support to the entity query criteria instance
            //in order to synchronise entity query criteria values with model values
            synchroniseWithModel(criteriaEntity);

            return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>) criteriaEntity;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(
        final User user,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final Optional<String> saveAsName,
        final ICentreDomainTreeManagerAndEnhancer centreManager
    ) {
        return generateCentreQueryCriteria(() -> {
            if (user == null || miType == null || saveAsName == null) {
                throw new CriteriaGeneratorException(ERR_CRIT_TYPE_GEN_WITH_EMPTY_KEY_PART.formatted(user, miType, saveAsName));
            }
            if (centreManager == null) {
                throw new CriteriaGeneratorException(ERR_CRIT_TYPE_GEN_CENTRE_MANAGER_MISSING.formatted(user, miType, saveAsName));
            }

            final Class<T> root = (Class<T>) getEntityType(miType);
            final T3<User, Class<? extends MiWithConfigurationSupport<?>>, Optional<String>> configKey = t3(user, miType, saveAsName);
            final Class<? extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> queryCriteriaClass;
            try {
                queryCriteriaClass = (Class<? extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>>) generatedClasses.get(configKey, () -> {
                    final MiType miTypeAnnotation = new MiTypeAnnotation().newInstance(miType);
                    final Annotation [] customAnnotations = saveAsName.map(name -> new Annotation[] {miTypeAnnotation, new SaveAsNameAnnotation().newInstance(name)}).orElseGet(() -> new Annotation[] {miTypeAnnotation});
                    return generateCriteriaType(root, centreManager.getFirstTick().checkedProperties(root), centreManager.getEnhancer().getManagedType(root), customAnnotations);
                });
            } catch (final ExecutionException ex) {
                throw new CriteriaGeneratorException(ERR_CRIT_TYPE_GEN.formatted(user, miType, saveAsName), ex);
            }
            
            return t2(queryCriteriaClass, root);
        }, centreManager);
    }

    /**
     * Generates criteria entity type for the specified <code>properties</code> subset and <code>managedType</code>.
     * 
     * @param root -- root type from which <code>managedType</code> has been derived (calculated props added)
     * @param properties
     * @param managedType
     * @param customAnnotations -- custom annotations for generated criteria type
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    static <CDTME extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>> Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>> generateCriteriaType(
        final Class<T> root,
        final List<String> properties,
        final Class<?> managedType,
        final Annotation... customAnnotations)
    {
        final List<NewProperty> newProperties = new ArrayList<>();
        for (final String propertyName : properties) {
            if (!isPlaceholder(propertyName)) {
                newProperties.addAll(generateCriteriaProperties(root, managedType, propertyName));
            }
        }
        try {
            return (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>)
                    DynamicEntityClassLoader.startModification(CentreEntityQueryCriteriaToEnhance.class)
                    .addClassAnnotations(customAnnotations)
                    .addProperties(newProperties.toArray(new NewProperty[0]))
                    .endModification();
        } catch (final ClassNotFoundException ex) {
            throw new CriteriaGeneratorException(format("Criteria type for [%s] could not be generated.", root.getSimpleName()), ex);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation clears generated selection criteria classes that become out-dated when adding / removing selection criteria properties to Web UI centre configurations.
     */
    @Override
    public void clear() {
        generatedClasses.cleanUp();
    }

    /**
     * Generates criteria properties for the specified <code>propertyName</code> and <code>managedType</code>.
     *
     * @param root -- root type from which <code>managedType</code> has been derived (calculated props added)
     * @param managedType
     * @param propertyName
     * @return
     */
    private static List<NewProperty> generateCriteriaProperties(final Class<?> root, final Class<?> managedType, final String propertyName) {
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : determinePropertyType(managedType, propertyName);
        final CritOnly critOnlyAnnotation = isEntityItself ? null : getPropertyAnnotation(CritOnly.class, managedType, propertyName);
        final Pair<String, String> titleAndDesc = getCriteriaTitleAndDesc(managedType, propertyName);
        final List<NewProperty> generatedProperties = new ArrayList<>();
        
        final IsProperty isPropertyAnnotation = isEntityItself ? null : getPropertyAnnotation(IsProperty.class, managedType, propertyName);
        final List<Annotation> additionalAnnotations = isDate(propertyType) ? copyDateAnnotations(managedType, propertyName) : emptyList();
        if (isDoubleCriterion(managedType, propertyName)) {
            generatedProperties.addAll(generateRangeCriteriaProperties(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation, isPropertyAnnotation, additionalAnnotations));
        } else {
            generatedProperties.add(generateSingleCriteriaProperty(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation, isPropertyAnnotation, additionalAnnotations));
        }
        return generatedProperties;
    }

    /**
     * Copies date-related property annotations.
     * 
     * @param managedType
     * @param propertyName
     * @return
     */
    private static List<Annotation> copyDateAnnotations(final Class<?> managedType, final String propertyName) {
        return of(DateOnly.class, TimeOnly.class, PersistentType.class)
            .map(annotationType -> getPropertyAnnotationOptionally(annotationType, managedType, propertyName))
            .flatMap(annotation -> annotation.isPresent() ? of(annotation.get()) : empty())
            .map(annotation -> {
                if (annotation instanceof DateOnly) {
                    return newDateOnlyAnnotation();
                } else if (annotation instanceof TimeOnly) {
                    return newTimeOnlyAnnotation();
                } else {
                    return newUtcAnnotation();
                }
            })
            .collect(toList());
    }

    /**
     * Generates criteria property with appropriate annotations.
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @param additionalAnnotations -- additional annotations to be generated in criteria property
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static NewProperty generateSingleCriteriaProperty(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation, final IsProperty isPropertyAnnotation, final List<Annotation> additionalAnnotations) {
        final boolean isEntity = EntityUtils.isEntityType(propertyType);
        final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
        final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (EntityUtils.isBoolean(propertyType) ? boolean.class : propertyType);

        final List<Annotation> annotations = new ArrayList<>();
        if (isEntity && isSingle) {
            annotations.add(new SkipEntityExistsValidationAnnotation(false, false).newInstance());
        }
        if (isEntity && !isSingle && EntityUtils.isCollectional(newPropertyType)) {
            annotations.add(new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
            annotations.add(new EntityTypeAnnotation((Class<? extends AbstractEntity<?>>) propertyType).newInstance());
        } else {
            if (isPropertyAnnotation != null) {
                annotations.add(new IsPropertyAnnotation().copyFrom(isPropertyAnnotation));
            }
        }
        annotations.add(new CriteriaPropertyAnnotation(managedType, propertyName).newInstance());
        annotations.add(new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());
        annotations.addAll(additionalAnnotations);
        return new NewProperty(CriteriaReflector.critName(root, propertyName), newPropertyType, titleAndDesc.getKey(), titleAndDesc.getValue(), annotations.toArray(new Annotation[0]));
    }

    /**
     * Generates two criteria properties for range properties (i. e. number, money, date or boolean properties).
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @param additionalAnnotations -- additional annotations to be generated in criteria properties
     * @return
     */
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation, final IsProperty isPropertyAnnotation, final List<Annotation> additionalAnnotations) {
        final String firstPropertyName = CriteriaReflector.critName(root, EntityUtils.isBoolean(propertyType) ? is(propertyName) : from(propertyName));
        final String secondPropertyName = CriteriaReflector.critName(root, EntityUtils.isBoolean(propertyType) ? not(propertyName) : to(propertyName));
        final Class<?> newPropertyType = EntityUtils.isBoolean(propertyType) ? boolean.class : propertyType;
        
        final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, titleAndDesc.getKey(), titleAndDesc.getValue(), createAnnotations(true, managedType, secondPropertyName, propertyName, isPropertyAnnotation, additionalAnnotations));
        final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, titleAndDesc.getKey(), titleAndDesc.getValue(), createAnnotations(false, managedType, firstPropertyName, propertyName, isPropertyAnnotation, additionalAnnotations));
        
        return listOf(firstProperty, secondProperty);
    }
    
    private static Annotation[] createAnnotations(final boolean first, final Class<?> managedType, final String otherPropertyName, final String originalPropertyName, final IsProperty isProperty, final List<Annotation> additionalAnnotations) {
        final List<Annotation> annotations = new ArrayList<>();
        if (isProperty != null) {
            annotations.add(new IsPropertyAnnotation().copyFrom(isProperty));
        }
        if (first) {
            annotations.add(new FirstParamAnnotation(otherPropertyName).newInstance());
        } else {
            annotations.add(new SecondParamAnnotation(otherPropertyName).newInstance());
        }
        annotations.add(new CriteriaPropertyAnnotation(managedType, originalPropertyName).newInstance());
        annotations.add(new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());
        annotations.addAll(additionalAnnotations);
        return annotations.toArray(new Annotation[0]);
    }
    
    /**
     * Synchronises entity query criteria property values with domain tree model.
     *
     * @param criteriaEntity
     */
    private static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> void synchroniseWithModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteriaEntity) {
        // LOGGER.error(format("synchroniseWithModel started..."));
        final Class<T> root = criteriaEntity.getEntityClass();
        final IAddToCriteriaTickManager ftm = criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        CriteriaReflector.getCriteriaProperties(criteriaEntity.getType()).stream().map(propertyField -> {
            final String critPropName = getAnnotation(propertyField, CriteriaProperty.class).propertyName();
            return t2(propertyField, getAnnotation(propertyField, SecondParam.class) == null ? ftm.getValue(root, critPropName) : ftm.getValue2(root, critPropName));
        }).collect(toList()).forEach(fieldAndVal -> { // there is a need to collect all results BEFORE forEach processing due to mutable nature of 'getValue*' methods
            final Field field = fieldAndVal._1;
            try {
                // LOGGER.error(format("\tsynchroniseWithModel prop [%s] setting... val = [%s]", field.getName(), fieldAndVal._2));
                // need to enforce the setting to ensure invocation of SynchroniseCriteriaWithModelHandler; this will ensure application of editable / required (and other) attributes and integrity of property dependencies
                criteriaEntity.getProperty(field.getName()).setValue(fieldAndVal._2, true);
                // LOGGER.error("\tsynchroniseWithModel. valResult = " + criteriaEntity.getProperty(field.getName()).getFirstFailure());
            } catch (final Exception ex) {
                LOGGER.warn(format("\tCould not assign crit value to [%s] in root [%s].", field.getName(), root.getName()));
            } 
            // finally { LOGGER.error(format("\tsynchroniseWithModel prop [%s] setting...done", field.getName())); }
        });
        criteriaEntity.critOnlySinglePrototypeOptional().ifPresent(cosPrototype -> {
            // At this stage 'cosPrototype' is in initialising phase, aka 'cosPrototype.isInitialising() == true'.
            // This was done deliberately to prevent invoking of cosPrototype's definers with 'isInitialising == false' marker, that triggers defining (changing) of other properties, sometimes in badly "overriding" order.
            // Still, we need to validate and define (with isInitialising == true) all properties.
            final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) root;
            
            // Validation need be performed first - we must avoid defining for invalid properties, that are really possible in cdtmae from which criteria entity is derived.
            // For that purpose we invoke meta-property revalidation.
            // MetaPropertyFull.revalidate implementation requires 'assigned == true' for revalidation to be actually performed.
            // ObservableMutatorInterceptor guarantees that 'assigned == true' if value is set in isInitialising phase.
            // Thus we can be sure that all crit-only single props will be revalidated.
            cosPrototype.endInitialising(); // this initialising:=false setting is made to ensure that if revalidate method is changed (aka made dependent on isInitialising flag) revalidation still will be processed
            cosPrototype.nonProxiedProperties().filter(mp -> isCritOnlySingle(entityType, mp.getName())).forEach(mp -> {
                // LOGGER.error(format("\t\trevalidate... property [%s]", mp.getName()));
                mp.revalidate(true); // it is very handy to ignore requiredness here instead of making some special clearing after revalidation
            });
            cosPrototype.beginInitialising();
            
            // Only valid crit-only single properties should be defined.
            // DefinersExecutor is not applicable for the following reasons:
            //  1. setOriginalValue should not be actioned, because original values we leave empty.
            //  2. do not need to process entity-typed values; they can even be instrumented for some reason, however DefinersExecutor does not allow this.
            cosPrototype.nonProxiedProperties().filter(mp -> isCritOnlySingle(entityType, mp.getName()) && mp.isValid()).forEach(mp -> {
                final MetaProperty<Object> metaProp = (MetaProperty<Object>) mp;
                metaProp.define(metaProp.getValue());
            });
            cosPrototype.endInitialising();
            
            // take a snapshot of all needed crit-only single prop information to be applied back against criteriaEntity
            final Stream<MetaProperty<?>> snapshot = criteriaEntity.critOnlySinglePrototype().nonProxiedProperties().filter(metaProp -> isCritOnlySingle(entityType, metaProp.getName()));
            // apply the snapshot against criteriaEntity
            applySnapshot(criteriaEntity, snapshot);
        });
        // LOGGER.error(format("synchroniseWithModel started...done"));
    }
}
