/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

/**
 * Key based value matcher, which supports context assignment.
 *
 * @author TG Team
 */
public abstract class AbstractSearchEntityByKeyWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>>
                      implements IValueMatcherWithContext<CONTEXT, T>, IValueMatcherWithFetch<T> {

    protected final IEntityDao<T> companion;
    private final fetch<T> defaultFetchModel;
    private fetch<T> fetchModel;
    private CONTEXT context;

    private final int pageSize = 10;


    public AbstractSearchEntityByKeyWithContext(final IEntityDao<T> companion) {
        this.companion = companion;
        this.defaultFetchModel = companion == null ? null : fetchKeyAndDescOnly(companion.getEntityType());
    }

    /**
     * This method may be overriden to create a different start of EQL.
     *
     * @param context
     * @param searchString
     * @return
     */
    protected ICompoundCondition0<T> startEqlBasedOnContext(final CONTEXT context, final String searchString) {
        return select(companion.getEntityType()).where().condition(createSearchByKeyAndDescCondition(searchString));
    }

    /**
     * This method may be overridden to enhance the resulting query based on the provided context.
     *
     * @param context
     * @param searchString
     * @param incompleteEql
     * @return
     */
    protected EntityResultQueryModel<T> completeEqlBasedOnContext(final CONTEXT context, final String searchString, final ICompoundCondition0<T> incompleteEql) {
        return incompleteEql.model();
    }

    /**
     * This method may be overridden to provide the param values for the
     * resulting query based on the provided context.
     *
     * @param context
     * @param params
     *            - params to fill
     */
    protected void fillParamsBasedOnContext(final CONTEXT context, final Map<String, Object> params) {
        // Do nothing here
    }

    /**
     * This method may be overridden to provide an alternative ordering if the
     * default ordering by the key is not suitable.
     *
     * @return alternative ordering model
     */
    protected OrderingModel makeOrderingModel() {
        return orderBy().prop(KEY).asc().model();
    }

    private Builder<T, EntityResultQueryModel<T>> createCommonQueryBuilderForFindMatches(final String searchString) {
        final ICompoundCondition0<T> incompleteEql = startEqlBasedOnContext(getContext(), searchString);
        final EntityResultQueryModel<T> queryModel = completeEqlBasedOnContext(getContext(), searchString, incompleteEql);
        final boolean searchModelNotOverriden =  queryModel.equals(select(companion.getEntityType()).where().condition(createSearchByKeyAndDescCondition(searchString)).model());
        queryModel.setFilterable(true);
        final OrderingModel ordering = searchModelNotOverriden ? orderBy().order(createKeyBeforeDescOrderingModel(searchString)).order(makeOrderingModel()).model() : makeOrderingModel();
        final Map<String, Object> params = new HashMap<>();
        fillParamsBasedOnContext(getContext(), params);
        return from(queryModel).with(ordering).with(defaultFetchModel).with(params).lightweight();
    }
    
    @Override
    public List<T> findMatches(final String searchString) {
        return companion.getFirstEntities(createCommonQueryBuilderForFindMatches(searchString).with(defaultFetchModel).model(), getPageSize());
    }

    @Override
    public List<T> findMatchesWithModel(final String searchString) {
        return companion.getFirstEntities(createCommonQueryBuilderForFindMatches(searchString).with(getFetch()).model(), getPageSize());
    }

    @Override
    public fetch<T> getFetch() {
        return fetchModel;
    }

    @Override
    public void setFetch(final fetch<T> fetchModel) {
        this.fetchModel = fetchModel;
    }

    @Override
    public CONTEXT getContext() {
        return context;
    }

    @Override
    public void setContext(final CONTEXT context) {
        this.context = context;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

}