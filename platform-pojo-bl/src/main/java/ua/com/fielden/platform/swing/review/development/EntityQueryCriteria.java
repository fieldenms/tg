package ua.com.fielden.platform.swing.review.development;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;

import com.google.inject.Inject;

@KeyType(String.class)
public abstract class EntityQueryCriteria<C extends IDomainTreeManager, T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractEntity<String> {

    private static final long serialVersionUID = 9154466083364529734L;

    private final Map<String, IValueMatcher> valueMatchers = new HashMap<String, IValueMatcher>();
    private final IValueMatcherFactory valueMatcherFactory;

    private final DAO dao;
    private final IEntityAggregatesDao entityAggregatesDao;

    private final C dtm;

    @Inject
    public EntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IEntityAggregatesDao entityAggregatesDao){
	this.valueMatcherFactory = valueMatcherFactory;
	this.entityAggregatesDao = entityAggregatesDao;

	//This values should be initialized through reflection.
	this.dao = null;
	this.dtm = null;
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

    public C getDomainTreeManger(){
	return dtm;
    }

    //TODO What entity class it should return entity class or enhanced entity class? Please consider that.
    //As far as this class might be generated with asm then one must consider whether criteria generation algorithm works correctly.
    public Class<T> getEntityClass(){
	return dao.getEntityType();
    }

    //TODO must implement later.
}
