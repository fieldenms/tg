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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Finder.unionProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.PROPERTY_SPLITTER;
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

    /// Creates condition to query active only values for a type.
    /// For union types, it includes also all non-activatable subtype values, as they are treated as active.
    ///
    /// @param activatableEntityOrUnionType activatable type or union type with at least one activatable subtype
    ///
    public static <T extends AbstractEntity<?>> IStandAloneConditionCompoundCondition<T> createActiveOnlyCondition(final Class<T> activatableEntityOrUnionType) {
        final var activatableAndNonActivatableProps = determineActivatableAndOtherwisePropertiesFrom(activatableEntityOrUnionType);
        // Activatable properties must exist here, i.e. activatableAndNonActivatableProps.get(true) != null.
        // Create a combined condition from all these properties and get() the result from Optional as it will be present.
        final var activatableConditions = activatableAndNonActivatableProps.get(true).stream()
            .map(activatableProp -> EntityQueryUtils.<T>cond().prop(activePropFrom(activatableProp)).eq().val(true))
            .reduce((cond1, cond2) -> cond1.or().condition(cond2.model()))
            .get();
        // Non-activatable properties (if present) must be included too -- use isNotNull() condition for each property.
        final var maybeNonActivatableConditions = ofNullable(activatableAndNonActivatableProps.get(false))
            .flatMap(nonActivatableProps -> nonActivatableProps.stream()
                .map(nonActivatableProp -> EntityQueryUtils.<T>cond().prop(nonActivatableProp).isNotNull())
                .reduce((cond1, cond2) -> cond1.or().condition(cond2.model())
            ));
        // Combine both categories of conditions.
        return maybeNonActivatableConditions
            .map(nonActivatableConditions -> nonActivatableConditions.or().condition(activatableConditions.model()))
            .orElse(activatableConditions);
    }

    /// Determines "active" sub-property from activatable property.
    /// Takes into account "" activatable property (aka "this" / "entity itself").
    ///
    public static String activePropFrom(final String activatableProp) {
        return (isBlank(activatableProp) ? "" : activatableProp + PROPERTY_SPLITTER) + ACTIVE;
    }

    /// Returns two categories of properties from activatable or union with activatable type (`activatableEntityOrUnionType`).
    /// First category contains activatable properties.
    /// Second category contains non-activatable properties.
    ///
    /// If the type is activatable then returns "" (aka "this" / "entity itself") property only (in [true]-keyed value).
    /// If the type is union with at least one activatable, returns activatable sub-properties in [true]-keyed value,
    ///   and non-activatable sub-properties, if any, in [false]-keyed value.
    /// If the type is union with no activatable sub-properties, throws [ValueMatcherException].
    ///
    /// @param activatableEntityOrUnionType activatable type or union type with at least one activatable subtype
    ///
    public static Map<Boolean, List<String>> determineActivatableAndOtherwisePropertiesFrom(final Class<? extends AbstractEntity<?>> activatableEntityOrUnionType) {
        // In case of activatable type return "entity itself" property.
        // Activatable type can't be union and vice versa.
        if (isActivatableEntityType(activatableEntityOrUnionType)) {
            return mapOf(t2(true, listOf("")));
        }

        // In case of union type, categorise its `unionProperties` by `isActivatableEntityType`.
        final var unionType = (Class<? extends AbstractUnionEntity>) activatableEntityOrUnionType;
        final var activatableAndNonActivatableProps = unionProperties(unionType).stream()
            .collect(groupingBy(
                unionField -> isActivatableEntityType(unionField.getType()),
                mapping(Field::getName, toList())
            ));

        // Ensure only union with some activatable subtype[s] is used here.
        if (!activatableAndNonActivatableProps.containsKey(true)) {
            throw new ValueMatcherException(ERR_UNION_TYPE_HAS_NO_ACTIVATABLE_SUBTYPE.formatted(unionType.getSimpleName()));
        }
        return activatableAndNonActivatableProps;
    }

}
