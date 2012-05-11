package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.LocatorEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.factory.AfterChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CriteriaPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.EntityTypeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.FirstParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.SecondParamAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EnhancedLocatorEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * The implementation of the {@link ICriteriaGenerator} that generates {@link EntityQueryCriteria} with criteria properties.
 *
 * @author TG Team
 *
 */
public class CriteriaGenerator implements ICriteriaGenerator {

    private static final Logger logger = Logger.getLogger(CriteriaGenerator.class);

    private static final String _IS = "_is", _NOT = "_not", _FROM = "_from", _TO = "_to";

    private final EntityFactory entityFactory;

    private final IDaoFactory daoFactory;

    @Inject
    public CriteriaGenerator(final EntityFactory entityFactory, final IDaoFactory daoFactory){
	this.entityFactory = entityFactory;
	this.daoFactory = daoFactory;
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>)generateQueryCriteria(root, cdtme, CentreEntityQueryCriteriaToEnhance.class);
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>> generateLocatorQueryCriteria(final Class<T> root, final ILocatorDomainTreeManagerAndEnhancer ldtme) {
	return (EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>>)generateQueryCriteria(root, ldtme, LocatorEntityQueryCriteriaToEnhance.class);
    }

    private <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> EntityQueryCriteria<CDTME, T, IEntityDao<T>> generateQueryCriteria(final Class<T> root, final CDTME cdtme, final Class<? extends EntityQueryCriteria> entityClass) {
	final List<NewProperty> newProperties = new ArrayList<NewProperty>();
	for(final String propertyName : cdtme.getFirstTick().checkedProperties(root)){
	    if (!AbstractDomainTree.isPlaceholder(propertyName)) {
		newProperties.addAll(generateCriteriaProperties(root, cdtme.getEnhancer(), propertyName));
	    }
	}
	final DynamicEntityClassLoader cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

	try {
	    final Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>> queryCriteriaClass = (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) cl.startModification(entityClass.getName()).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
	    final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity = entityFactory.newByKey(queryCriteriaClass, "not required");

	    //Set dao for generated entity query criteria.
	    final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
	    final boolean isDaoAccessible = daoField.isAccessible();
	    daoField.setAccessible(true);
	    daoField.set(entity, daoFactory.newDao(root));
	    daoField.setAccessible(isDaoAccessible);

	    //Set domain tree manager for entity query criteria.
	    final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "cdtme");
	    final boolean isCdtmeAccessible = dtmField.isAccessible();
	    dtmField.setAccessible(true);
	    dtmField.set(entity, cdtme);
	    dtmField.setAccessible(isCdtmeAccessible);

	    //Add change support to the entity query criteria instance
	    //in order to synchronise entity query criteria values with model values
	    synchroniseWithModel(entity);

	    return entity;
	} catch (final Exception e) {
	    logger.error(e);
	    e.printStackTrace();
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
	boolean isCalculated = false;
	try{
	    enhancer.getCalculatedProperty(root, propertyName);
	    isCalculated = true;
	}catch(final Exception e){

	}
	final Class<?> inspectedType = isCalculated ? enhancer.getManagedType(root) : root;
	final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
	final Class<?> propertyType = isEntityItself ? inspectedType : PropertyTypeDeterminator.determinePropertyType(inspectedType, propertyName);
	final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, inspectedType, propertyName);
	final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(inspectedType, propertyName);

	final List<NewProperty> generatedProperties = new ArrayList<NewProperty>();

	if((EntityUtils.isRangeType(propertyType) || isBoolean(propertyType)) //
		&& !(critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value()))){
	    generatedProperties.addAll(generateRangeCriteriaProperties(root, propertyType, propertyName, titleAndDesc));
	}else{
	    generatedProperties.add(generateSingleCriteriaProperty(root, propertyType, propertyName, titleAndDesc, critOnlyAnnotation));
	}
	return generatedProperties;
    }

    /**
     * Generates criteria property with appropriate annotations.
     *
     * @param root
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @return
     */
    private static NewProperty generateSingleCriteriaProperty(final Class<?> root, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation) {
	final boolean isEntity = EntityUtils.isEntityType(propertyType);
	final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
	final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (isBoolean(propertyType) ? Boolean.class : propertyType);

	final List<Annotation> annotations = new ArrayList<Annotation>(){{
	    if(isEntity && !isSingle && EntityUtils.isCollectional(newPropertyType)){
		add(new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
		add(new EntityTypeAnnotation((Class<? extends AbstractEntity>) propertyType).newInstance());
	    }
	    add(new CriteriaPropertyAnnotation(propertyName).newInstance());
	    add(new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());
	}};

	return new NewProperty(CriteriaReflector.generateCriteriaPropertyName(root, propertyName, ""), newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), annotations.toArray(new Annotation[0]));
    }

    /**
     * Generates two criteria properties for range properties (i. e. number, money, date or boolean properties).
     *
     * @param root
     * @param propertyType
     * @param propertyName
     * @return
     */
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc) {
	final String firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _IS : _FROM);
	final String secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _NOT : _TO);
	final Class<?> newPropertyType = isBoolean(propertyType) ? Boolean.class : propertyType;

	final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		new CriteriaPropertyAnnotation(propertyName).newInstance(), new FirstParamAnnotation(secondPropertyName).newInstance(), new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());
	final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		new CriteriaPropertyAnnotation(propertyName).newInstance(), new SecondParamAnnotation(firstPropertyName).newInstance(), new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());

	return new ArrayList<NewProperty>() {{ add(firstProperty); add(secondProperty); }};
    }

    /**
     *Synchronises entity query criteria property values with domain tree model.
     * 
     * @param entity
     */
    private static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> void synchroniseWithModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity){
	final IAddToCriteriaTickManager ftm = entity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
	for(final Field propertyField : CriteriaReflector.getCriteriaProperties(entity.getType())){
	    final SecondParam secondParam = propertyField.getAnnotation(SecondParam.class);
	    final CriteriaProperty critProperty = propertyField.getAnnotation(CriteriaProperty.class);
	    final Class<T> root = entity.getEntityClass();
	    entity.set(propertyField.getName(), secondParam == null ? ftm.getValue(root, critProperty.propertyName()) : ftm.getValue2(root, critProperty.propertyName()));
	}
    }

    /**
     * Returns value that indicates whether specified type is of boolean type.
     *
     * @param type - the type that must be checked whether it is boolean or not.
     * @return
     */
    private static boolean isBoolean(final Class<?> type){
	return boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type);
    }

}
