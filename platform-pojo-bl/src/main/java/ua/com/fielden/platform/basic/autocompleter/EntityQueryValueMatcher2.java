/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import static ua.com.fielden.platform.entity.query.fluent.query.from;
import static ua.com.fielden.platform.entity.query.fluent.query.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

/**
 * Value matcher that uses {@link EntityQuery} inside and, thus, it can be provided with already formed queries.
 *
 * @author TG Team
 */
public class EntityQueryValueMatcher2<T extends AbstractEntity<?>> implements IValueMatcher2<T> {

    private final IEntityDao2<T> dao;

    private final EntityResultQueryModel<T> defaultModel;
    private final OrderingModel defaultOrdering;
    private fetch<T> fetchModel;

    private final String propertyParamName;
    private final String propertyName;

    private int pageSize = 10;

    public EntityQueryValueMatcher2(final IEntityDao2<T> dao, final String propertyName) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName;
	this.propertyName = propertyName;
	this.defaultModel = select(dao.getEntityType()).where().prop(propertyName).like().param(propertyParamName).model();
	this.defaultOrdering = orderBy().prop("key").asc().model();
    }

    public EntityQueryValueMatcher2(final IEntityDao2<T> dao, final String propertyName, final String orderBy) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;
	this.defaultModel = select(dao.getEntityType()).where().prop(propertyName).like().param(propertyParamName).model();
	this.defaultOrdering = orderBy().prop(orderBy).asc().model();
    }

    /**
     * Instantiates matcher based on the passed compound condition, which gets enhanced with "like" expression for propertyName.
     *
     * @param entityQuery
     * @param propertyName
     *            -- should contain alias
     */
    public EntityQueryValueMatcher2(final IEntityDao2<T> dao, final ICompoundCondition0 condition, final String propertyName) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;
	this.defaultModel = condition.and().prop(propertyName).like().param(propertyParamName).model();
	this.defaultOrdering = null;
    }

    @Override
    public List<T> findMatches(final String value) {
	return dao.getPage(from(defaultModel).with(defaultOrdering).with(propertyParamName, value).build(), 0, pageSize).data();
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
	return dao.getPage(from(defaultModel).with(defaultOrdering).with(propertyParamName, value).with(fetchModel).build(), 0, pageSize).data();
    }

    public EntityQueryValueMatcher2<T> setPageSize(final int pageSize) {
	this.pageSize = pageSize;
	return this;
    }

    @Override
    public Integer getPageSize() {
	return pageSize;
    }

    @Override
    public void setFetchModel(final fetch<T> fetchModel) {
	this.fetchModel = fetchModel;
    }

    public String getPropertyName() {
	return propertyName;
    }

    @Override
    public fetch<T> getFetchModel() {
	return fetchModel;
    }
}
