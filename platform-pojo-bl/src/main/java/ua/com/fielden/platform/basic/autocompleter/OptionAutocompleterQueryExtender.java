package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.equery.equery.select;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndOrdered;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.EntityQueryCriteriaExtender;

public class OptionAutocompleterQueryExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteriaExtender<T, DAO, IPage<T>> {

    private Object value;
    private fetch<?> fetchModel;
    private boolean searchByDesc, searchByKey;
    private transient IBindingEntity entity;
    private transient final String propertyName;

    public OptionAutocompleterQueryExtender(final IBindingEntity entity, final String propertyName) {
	this.entity = entity;
	this.propertyName = propertyName;
    }

    @Override
    public IPage<T> runExtendedQuery(final int pageSize) {
	final IQueryModel<T> baseQuerySubmodel = getBaseQueryModel().model(getBaseCriteria().getEntityClass());
	final IWhere whereStatement = select(baseQuerySubmodel).where();
	ICompoundCondition compoundStatement = null;
	if (searchByKey && searchByDesc) {
	    compoundStatement = whereStatement.begin().prop("key").like().val(value).or().prop("desc").like().val(value).end();
	} else if (searchByKey) {
	    compoundStatement = whereStatement.prop("key").like().val(value);
	} else if (searchByDesc) {
	    compoundStatement = whereStatement.prop("desc").like().val(value);
	}

	// process dependent properties, which need to be added to the filtering condition
	if (entity instanceof AbstractEntity) {
	    @SuppressWarnings("rawtypes")
	    final AbstractEntity ent = (AbstractEntity) entity;
	    final MetaProperty searchProp = ent.getProperty(propertyName);
	    for (final String dependentProperty : ent.getProperty(propertyName).getDependentPropertyNames()) {
		if (Finder.isPropertyPresent(searchProp.getType(), dependentProperty)) {
		    final Object value = ent.get(dependentProperty);
		    compoundStatement = compoundStatement == null ? whereStatement.prop(dependentProperty).eq().val(value)
			    : compoundStatement.and().prop(dependentProperty).eq().val(value);
		}
	    }
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



    public IBindingEntity getEntity() {
        return entity;
    }



    public void setEntity(final IBindingEntity entity) {
        this.entity = entity;
    }

}
