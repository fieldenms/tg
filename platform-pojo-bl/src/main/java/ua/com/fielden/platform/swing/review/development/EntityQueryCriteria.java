package ua.com.fielden.platform.swing.review.development;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;

    private final C cdtme;

    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory){
	this.valueMatcherFactory = valueMatcherFactory;

	//This values should be initialized through reflection.
	this.dao = null;
	this.cdtme = null;
    }

    //    public DAO getDao() {
    //	return dao;
    //    }
    //
    //    public IEntityAggregatesDao getEntityAggregatesDao() {
    //	return entityAggregatesDao;
    //    }
    //
    //    public IValueMatcherFactory getValueMatcherFactory() {
    //	return valueMatcherFactory;
    //    }

    public C getCentreDomainTreeMangerAndEnhancer(){
	return cdtme;
    }

    public Class<T> getEntityClass(){
	return dao.getEntityType();
    }

    /**
     * Must load default values for the properties of the binding entity.
     */
    public void defaultValues(){
	final IAddToCriteriaTickRepresentation ftr = getCentreDomainTreeMangerAndEnhancer().getRepresentation().getFirstTick();
	for(final Field propertyField : CriteriaReflector.getCriteriaProperties(getType())){
	    final SecondParam secondParam = propertyField.getAnnotation(SecondParam.class);
	    final CriteriaProperty critProperty = propertyField.getAnnotation(CriteriaProperty.class);
	    final Class<T> root = getEntityClass();
	    set(propertyField.getName(), secondParam == null ? ftr.getValueByDefault(root, critProperty.propertyName()) : ftr.getValue2ByDefault(root, critProperty.propertyName()));
	}
    }

    /**
     * Determines whether default values can be set or not.
     *
     * @return
     */
    public boolean isDefaultEnabled(){
	return !CriteriaReflector.getCriteriaProperties(getType()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public IValueMatcher<?> getValueMatcher(final String propertyName) {
	if (valueMatchers.get(propertyName) == null) {
	    valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher((Class<? extends AbstractEntity<?>>) getType(), propertyName));
	}
	return valueMatchers.get(propertyName);
    }
}