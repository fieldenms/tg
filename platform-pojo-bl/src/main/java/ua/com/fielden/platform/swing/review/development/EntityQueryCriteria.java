package ua.com.fielden.platform.swing.review.development;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>, DAO extends IEntityDao2<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    private final Map<String, IValueMatcher2> valueMatchers = new HashMap<String, IValueMatcher2>();
    private final IValueMatcherFactory2 valueMatcherFactory;

    private final DAO dao;

    private final C cdtme;

    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory2 valueMatcherFactory){
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

    //TODO What entity class it should return entity class or enhanced entity class? Please consider that.
    //As far as this class might be generated with asm then one must consider whether criteria generation algorithm works correctly.
    public Class<T> getEntityClass(){
	return dao.getEntityType();
    }

    //TODO must implement later.

    /**
     * Must load default values for the properties of the binding entity.
     */
    public void defaultValues(){
	//TODO implement default values for this criteria values.
    }

    /**
     * Determines whether default values can be set or not.
     *
     * @return
     */
    public boolean isDefaultEnabled(){
	//TODO implement is default enable method
	return false;
    }

    @SuppressWarnings("unchecked")
    public IValueMatcher2<?> getValueMatcher(final String propertyName) {
	if (valueMatchers.get(propertyName) == null) {
	    valueMatchers.put(propertyName, valueMatcherFactory.getValueMatcher((Class<? extends AbstractEntity<?>>) getType(), propertyName));
	}
	return valueMatchers.get(propertyName);
    }
}