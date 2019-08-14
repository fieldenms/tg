package ua.com.fielden.platform.entity.query;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.emptyCondition;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.buildCondition;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.createNotInitialisedQueryProperty;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryCompositionTCase;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;

public class CritConditionOperatorTest extends BaseEntQueryCompositionTCase {
    private static final IWhere0<TgVehicle> select_veh_where = select(VEHICLE).where();

    private static final String critProp = "fuelTypeCrit";
    private static final String persistedProp = "lastFuelUsage.fuelType.key";
    private static final String persistedPropInCollection = "fuelType.key";
    private static final String D = "D";
    private static final ICompoundCondition0<TgFuelUsage> modelStart = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID);

    private static Map<String, Object> getParams(final String critPropName, final QueryProperty queryProperty) {
        return mapOf(t2(DynamicQueryBuilder.QueryProperty.queryPropertyParamName(critProp), queryProperty));
    }

    private static QueryProperty getQueryProperty(final Optional<String> value, final boolean negated, final boolean missing) {
        final QueryProperty queryProperty = createNotInitialisedQueryProperty(VEHICLE, critProp);
        queryProperty.setValue(value.map(v -> asList(v)).orElse(emptyList()));
        queryProperty.setNot(negated);
        queryProperty.setOrNull(missing);
        return queryProperty;
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_param_not_available() {
        assertModelsEquals(//
                conditions(select_veh_where.condition(emptyCondition())), //
                conditions(select_veh_where.critCondition(persistedProp, critProp)));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_mnemonicsless() {
        final QueryProperty queryProperty = getQueryProperty(empty(), false, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(emptyCondition()), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(empty(), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(empty(), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(empty(), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                    conditions(select_veh_where.condition(emptyCondition()), paramValues), //
                    conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_but_mnemonicsless() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(buildCondition(queryProperty, persistedProp, false)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_but_mnemonicsless() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().exists(modelStart.and().condition(buildCondition(queryProperty, persistedPropInCollection, false)).model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = buildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, false);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().exists(modelStart.and().condition(valueCondition).model()).//
                        or().notExists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = buildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, false);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().notExists(modelStart.and().condition(valueCondition).model()).//
                        or().notExists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = buildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, false);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().notExists(modelStart.and().condition(valueCondition).model()).//
                        and().exists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(empty(), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().exists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(empty(), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().notExists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(empty(), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(emptyCondition()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_mnemonicsless() {
        final QueryProperty queryProperty = getQueryProperty(empty(), false, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(emptyCondition()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_param_not_available() {
        assertModelsEquals(//
                conditions(select_veh_where.condition(emptyCondition())), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp)));
    }
}