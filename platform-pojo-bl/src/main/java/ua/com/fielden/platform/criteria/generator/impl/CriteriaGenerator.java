package ua.com.fielden.platform.criteria.generator.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.LocatorEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.AfterChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CriteriaPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.EntityTypeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.FirstParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.SecondParamAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedLocatorEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * The implementation of the {@link ICriteriaGenerator} that generates {@link EntityQueryCriteria} with criteria properties.
 *
 * @author TG Team
 *
 */
public class CriteriaGenerator implements ICriteriaGenerator {

    private static final Logger LOGGER = Logger.getLogger(CriteriaGenerator.class);

    private final EntityFactory entityFactory;

    private final ICompanionObjectFinder coFinder;

    private final Map<Class<?>, Class<?>> generatedClasses;

    @Inject
    public CriteriaGenerator(final EntityFactory entityFactory, final ICompanionObjectFinder controllerProvider) {
        this.entityFactory = entityFactory;
        this.coFinder = controllerProvider;
        this.generatedClasses = new WeakHashMap<>();
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final Annotation... customAnnotations) {
        return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, cdtme, CentreEntityQueryCriteriaToEnhance.class, null, customAnnotations);
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>> generateLocatorQueryCriteria(final Class<T> root, final ILocatorDomainTreeManagerAndEnhancer ldtme, final Annotation... customAnnotations) {
        return (EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, ldtme, LocatorEntityQueryCriteriaToEnhance.class, null, customAnnotations);
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final Class<?> miType, final Annotation... customAnnotations) {
        return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, cdtme, CentreEntityQueryCriteriaToEnhance.class, miType, customAnnotations);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> EntityQueryCriteria<CDTME, T, IEntityDao<T>> generateQueryCriteria(final Class<T> root, final CDTME cdtme, final Class<? extends EntityQueryCriteria> entityClass, final Class<?> miType, final Annotation... customAnnotations) {
        try {
            final Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>> queryCriteriaClass;

            if (miType != null && generatedClasses.containsKey(miType)) {
                queryCriteriaClass = (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) generatedClasses.get(miType);
            } else {
                final List<NewProperty> newProperties = new ArrayList<>();
                for (final String propertyName : cdtme.getFirstTick().checkedProperties(root)) {
                    if (!AbstractDomainTree.isPlaceholder(propertyName)) {
                        newProperties.addAll(generateCriteriaProperties(root, cdtme.getEnhancer(), propertyName));
                    }
                }
                final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());

                queryCriteriaClass = (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) cl.startModification(entityClass.getName()).addClassAnnotations(customAnnotations).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
                generatedClasses.put(miType, queryCriteriaClass);
            }

            final DefaultEntityProducerWithContext<EntityQueryCriteria<CDTME, T, IEntityDao<T>>> entityProducer = new DefaultEntityProducerWithContext<>(entityFactory, (Class<EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) queryCriteriaClass, coFinder);
            final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity = entityProducer.newEntity();
            entity.beginInitialising();
            entity.setKey("not required");
            entity.endInitialising();

            //Set dao for generated entity query criteria.
            final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
            final boolean isDaoAccessable = daoField.isAccessible();
            daoField.setAccessible(true);
            daoField.set(entity, coFinder.find(root));
            daoField.setAccessible(isDaoAccessable);

            //Set domain tree manager for entity query criteria.
            final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "cdtme");
            final boolean isCdtmeAccessable = dtmField.isAccessible();
            dtmField.setAccessible(true);
            dtmField.set(entity, cdtme);
            dtmField.setAccessible(isCdtmeAccessable);

            //Add change support to the entity query criteria instance
            //in order to synchronise entity query criteria values with model values
            synchroniseWithModel(entity);

