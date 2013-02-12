/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Value matcher that uses {@link EntityQuery} inside and, thus, it can be provided with already formed queries.
 *
 * @author TG Team
 */
public class EntityQueryValueMatcher<T extends AbstractEntity<?>> implements IValueMatcher<T> {

    private final IEntityDao<T> dao;

    private final EntityResultQueryModel<T> queryModel;
    private final OrderingModel defaultOrdering;
    private fetch<T> fetchModel;
    private final fetch<T> defaultFetchModel;
    private final String propertyParamName;
    private final String propertyName;

    private int pageSize = 10;

    /**
     * Creates a matcher that matches entities by their property <code>propertyName</code> (can be a dot notated property) and orders the result set by property <code>orderBy</code> (can also be a dot notated property).
     *
     * @param dao
     * @param propertyName
     * @param orderBy
     */
    public EntityQueryValueMatcher(final IEntityDao<T> dao, final String propertyName, final String orderBy) {
	this.dao = dao;
	this.defaultFetchModel = produceDefaultFetchModel(dao.getEntityType());
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;

	// Entity's key may have a composite nature.
	// In order to support more intuitive autocompletion for such entities, it is necessary to enhance matching query
	final Pair<Class<?>, String> pair = PropertyTypeDeterminator.transform(dao.getEntityType(), propertyName);
	if (DynamicEntityKey.class != AnnotationReflector.getKeyType(pair.getKey())) { // the key is not composite
	    this.queryModel = select(dao.getEntityType()).where().prop(propertyName).iLike().param(propertyParamName).model();
	} else { // the key is composite
	    final String additionalSearchProp = createSearchProp(propertyName, pair.getKey());
	    // create query model, which is the same for non-composite case, but with an additional criteria for the property representing the last composite key member
	    this.queryModel = select(dao.getEntityType()).where().//
		    begin().//
		    	prop(propertyName).iLike().param(propertyParamName).or().//
		    	prop(additionalSearchProp).iLike().param(propertyParamName).//
		    end().model();//
	}

	this.defaultOrdering = orderBy().yield(orderBy).asc().model();
    }

    /**
     * Creates a search property based on the original search property <code>propertyName</code> and a pair
     *
     * @param propertyName
     * @param pair
     * @return
     */
    private static String createSearchProp(final String propertyName, final Class<?> typeForEntityWithCompositeKey) {
	// find key members... the last one is of our interest
	final List<Field> keyMembers = Finder.getKeyMembers(typeForEntityWithCompositeKey);
	// form new search property that should be based on the dot notated propertyName without the last portion + the name of the last composite key member
	// if propertyName represent a single property (i.e. not dot notated) then simply the last composite key member should be used
	final String additionalSearchProp = (propertyName.contains(".") ? PropertyTypeDeterminator.penultAndLast(propertyName).getKey() + "." : "")
	    + keyMembers.get(keyMembers.size() - 1).getName();
	return additionalSearchProp;
    }

    /**
     * Creates a matcher that uses property <code>key</code> for ordering the result set.
     *
     * @param dao
     * @param propertyName
     */
    public static <T extends AbstractEntity<?>> EntityQueryValueMatcher<T> matchByKey(final IEntityDao<T> dao) {
	return new EntityQueryValueMatcher<T>(dao, "key", "key");
    }

    /**
     * Creates a matcher based on the passed compound condition, which gets enhanced with "like" expression for propertyName.
     *
     * @param entityQuery
     * @param propertyName
     *            -- should contain alias
     */
    public EntityQueryValueMatcher(final IEntityDao<T> dao, final ICompoundCondition0<T> condition, final String propertyName) {
	this.dao = dao;
	this.defaultFetchModel = produceDefaultFetchModel(dao.getEntityType());
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;
	this.queryModel = condition.and().prop(propertyName).iLike().param(propertyParamName).model();
	this.defaultOrdering = null;
    }

    @Override
    public List<T> findMatches(final String value) {
	return dao.getPage(from(queryModel).with(defaultOrdering).with(propertyParamName, value).with(defaultFetchModel).model(), 0, pageSize).data();
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
	return dao.getPage(from(queryModel).with(defaultOrdering).with(propertyParamName, value).with(fetchModel).model(), 0, pageSize).data();
    }

    public EntityQueryValueMatcher<T> setPageSize(final int pageSize) {
	this.pageSize = pageSize;
	return this;
    }

    @Override
    public Integer getPageSize() {
	return pageSize;
    }

    public String getPropertyName() {
	return propertyName;
    }

    @Override
    public <FT extends AbstractEntity<?>> fetch<FT> getFetchModel() {
	return (fetch<FT>) fetchModel;
    }

    @Override
    public <FT extends AbstractEntity<?>> void setFetchModel(final fetch<FT> fetchModel) {
	this.fetchModel = (fetch<T>) fetchModel;
    }

    private fetch<T> produceDefaultFetchModel(final Class<T> entityType) {
	final fetch<T> fetchWithKeyOnly = fetchOnly(dao.getEntityType()).with("key");
	return EntityUtils.hasDescProperty(entityType) ? fetchWithKeyOnly.with("desc") : fetchWithKeyOnly;
    }


    public static void main(final String[] args) {
	System.out.println("key1.key2.key3".contains("."));
	System.out.println(PropertyTypeDeterminator.penultAndLast("key1.key2.key3"));
    }
}
