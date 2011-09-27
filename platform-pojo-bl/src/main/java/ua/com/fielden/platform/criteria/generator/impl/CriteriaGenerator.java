package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.EnhancedEntityQueryCriteria;
import ua.com.fielden.platform.criteria.enhanced.FirstParam;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.AnnotationDescriptor;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeManager;
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
    public <T extends AbstractEntity> EntityQueryCriteria<T, IEntityDao<T>> generateQueryCriteria(final Class<T> root, final ICriteriaDomainTreeManager cdtm) {
	final List<NewProperty> newProperties = new ArrayList<NewProperty>();
	for(final String propertyName : cdtm.getFirstTick().checkedProperties(root)){
	    newProperties.addAll(generateCriteriaProperties(root, propertyName));
	}

	final DynamicEntityClassLoader cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

	try {
	    final Class<? extends EntityQueryCriteria<T, IEntityDao<T>>> queryCriteriaClass = (Class<? extends EntityQueryCriteria<T, IEntityDao<T>>>)cl.startModification(EnhancedEntityQueryCriteria.class.getName()).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
	    final EntityQueryCriteria<T, IEntityDao<T>> entity = entityFactory.newByKey(queryCriteriaClass, "not required");
	    final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
	    final boolean isAccessible = daoField.isAccessible();
	    daoField.setAccessible(true);
	    daoField.set(entity, daoFactory.newDao(root));
	    daoField.setAccessible(isAccessible);
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
    private static List<NewProperty> generateCriteriaProperties(final Class<?> root, final String propertyName) {

	final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
	final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, propertyName);
	final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, root, propertyName);

	final List<NewProperty> generatedProperties = new ArrayList<NewProperty>();

	if((EntityUtils.isRangeType(propertyType) || isBoolean(propertyType)) //
		&& !(critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value()))){
	    generatedProperties.addAll(generateRangeCriteriaProperties(root, propertyType, propertyName));
	}else{
	    generatedProperties.add(generateSingleCriteriaProperty(root, propertyType, propertyName, critOnlyAnnotation));
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
    private static NewProperty generateSingleCriteriaProperty(final Class<?> root, final Class<?> propertyType, final String propertyName, final CritOnly critOnlyAnnotation) {
	final boolean isEntity = EntityUtils.isEntityType(propertyType);
	final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
	final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (isBoolean(propertyType) ? Boolean.class : propertyType);
	final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(root, propertyName);

	final List<AnnotationDescriptor> annotations = new ArrayList<AnnotationDescriptor>(){{
	    if(isEntity && !isSingle && EntityUtils.isCollectional(newPropertyType)){
		add(new AnnotationDescriptor(IsProperty.class, new HashMap<String, Object>() {{ put("value", String.class); }}));
		add(new AnnotationDescriptor(EntityType.class, new HashMap<String, Object>() {{ put("value", propertyType); }}));
	    }
	    add(generateCriteriaPropertyAnnotation(root, propertyName));
	}};

	return new NewProperty(CriteriaReflector.generateCriteriaPropertyName(root, propertyName, ""), newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), annotations.toArray(new AnnotationDescriptor[0]));
    }

    /**
     * Generates two criteria properties for range properties (i. e. number, money, date or boolean properties).
     * 
     * @param root
     * @param propertyType
     * @param propertyName
     * @return
     */
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> propertyType, final String propertyName) {
	final String firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _IS : _FROM);
	final String secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _NOT : _TO);
	final Class<?> newPropertyType = isBoolean(propertyType) ? Boolean.class : propertyType;
	final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(root, propertyName);

	final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		generateCriteriaPropertyAnnotation(root, propertyName), generateFirstParamAnnotation(secondPropertyName));
	final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		generateCriteriaPropertyAnnotation(root, propertyName), generateSecondParamAnnotation(firstPropertyName));

	return new ArrayList<NewProperty>() {{ add(firstProperty); add(secondProperty); }};
    }

    /**
     * Generates {@link AnnotationDescriptor} for {@link CriteriaProperty} annotation with specified root type and property name.
     * 
     * @param rootType
     * @param propertyName
     * @return
     */
    private static AnnotationDescriptor generateCriteriaPropertyAnnotation(final Class<?> rootType, final String propertyName){
	return new AnnotationDescriptor(CriteriaProperty.class, new HashMap<String, Object>() {{ put("rootType", rootType); put("propertyName", propertyName); }});
    }

    /**
     * Generates {@link AnnotationDescriptor} instance for {@link FirstParam} annotation with specified second property name.
     * 
     * @param secondProperty - the property name that is the pair for property annotated with this {@link FirstParam} annotation.
     * @return
     */
    private static AnnotationDescriptor generateFirstParamAnnotation(final String secondProperty){
	return new AnnotationDescriptor(FirstParam.class, new HashMap<String, Object>() {{ put("secondParam", secondProperty); }});
    }

    /**
     * Generates {@link AnnotationDescriptor} instance for {@link SecondParam} annotation with specified first property name.
     * 
     * @param firstProperty - the property name that is the pair for property annotated with this {@link SecondParam} annotation.
     * @return
     */
    private static AnnotationDescriptor generateSecondParamAnnotation(final String firstParam){
	return new AnnotationDescriptor(SecondParam.class, new HashMap<String, Object>() {{ put("firstParam", firstParam); }});
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
