package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.enhanced.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
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
    public <T extends AbstractEntity> EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManager cdtm) {
	final List<NewProperty> newProperties = new ArrayList<NewProperty>();
	for(final String propertyName : cdtm.getFirstTick().checkedProperties(root)){
	    newProperties.addAll(generateCriteriaProperties(root, propertyName));
	}

	final DynamicEntityClassLoader cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

	try {
	    final Class<? extends EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>>> queryCriteriaClass = (Class<? extends EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>>>)cl.startModification(EnhancedCentreEntityQueryCriteria.class.getName()).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
	    final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> entity = entityFactory.newByKey(queryCriteriaClass, "not required");

	    //Set dao for generated entity query criteria.
	    final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
	    final boolean isDaoAccessible = daoField.isAccessible();
	    daoField.setAccessible(true);
	    daoField.set(entity, daoFactory.newDao(root));
	    daoField.setAccessible(isDaoAccessible);

	    //Set domain tree manager for entity query criteria.
	    final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "dtm");
	    final boolean isDtmAccessible = dtmField.isAccessible();
	    dtmField.setAccessible(true);
	    dtmField.set(entity, cdtm);
	    dtmField.setAccessible(isDtmAccessible);

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

	final List<Annotation> annotations = new ArrayList<Annotation>(){{
	    if(isEntity && !isSingle && EntityUtils.isCollectional(newPropertyType)){
		add(new IsPropertyAnnotation(String.class).newInstance());
		add(new EntityTypeAnnotation((Class<? extends AbstractEntity>) propertyType).newInstance());
	    }
	    add(new CriteriaPropertyAnnotation(root, propertyName).newInstance());
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
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> propertyType, final String propertyName) {
	final String firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _IS : _FROM);
	final String secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName, isBoolean(propertyType) ? _NOT : _TO);
	final Class<?> newPropertyType = isBoolean(propertyType) ? Boolean.class : propertyType;
	final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(root, propertyName);

	final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		new CriteriaPropertyAnnotation(root, propertyName).newInstance(), new FirstParamAnnotation(secondPropertyName).newInstance());
	final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), //
		new CriteriaPropertyAnnotation(root, propertyName).newInstance(), new SecondParamAnnotation(firstPropertyName).newInstance());

	return new ArrayList<NewProperty>() {{ add(firstProperty); add(secondProperty); }};
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
