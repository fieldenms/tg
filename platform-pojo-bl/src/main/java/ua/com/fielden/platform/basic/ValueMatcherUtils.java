package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

import java.util.Map;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Finder.unionProperties;
import static ua.com.fielden.platform.utils.EntityUtils.*;

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

    public static ConditionModel createStrictSearchByKeyCriteriaModel(final Class<? extends AbstractEntity<?>> entityType, final String searchString) {
        ConditionModel keyCriteria = cond().prop(KEY).iLikeWithCast().val(searchString).model();

        if (isCompositeEntity((Class<? extends AbstractEntity<?>>) entityType)) {
            for (final String propName : keyPaths(entityType)) {
                keyCriteria = cond().condition(keyCriteria).or().prop(propName).iLikeWithCast().val(searchString).model();
            }
        }

        return keyCriteria;
    }

    public static ConditionModel createRelaxedSearchByKeyCriteriaModel(final String searchString) {
        return cond().prop(KEY).iLikeWithCast().val(searchString).model();
    }

    /// Creates condition to query active only values for a type.
    /// For union types, it includes also all non-activatable subtype values, as they are treated as active.
    ///
    /// @param entityType activatable type or union type with at least one activatable subtype
    ///
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> IStandAloneConditionCompoundCondition<T> createActiveOnlyCondition(final Class<T> entityType) {
        if (isActivatableEntityType(entityType)) {
            return EntityQueryUtils.<T>cond().prop(ACTIVE).eq().val(true);
        }
        else if (isUnionEntityType(entityType)) {
            return (IStandAloneConditionCompoundCondition<T>)
                    unionProperties((Class<? extends AbstractUnionEntity>) entityType)
                            .stream()
                            .map(memberField -> isActivatableEntityType(memberField.getType())
                                    ? cond().prop(memberField.getName() + "." + ACTIVE).eq().val(true)
                                    : cond().prop(memberField.getName()).isNotNull())
                            .reduce((cond1, cond2) -> cond1.or().condition(cond2.model()))
                            .orElseGet(() -> cond().iVal(null).eq().iVal(null));
        }
        else {
            return EntityQueryUtils.<T>cond().iVal(null).eq().iVal(null);
        }
    }

}
