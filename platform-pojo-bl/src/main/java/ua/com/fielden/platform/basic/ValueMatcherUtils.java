package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Finder.unionProperties;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public class ValueMatcherUtils {
    private static final String ERR_UNION_TYPE_HAS_NO_ACTIVATABLE_SUBTYPE = "Union type [%s] has no activatable subtype.";

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

    public static <T extends AbstractEntity<?>> IStandAloneConditionCompoundCondition<T> createActiveOnlyCondition(final Class<T> activatableEntityOrUnionType) {
        final var activatableAndNonActivatableProps = determineActivePropertiesFrom(activatableEntityOrUnionType);
        final var activatableConditions = activatableAndNonActivatableProps.get(true).stream()
            .map(activatableProp -> EntityQueryUtils.<T>cond().prop(activatableProp + "." + ACTIVE).eq().val(true))
            .reduce((cond1, cond2) -> cond1.or().condition(cond2.model()))
            .get();
        final var maybeNonActivatableConditions = ofNullable(activatableAndNonActivatableProps.get(false))
            .flatMap(nonActivatableProps -> nonActivatableProps.stream()
                .map(nonActivatableProp -> EntityQueryUtils.<T>cond().prop(nonActivatableProp).isNotNull())
                .reduce((cond1, cond2) -> cond1.or().condition(cond2.model())
            ));
        return maybeNonActivatableConditions
            .map(nonActivatableConditions -> nonActivatableConditions.or().condition(activatableConditions.model()))
            .orElse(activatableConditions);
    }

    public static Map<Boolean, List<String>> determineActivePropertiesFrom(final Class<? extends AbstractEntity<?>> activatableEntityOrUnionType) {

        if (isActivatableEntityType(activatableEntityOrUnionType)) {
            return mapOf(t2(true, listOf(ACTIVE)));
        }

        final var unionType = (Class<? extends AbstractUnionEntity>) activatableEntityOrUnionType;
        final var activatableAndNonActivatableProps = unionProperties(unionType).stream()
            .collect(groupingBy(
                unionField -> isActivatableEntityType(unionField.getType()),
                mapping(Field::getName, toList())
            ));

        if (!activatableAndNonActivatableProps.containsKey(true)) {
            throw new ValueMatcherException(ERR_UNION_TYPE_HAS_NO_ACTIVATABLE_SUBTYPE.formatted(unionType.getSimpleName()));
        }
        return activatableAndNonActivatableProps;
    }

}
