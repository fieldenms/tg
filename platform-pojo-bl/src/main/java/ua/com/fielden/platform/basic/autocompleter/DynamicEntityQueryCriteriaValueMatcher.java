package ua.com.fielden.platform.basic.autocompleter;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;

public class DynamicEntityQueryCriteriaValueMatcher<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IValueMatcher<T> {

    private final IValueMatcher<T> valueMatcher;
    private final OptionAutocompleterQueryExtender<T, DAO> valueMatcherQueryExtender;
    private Runnable beforeFindMatchesAction;

    private boolean useQueryCriteira = false;

    public DynamicEntityQueryCriteriaValueMatcher(final IValueMatcher<T> valueMatcher) {
	this.valueMatcher = valueMatcher;
	this.valueMatcherQueryExtender = new OptionAutocompleterQueryExtender<T, DAO>();
    }

    @Override
    public void setFetchModel(final fetch<?> fetchModel) {
	this.valueMatcher.setFetchModel(fetchModel);
    }

    private void initlaizeBaseQueryModel() {
	if (beforeFindMatchesAction != null){
	    beforeFindMatchesAction.run();
	}
    }

    private List<T> findMatches(final String value, final fetch<?> fetchModel) {
	initlaizeBaseQueryModel();
	if (useQueryCriteira) {
	    valueMatcherQueryExtender.setFetchModel(fetchModel);
	    valueMatcherQueryExtender.setValue(value);
	    return valueMatcherQueryExtender.runExtendedQuery(valueMatcher.getPageSize()).data();
	}
	if (fetchModel == null) {
	    return valueMatcher.findMatches(value);
	} else {
	    return valueMatcher.findMatchesWithModel(value);
	}
    }

    @Override
    public List<T> findMatches(final String value) {
	return findMatches(value, null);
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
	return findMatches(value, getFetchModel());
    }

    public void setUseQueryCriteira(final boolean useQueryCriteira) {
	this.useQueryCriteira = useQueryCriteira;
    }

    public boolean isSearchByDesc() {
	return valueMatcherQueryExtender.isSearchByDesc();
    }

    public void setSearchByDesc(final boolean searchByDesc) {
	valueMatcherQueryExtender.setSearchByDesc(searchByDesc);
    }

    public boolean isSearchByKey() {
	return valueMatcherQueryExtender.isSearchByKey();
    }

    public void setSearchByKey(final boolean searchByKey) {
	valueMatcherQueryExtender.setSearchByKey(searchByKey);
    }

    public boolean isUseQueryCriteira() {
	return useQueryCriteira;
    }

    public EntityQueryCriteria<T, DAO> getBaseCriteria() {
	return valueMatcherQueryExtender.getBaseCriteria();
    }

    public void setBaseCriteria(final EntityQueryCriteria<T, DAO> baseCriteria) {
	valueMatcherQueryExtender.setBaseCriteria(baseCriteria);
    }

    @Override
    public Integer getPageSize() {
	return valueMatcher.getPageSize();
    }

    @Override
    public fetch<?> getFetchModel() {
	return this.valueMatcher.getFetchModel();
    }

    /**
     * Returns an action that will be performed before each {@link #findMatches(String)} and/or {@link #findMatchesWithModel(String)} methods.
     *
     * @return
     */
    public Runnable getBeforeFindMatchesAction() {
        return beforeFindMatchesAction;
    }

    /**
     * Sets an action that will be performed before each {@link #findMatches(String)} and/or {@link #findMatchesWithModel(String)} methods.
     *
     * @param beforeFindMatchesAction
     */
    public void setBeforeFindMatchesAction(final Runnable beforeFindMatchesAction) {
        this.beforeFindMatchesAction = beforeFindMatchesAction;
    }
}
