/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
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

    private final EntityResultQueryModel<T> defaultModel;
    private final OrderingModel defaultOrdering;
    private fetch<T> fetchModel;

    private final String propertyParamName;
    private final String propertyName;

    private int pageSize = 10;

    public EntityQueryValueMatcher(final IEntityDao<T> dao, final String propertyName) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName;
	this.propertyName = propertyName;
	this.defaultModel = select(dao.getEntityType()).where().prop(propertyName).like().param(propertyParamName).model();
	this.defaultOrdering = orderBy().prop("key").asc().model();
    }

    public EntityQueryValueMatcher(final IEntityDao<T> dao, final String propertyName, final String orderBy) {
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
    public EntityQueryValueMatcher(final IEntityDao<T> dao, final ICompoundCondition0 condition, final String propertyName) {
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
}
