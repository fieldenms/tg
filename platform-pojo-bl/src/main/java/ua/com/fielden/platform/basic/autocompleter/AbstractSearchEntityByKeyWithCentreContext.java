/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Key based value matcher, which supports entity centre context assignment.
 *
 * @author TG Team
 */
public abstract class AbstractSearchEntityByKeyWithCentreContext<T extends AbstractEntity<?>>
                      implements IValueMatcherWithCentreContext<T>, IValueMatcherWithFetch<T> {

    private final IEntityDao<T> companion;
    private final fetch<T> defaultFetchModel;
    private fetch<T> fetchModel;
    private CentreContext<T, ?> context;

    private int pageSize = 10;


    public AbstractSearchEntityByKeyWithCentreContext(final IEntityDao<T> companion) {
        this.companion = companion;
        this.defaultFetchModel = fetchKeyAndDescOnly(companion.getEntityType());
    }

    /**
     * This method needs to be implemented to enhance the resulting query based on the provided context.
     *
     * @param incompleteEql
     * @return
     */
    protected abstract EntityResultQueryModel<T> completeEqlBasedOnContext(final CentreContext<T, ?> context, final String searchString, final ICompoundCondition0<T> incompleteEql);


    @Override
    public List<T> findMatches(final String searchString) {
        final ICompoundCondition0<T> incompleteEql = select(companion.getEntityType()).where().prop(KEY).iLike().val(searchString);
        final EntityResultQueryModel<T> queryModel = completeEqlBasedOnContext(getContext(), searchString, incompleteEql);
        final OrderingModel ordering = orderBy().prop(KEY).asc().model();
        return companion.getFirstEntities(from(queryModel).with(ordering).with(defaultFetchModel).lightweight().model(), getPageSize());
    }

    @Override
    public List<T> findMatchesWithModel(final String searchString) {
        final ICompoundCondition0<T> incompleteEql = select(companion.getEntityType()).where().prop(KEY).iLike().val(searchString);
        final EntityResultQueryModel<T> queryModel = completeEqlBasedOnContext(getContext(), searchString, incompleteEql);
        final OrderingModel ordering = orderBy().prop(KEY).asc().model();
        return companion.getFirstEntities(from(queryModel).with(ordering).with(getFetch()).lightweight().model(), getPageSize());
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
    public CentreContext<T, ?> getContext() {
        return context;
    }

    @Override
    public void setContext(final CentreContext<T, ?> context) {
        this.context = context;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }


}