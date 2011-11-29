/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.equery.equery.select;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

/**
 * Value matcher that uses {@link EntityQuery} inside and, thus, it can be provided with already formed queries.
 *
 * @author TG Team
 */
public class EntityQueryValueMatcher<T extends AbstractEntity> implements IValueMatcher<T> {

    private final IEntityDao<T> dao;

    private final IQueryOrderedModel<T> defaultModel;
    private fetch<T> fetchModel;

    private final String propertyParamName;
    private final String propertyName;

    private int pageSize = 10;

    public EntityQueryValueMatcher(final IEntityDao<T> dao, final String propertyName) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName;
	this.propertyName = propertyName;
	this.defaultModel = select(dao.getEntityType()).where().prop(propertyName).like().param(propertyParamName).orderBy("key").model();
    }

    public EntityQueryValueMatcher(final IEntityDao<T> dao, final String propertyName, final String orderBy) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;
	this.defaultModel = select(dao.getEntityType()).where().prop(propertyName).like().param(propertyParamName).orderBy(orderBy).model();
    }

    /**
     * Instantiates matcher based on the passed compound condition, which gets enhanced with "like" expression for propertyName.
     *
     * @param entityQuery
     * @param propertyName
     *            -- should contain alias
     */
    public EntityQueryValueMatcher(final IEntityDao<T> dao, final ICompoundCondition condition, final String propertyName) {
	this.dao = dao;
	this.propertyParamName = "paramNameFor" + propertyName.replaceAll("\\.", "_");
	this.propertyName = propertyName;
	this.defaultModel = condition.and().prop(propertyName).like().param(propertyParamName).model();
    }

    @Override
    public List<T> findMatches(final String value) {
	defaultModel.setParamValue(propertyParamName, value);
	return dao.getPage(defaultModel, null, 0, pageSize).data();
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
	defaultModel.setParamValue(propertyParamName, value);
	return dao.getPage(defaultModel, fetchModel, 0, pageSize).data();
    }

    public EntityQueryValueMatcher<T> setPageSize(final int pageSize) {
	this.pageSize = pageSize;
	return this;
    }

    @Override
    public Integer getPageSize() {
	return pageSize;
    }

    @Override
    public void setFetchModel(final fetch fetchModel) {
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
