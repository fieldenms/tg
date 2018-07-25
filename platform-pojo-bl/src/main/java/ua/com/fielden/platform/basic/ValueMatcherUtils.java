package ua.com.fielden.platform.basic;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

public class ValueMatcherUtils {
    
    private ValueMatcherUtils() {}

    public static <T extends AbstractEntity<?>> Builder<T, EntityResultQueryModel<T>> createCommonQueryBuilderForFindMatches(
            final Class<T> entityType, 
            final ConditionModel searchCriteria,
            final OrderingModel ordering,
            final Map<String, Object> queryParams) {
        final EntityResultQueryModel<T> queryModel = (searchCriteria != null ? select(entityType).where().condition(searchCriteria).model() : select(entityType).model()).setFilterable(true);
        return from(queryModel).with(ordering).with(queryParams).lightweight();
    }

    public static ConditionModel createStrictSearchByKeyCriteriaModel(Class<? extends AbstractEntity<?>> entityType, final String searchString) {
        ConditionModel keyCriteria = cond().prop(KEY).iLike().val(searchString).model();

        if (isCompositeEntity((Class<? extends AbstractEntity<?>>) entityType)) {
            for (String propName : keyPaths(entityType)) {
                keyCriteria = cond().condition(keyCriteria).or().prop(propName).iLike().val(searchString).model();
            }
        }

        return keyCriteria;
    }

    public static ConditionModel createRelaxedSearchByKeyCriteriaModel(final String searchString) {
        return cond().prop(KEY).iLike().val("%" + searchString).model();
    }

}
