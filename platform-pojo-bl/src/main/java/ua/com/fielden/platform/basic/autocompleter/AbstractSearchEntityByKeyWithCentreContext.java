/**
 *
 */
package ua.com.fielden.platform.basic.autocompleter;

import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.basic.ValueMatcherUtils.createCommonQueryBuilderForFindMatches;
import static ua.com.fielden.platform.basic.ValueMatcherUtils.createRelaxedSearchByKeyCriteriaModel;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.IValueMatcherWithFetch;
import ua.com.fielden.platform.basic.ValueMatcherUtils;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Key based value matcher, which supports entity centre context assignment.
 *
 * @author TG Team
 */
public abstract class AbstractSearchEntityByKeyWithCentreContext<T extends AbstractEntity<?>> implements IValueMatcherWithCentreContext<T>, IValueMatcherWithFetch<T> {

    private static final Supplier<? extends EntityCentreExecutionException> CO_MISSING_EXCEPTION_SUPPLIER = () -> new EntityCentreExecutionException("A companion is massing to perform this operation.");

    private final Optional<IEntityDao<T>> maybeCompanion;
    private final fetch<T> defaultFetchModel;
    private fetch<T> fetchModel;
    private CentreContext<T, ?> context;

    public AbstractSearchEntityByKeyWithCentreContext(final IEntityDao<T> companion) {
        this.maybeCompanion = Optional.ofNullable(companion);
        this.defaultFetchModel = maybeCompanion.map(co -> fetchKeyAndDescOnly(co.getEntityType())).orElse(null);
    }

    /**
     * This method may be overridden to create a different EQL condition model for search criteria.
     *
     * @param context
     * @param searchString
     * @return
     */
    protected ConditionModel makeSearchCriteriaModel(final CentreContext<T, ?> context, final String searchString) {
        if ("%".equals(searchString)) {
            return cond().val(1).eq().val(1).model();
        }

        final ConditionModel keyCriteria = createRelaxedSearchByKeyCriteriaModel(searchString);
        final Class<T> entityType = maybeCompanion.orElseThrow(CO_MISSING_EXCEPTION_SUPPLIER).getEntityType();

        return hasDescProperty(entityType) ? cond().condition(keyCriteria).or().prop(AbstractEntity.DESC).iLike().val("%" + searchString).model() : keyCriteria;
    }

    /**
     * This method may be overridden to provide the param values for the resulting query based on the provided context.
     *
     * @param context
     */
    protected Map<String, Object> fillParamsBasedOnContext(final CentreContext<T, ?> context) {
        return emptyMap();
    }

    /**
     * This method may be overridden to provide an alternative ordering if the default ordering by the key is not suitable.
     *
     * @return alternative ordering model
     */
    protected OrderingModel makeOrderingModel(final String searchString) {
        return orderBy().prop(KEY).asc().model();
    }

    private OrderingModel composeOrderingModelForQuery(final String searchString, final Class<T> entityType) {
        return "%".equals(searchString) ? makeOrderingModel(searchString)
                : orderBy().expr(makeSearchResultOrderingPriority(entityType, searchString)).asc().order(makeOrderingModel(searchString)).model();
    }

    @Override
    public List<T> findMatches(final String searchString) {
        final IEntityDao<T> companion = maybeCompanion.orElseThrow(CO_MISSING_EXCEPTION_SUPPLIER);
        final ConditionModel searchCriteria = makeSearchCriteriaModel(getContext(), searchString);
        final OrderingModel ordering = composeOrderingModelForQuery(searchString, companion.getEntityType());
        final Map<String, Object> queryParams = fillParamsBasedOnContext(getContext());
        return companion.getFirstEntities(createCommonQueryBuilderForFindMatches(companion.getEntityType(), searchCriteria, ordering, queryParams).with(defaultFetchModel).model(), getPageSize());
    }

    @Override
    public List<T> findMatchesWithModel(final String searchString) {
        final IEntityDao<T> companion = maybeCompanion.orElseThrow(CO_MISSING_EXCEPTION_SUPPLIER);
        final ConditionModel searchCriteria = makeSearchCriteriaModel(getContext(), searchString);
        final OrderingModel ordering = composeOrderingModelForQuery(searchString, companion.getEntityType());
        final Map<String, Object> queryParams = fillParamsBasedOnContext(getContext());
        return companion.getFirstEntities(createCommonQueryBuilderForFindMatches(companion.getEntityType(), searchCriteria, ordering, queryParams).with(getFetch()).model(), getPageSize());
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
    public AbstractSearchEntityByKeyWithCentreContext<T> setContext(final CentreContext<T, ?> context) {
        this.context = context;
        return this;
    }

}