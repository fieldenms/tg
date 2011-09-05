package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.equery.equery.select;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndOrdered;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.EntityQueryCriteriaExtender;

public class OptionAutocompleterQueryExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteriaExtender<T, DAO, IPage<T>> {

    private Object value;
    private fetch<?> fetchModel;
    private boolean searchByDesc, searchByKey;

    public OptionAutocompleterQueryExtender() {
    }

    @Override
    public IPage<T> runExtendedQuery(final int pageSize) {
	final IQueryModel<T> baseQuerySubmodel = getBaseQueryModel().model(getBaseCriteria().getEntityClass());
	final IWhere whereStatement = select(baseQuerySubmodel).where();
	ICompoundCondition compoundStatement = null;
	if (searchByKey) {
	    compoundStatement = whereStatement.prop("key").like().val(value);
	}
	if (searchByDesc) {
	    compoundStatement = compoundStatement == null ? whereStatement.prop("desc").like().val(value) : compoundStatement.or().prop("desc").like().val(value);
	}
	final ICompletedAndOrdered ordered = compoundStatement == null ? null : (searchByKey ? compoundStatement.orderBy("key") : compoundStatement.orderBy("desc"));
	final IQueryOrderedModel<T> finalQueryModel = ordered == null ? baseQuerySubmodel : ordered.model(getBaseCriteria().getEntityClass());
	return getBaseCriteria().getDao().firstPage(finalQueryModel, (fetch<T>) getFetchModel(), pageSize);
    }

    public Object getValue() {
	return value;
    }

    public void setValue(final Object value) {
	this.value = value;
    }

    public fetch<?> getFetchModel() {
	return fetchModel;
    }

    public void setFetchModel(final fetch<?> fetchModel) {
	this.fetchModel = fetchModel;
    }

    public boolean isSearchByDesc() {
	return searchByDesc;
    }

    public void setSearchByDesc(final boolean searchByDesc) {
	this.searchByDesc = searchByDesc;
    }

    public boolean isSearchByKey() {
	return searchByKey;
    }

    public void setSearchByKey(final boolean searchByKey) {
	this.searchByKey = searchByKey;
    }

    @Override
    public IPage<T> exportExtendedQueryData() {
	throw new UnsupportedOperationException("The exporting in to the external file is unsupported yet.");
    }

}