            return entity;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates criteria properties for specified list of properties and their root type.
     *
     * @param root
     * @param propertyName
     * @return
     */
    private static List<NewProperty> generateCriteriaProperties(final Class<?> root, final IDomainTreeEnhancer enhancer, final String propertyName) {
        final Class<?> managedType = enhancer.getManagedType(root);
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);
        final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, propertyName);
        final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        final List<NewProperty> generatedProperties = new ArrayList<NewProperty>();
        
        final IsProperty isPropertyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(IsProperty.class, managedType, propertyName);
        if (isDoubleCriterion(managedType, propertyName)) {
            generatedProperties.addAll(generateRangeCriteriaProperties(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation, isPropertyAnnotation));
        } else {
            generatedProperties.add(generateSingleCriteriaProperty(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation, isPropertyAnnotation));
        }
        return generatedProperties;
    }

    /**
     * Generates criteria property with appropriate annotations.
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static NewProperty generateSingleCriteriaProperty(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation, final IsProperty isPropertyAnnotation) {
        final boolean isEntity = EntityUtils.isEntityType(propertyType);
        final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
        final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (EntityUtils.isBoolean(propertyType) ? boolean.class : propertyType);

        final List<Annotation> annotations = new ArrayList<Annotation>();
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
        return new NewProperty(CriteriaReflector.generateCriteriaPropertyName(root, propertyName), newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), annotations.toArray(new Annotation[0]));
    }

    /**
     * Generates two criteria properties for range properties (i. e. number, money, date or boolean properties).
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @return
     */
    @SuppressWarnings("serial")
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation, final IsProperty isPropertyAnnotation) {
        //final boolean isEntityItself = "".equals(propertyName);
        final String firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? is(propertyName) : from(propertyName));
        final String secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? not(propertyName) : to(propertyName));
        final Class<?> newPropertyType = EntityUtils.isBoolean(propertyType) ? boolean.class : propertyType;
        
        final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), createAnnotations(true, managedType, secondPropertyName, propertyName, isPropertyAnnotation));
        final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), createAnnotations(false, managedType, firstPropertyName, propertyName, isPropertyAnnotation));
        
        return new ArrayList<NewProperty>() {
            {
                add(firstProperty);
                add(secondProperty);
            }
        };
    }
    
    private static Annotation[] createAnnotations(final boolean first, final Class<?> managedType, final String otherPropertyName, final String originalPropertyName, final IsProperty isProperty) {
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
        return annotations.toArray(new Annotation[0]);
    }
    
    /**
     * Synchronises entity query criteria property values with domain tree model.
     *
     * @param entity
     */
    private static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> void synchroniseWithModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity) {
        // LOGGER.error(format("synchroniseWithModel started..."));
        final Class<T> root = entity.getEntityClass();
        final IAddToCriteriaTickManager ftm = entity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        CriteriaReflector.getCriteriaProperties(entity.getType()).stream().map(propertyField -> {
            final String critPropName = getAnnotation(propertyField, CriteriaProperty.class).propertyName();
            return t2(propertyField, getAnnotation(propertyField, SecondParam.class) == null ? ftm.getValue(root, critPropName) : ftm.getValue2(root, critPropName));
        }).collect(toList()).forEach(fieldAndVal -> { // there is a need to collect all results BEFORE forEach processing due to mutable nature of 'getValue*' methods
            final Field field = fieldAndVal._1;
            try {
                // LOGGER.error(format("\tsynchroniseWithModel prop [%s] setting... val = [%s]", field.getName(), fieldAndVal._2));
                // need to enforce the setting to ensure invocation of SynchroniseCriteriaWithModelHandler; this will ensure application of editable / required (and other) attributes and integrity of property dependencies
                entity.getProperty(field.getName()).setValue(fieldAndVal._2, true);
                // LOGGER.error("\tsynchroniseWithModel. valResult = " + entity.getProperty(field.getName()).getFirstFailure());
            } catch (final Exception ex) {
                LOGGER.warn(format("\tCould not assign crit value to [%s] in root [%s].", field.getName(), root.getName()));
            } 
            // finally { LOGGER.error(format("\tsynchroniseWithModel prop [%s] setting...done", field.getName())); }
        });
        // LOGGER.error(format("synchroniseWithModel started...done"));
    }
}
