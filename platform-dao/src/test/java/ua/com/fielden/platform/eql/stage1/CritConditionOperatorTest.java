package ua.com.fielden.platform.eql.stage1;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.eql.meta.EqlStage1TestCase;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.utils.IDates;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.buildCondition;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.createNotInitialisedQueryProperty;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

public class CritConditionOperatorTest extends EqlStage1TestCase {

    private static final String
            critProp = "fuelTypeCrit",
            persistedProp = "lastFuelUsage.fuelType.key",
            persistedPropInCollection = "fuelType.key",
            D = "D";
    private static final IWhere0<TeVehicle> select_veh_where = select(VEHICLE).where();
    private static final ICompoundCondition0<TgFuelUsage> modelStart = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID);
    private static final IDates dates = new DatesForTesting();

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
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(empty(), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
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
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void single_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(dqbBuildCondition(queryProperty, persistedProp, dates)), paramValues), //
                conditions(select_veh_where.critCondition(persistedProp, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_but_mnemonicsless() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().exists(modelStart.and().condition(dqbBuildCondition(queryProperty, persistedPropInCollection, dates)).model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), false, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = dqbBuildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, dates);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().exists(modelStart.and().condition(valueCondition).model()).//
                        or().notExists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, false);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = dqbBuildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, dates);
        assertModelsEquals(//
                conditions(select_veh_where.condition(cond().notExists(modelStart.and().condition(valueCondition).model()).//
                        or().notExists(modelStart.model()).model()), paramValues), //
                conditions(select_veh_where.critCondition(modelStart, persistedPropInCollection, critProp), paramValues));
    }

    @Test
    public void collectional_operator_generates_correct_condition_if_crit_prop_value_is_not_empty_and_negated_and_missing() {
        final QueryProperty queryProperty = getQueryProperty(of(D), true, true);
        final Map<String, Object> paramValues = getParams(critProp, queryProperty);
        final ConditionModel valueCondition = dqbBuildCondition(getQueryProperty(of(D), false, false), persistedPropInCollection, dates);
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

    @Test
    public void single_critCondition_on_union_typed_property_and_union_typed_crit() {
        final var locationCrit = createNotInitialisedQueryProperty(TgBogie.class, TgBogie.LOCATION_CRIT);
        locationCrit.setValue(List.of("x"));
        assertModelsEquals(
                conditions(select(TgBogie.class).where().condition(
                        cond()
                        .prop(TgBogie.LOCATION).isNotNull()
                        .and().condition(cond().prop(TgBogie.LOCATION).in().model(select(TgBogieLocation.class).where().prop(KEY).in().values("x").model()).model())
                        .model())),
                conditions(select(TgBogie.class).where().critCondition(TgBogie.LOCATION, TgBogie.LOCATION_CRIT),
                           getParams(TgBogie.LOCATION_CRIT, locationCrit)));
    }

    @Test
    public void single_critCondition_on_key_of_union_typed_property_and_union_typed_crit() {
        final var locationCrit = createNotInitialisedQueryProperty(TgBogie.class, TgBogie.LOCATION_CRIT);
        locationCrit.setValue(List.of("x"));
        assertModelsEquals(
                conditions(select(TgBogie.class).where().condition(
                        cond()
                        .prop(TgBogie.LOCATION).isNotNull()
                        .and().condition(cond().prop(TgBogie.LOCATION).in().model(select(TgBogieLocation.class).where().prop(KEY).in().values("x").model()).model())
                        .model())),
                conditions(select(TgBogie.class).where().critCondition(TgBogie.LOCATION + "." + KEY, TgBogie.LOCATION_CRIT),
                           getParams(TgBogie.LOCATION_CRIT, locationCrit)));
    }

    @Test
    public void single_critCondition_on_key_and_union_typed_crit() {
        final var locationCrit = createNotInitialisedQueryProperty(TgBogie.class, TgBogie.LOCATION_CRIT);
        locationCrit.setValue(List.of("x"));
        assertModelsEquals(
                conditions(select(TgWorkshop.class).where().condition(
                        cond()
                        .prop(ID).isNotNull()
                        .and().condition(cond().prop(ID).in().model(select(TgBogieLocation.class).where().prop(KEY).in().values("x").model()).model())
                        .model())),
                conditions(select(TgWorkshop.class).where().critCondition(KEY, TgBogie.LOCATION_CRIT),
                           getParams(TgBogie.LOCATION_CRIT, locationCrit)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Helpers
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static Map<String, Object> getParams(final String critPropName, final QueryProperty queryProperty) {
        return mapOf(t2(queryPropertyParamName(critPropName), queryProperty));
    }

    private static QueryProperty getQueryProperty(final Optional<String> value, final boolean negated, final boolean missing) {
        final QueryProperty queryProperty = createNotInitialisedQueryProperty(VEHICLE, critProp);
        queryProperty.setValue(value.map(v -> asList(v)).orElse(emptyList()));
        queryProperty.setNot(negated);
        queryProperty.setOrNull(missing);
        return queryProperty;
    }
    
    /// A helper function to remove confusion that has to do with passing `false` as the last argument value for calls to [DynamicQueryBuilder#buildCondition(QueryProperty, String, boolean, IDates)].
    ///
    private static <ET extends AbstractEntity<?>> ConditionModel dqbBuildCondition(final QueryProperty property, final String propertyName, final IDates dates) {
        return buildCondition(property, propertyName, false, dates);
    }

    private static Conditions1 conditions(final ICompoundCondition0<?> condition) {
        return resultQry(condition.model()).whereConditions;
    }

    private static Conditions1 conditions(final ICompoundCondition0<?> condition, final Map<String, Object> paramValues) {
        return resultQry(condition.model(), paramValues).whereConditions;
    }
   
    protected static void assertModelsEquals(final Conditions1 exp, final Conditions1 act) {
        assertEquals(("Condition models are different! exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

}
