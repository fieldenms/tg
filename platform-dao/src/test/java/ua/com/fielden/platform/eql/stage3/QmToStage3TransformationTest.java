package ua.com.fielden.platform.eql.stage3;


import org.junit.Assert;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage1.sources.exceptions.InvalidYieldMatrixException;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.sample.domain.*;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.test_utils.TestUtils.assertThrows;

public class QmToStage3TransformationTest extends EqlStage3TestCase {

    @Test
    public void common_subproperty_of_union_property_is_resolved() {
        final var actualEql = select(TgBogie.class)
                .where().prop("location.id").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(TgBogie.class)
                .where().caseWhen().prop("location.wagonSlot").isNotNull().then().prop("location.wagonSlot.id")
                        .when().prop("location.workshop").isNotNull().then().prop("location.workshop.id")
                        .end()
                        .isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void invoking_id_property_on_persistent_property_of_entity_type_does_not_generate_extra_join() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().prop("make.id").isNotNull());

        final Source3BasedOnTable model = source(MODEL, 1);
        final Conditions3 conditions = or(isNotNull(prop("make", model, LONG_PROP_TYPE)));
        final ResultQuery3 expQry = qryCountAll(sources(model), conditions);

        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void invoking_id_property_on_calculated_property_of_entity_type_does_not_generate_extra_join() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().prop("modelMake.id").isNotNull());

        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);

        final IJoinNode3 sources =
                ij(
                        veh,
                        model,
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(isNotNull(entityProp("make", model, MAKE)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void calc_props_of_component_type_are_resolved_correctly() {
        final var actualEql = select(ORG5)
                .where().anyOfProps("averageVehPrice", "averageVehPurchasePrice").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(ORG5)
                .where().model(select(TgVehicle.class).where().prop("station").eq().extProp(ID).yield().avgOf().prop("price").modelAsPrimitive()).isNotNull()
                        .or()
                        .model(select(TgVehicle.class).where().prop("station").eq().extProp(ID).yield().avgOf().prop("purchasePrice").modelAsPrimitive()).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void yielding_entity_id_under_different_alias_preserves_entity_type_info() {
        final var qry = select(TeVehicleModel.class).
                yield().prop("id").as("model").
                yield().prop("make").as("make").
                modelAsAggregate();

        final ResultQuery3 actQry = qry(qry);

        final Source3BasedOnTable source = source(MODEL, 1);

        final Yield3 modelYield = yieldId(source, "model");
        final Yield3 makeYield = yieldProp("make", source, "make", propType(MAKE, H_LONG));
        final Yields3 yields = yields(modelYield, makeYield);

        final ResultQuery3 expQry = qry(sources(source), yields);
        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void extProp_resolves_to_the_outer_query() {
        final var actualEql = select(TeVehicleModel.class)
                .yield().model(select(select(TeVehicle.class)
                                              .where().prop("model").eq().extProp("id")
                                              .yield().countAll().as("qty")
                                              .modelAsAggregate())
                                       .yield().prop("qty")
                                       .modelAsPrimitive())
                .as("qty")
                .modelAsAggregate();

        final var expectedEql = select(TeVehicleModel.class).as("q1")
                .yield().model(select(select(TeVehicle.class)
                                              .where().prop("model").eq().prop("q1.id")
                                              .yield().countAll().as("qty")
                                              .modelAsAggregate())
                                       .yield().prop("qty")
                                       .modelAsPrimitive())
                .as("qty")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_10() {
        final var actualEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.model.make.key", "vehicle.model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_11() {
        final var actualEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.modelMakeKey", "vehicle.modelMakeKeyDuplicate").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.model.make.key", "vehicle.model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }


    @Test
    public void calc_prop_is_correctly_transformed_13() {
        final var actualEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.model.key", "vehicle.model.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_12() {
        final var actualEql = select(WORK_ORDER)
                .where().anyOfProps("vehicle.modelMakeKey6").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER).as("wo")
                .leftJoin(TeVehicle.class).as("v")
                .on().prop("wo.vehicle").eq().prop("v.id")
                .where().model(select(TeVehicleModel.class).where().prop("id").eq().prop("v.model").yield().prop("makeKey2").modelAsPrimitive()).isNotNull()
                .yield().prop("wo.id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_09() {
        final var actualEql = select(WORK_ORDER)
                .where().anyOfProps("makeKey2").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("modelMakeKey4").modelAsPrimitive()).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_14() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("vehicleModel.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER).as("wo")
                .leftJoin(VEHICLE).as("v")
                .on().prop("wo.vehicle").eq().prop("v.id")
                .leftJoin(TeVehicleModel.class).as("vm")
                .on().prop("v.model").eq().prop("vm.id")
                .where().prop("vm.key").isNotNull()
                .yield().prop("wo.id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_08() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER).as("wo")
                .leftJoin(TeVehicleMake.class).as("m")
                .on().model(select(TeVehicle.class).where().prop("id").eq().prop("wo.vehicle").yield().prop("model.make").modelAsEntity(TeVehicleMake.class))
                     .eq().prop("m.id")
                .where().prop("m.key").isNotNull()
                .yield().prop("wo.id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_07() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("make").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().model(select(VEHICLE).where().prop("id").eq().extProp("vehicle").yield().prop("model.make").modelAsEntity(MAKE)).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_06() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("makeKey").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        // Expansion of the calculated property `makeKey` of `TeWorkOrder`.
        final var expectedEql = select(WORK_ORDER)
                .where().model(select(VEHICLE).where().prop("id").eq().extProp("vehicle").yield().prop("model.make.key").modelAsPrimitive()).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_04() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("vehicleModel.makeKey").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().prop("vehicleModel.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_03() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("vehicle.modelMakeKey2").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().prop("vehicle.model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void calc_prop_is_correctly_transformed_02() {
        final var actualEql = select(WORK_ORDER)
                .where().prop("vehicle.modelMakeKey").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(WORK_ORDER)
                .where().prop("vehicle.model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_08() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps("modelMakeKey2", "make.key", "model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().anyOfProps("model.make.key", "make.key", "model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_06() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps("modelMakeKey2", "make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().anyOfProps("model.make.key", "make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_04() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps("modelMakeKey2", "model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().anyOfProps("model.make.key", "model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_03() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps("modelMakeKey", "modelMakeDesc").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().anyOfProps("model.make.key", "model.make.desc").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_02() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps("modelKey", "modelDesc").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().anyOfProps("model.key", "model.desc").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_01() {
        final var actualEql = select(VEHICLE)
                .where().prop("modelMakeKey").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE)
                .where().prop("model.make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_05() {
        final var actualEql = select(MODEL)
                .where().anyOfProps("makeKey", "makeKey2", "make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        // `makeKey` expands to the path `make.key`, `makeKey2` expands to a correlated subquery.
        final var expectedEql = select(MODEL)
                .where().prop("make.key").isNotNull()
                        .or()
                        .model(select(MAKE).where().prop("id").eq().extProp("make").yield().prop("key").modelAsPrimitive()).isNotNull()
                        .or()
                        .prop("make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_04() {
        final var actualEql = select(MODEL)
                .where().anyOfProps("makeKey", "makeKey2").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(MODEL)
                .where().prop("make.key").isNotNull()
                        .or()
                        .model(select(MAKE).where().prop("id").eq().extProp("make").yield().prop("key").modelAsPrimitive()).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_03() {
        final var actualEql = select(MODEL)
                .where().anyOfProps("makeKey", "make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(MODEL)
                .where().anyOfProps("make.key", "make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_02() {
        final var actualEql = select(MODEL)
                .where().prop("makeKey2").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(MODEL)
                .where().model(select(MAKE).where().prop("id").eq().extProp("make").yield().prop("key").modelAsPrimitive()).isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_01() {
        final var actualEql = select(MODEL)
                .where().prop("makeKey").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(MODEL)
                .where().prop("make.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_01() {
        final var actualEql = select(VEHICLE)
                .where().anyOfProps(KEY, "replacedBy.key").isNotNull()
                .yield().prop("id").as("id")
                .modelAsAggregate();

        final var expectedEql = select(VEHICLE).as("v1")
                .leftJoin(VEHICLE).as("v2")
                .on().prop("v1.replacedBy").eq().prop("v2.id")
                .where().prop("v1.key").isNotNull()
                        .or()
                        .prop("v2.key").isNotNull()
                .yield().prop("v1.id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_02() {
        // This test cannot be refactored to a query-to-query comparison.
        // The implicit joins form a bushy tree: `station.name`/`station.parent.name` produce a grouped `ij(org5, org4)` subtree hanging off `veh`, alongside the separate `replacedBy` branch.
        // The fluent join API builds only left-deep trees, so no explicit-join query can reproduce this shape.
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 2);
        final Source3BasedOnTable org5 = source(ORG5, 3);
        final Source3BasedOnTable org4 = source(ORG4, 4);

        final IJoinNode3 sources =
                lj(
                        lj(
                                veh,
                                repVeh,
                                eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                          ),
                        ij(
                                org5,
                                org4,
                                eq(entityProp("parent", org5, ORG4), idProp(org4))
                          ),
                        eq(entityProp("station", veh, ORG5), idProp(org5))
                  );
        final Conditions3 conditions = or(isNotNull(dateProp("initDate", veh)), isNotNull(stringProp("name", org5)), isNotNull(stringProp("name", org4)), isNotNull(dateProp("initDate", repVeh)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_03() {
        final var actualEql = select(VEHICLE).as("veh")
                .join(ORG5).as("ou5e").on().prop("veh.station").eq().prop("ou5e.id")
                .where().anyOfProps("veh.key", "veh.replacedBy.key").isNotNull()
                .yield().prop("veh.id").as("id")
                .modelAsAggregate();

        // The implicit join for `veh.replacedBy` is written out as an explicit left join.
        final var expectedEql = select(VEHICLE).as("veh")
                .leftJoin(VEHICLE).as("rep").on().prop("veh.replacedBy").eq().prop("rep.id")
                .join(ORG5).as("ou5e").on().prop("veh.station").eq().prop("ou5e.id")
                .where().anyOfProps("veh.key", "rep.key").isNotNull()
                .yield().prop("veh.id").as("id")
                .modelAsAggregate();

        assertAlphaEq(qry(expectedEql), qry(actualEql));
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_04() {
        // This test cannot be refactored to a query-to-query comparison.
        // The implicit joins form a bushy tree with grouped subtrees hanging off `veh` (e.g. `ij(ou5, ou4)` for `station.parent.name`, `ij(ou5e, ou5eou4)` for `ou5e.parent.name`).
        // The fluent join API builds only left-deep trees, so no explicit-join query can reproduce this shape.
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).as("veh").join(ORG5).as("ou5e").on().prop("station").eq().prop("ou5e.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "ou5e.parent.name").isNotNull());

        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 3);
        final Source3BasedOnTable ou5 = source(ORG5, 4);
        final Source3BasedOnTable ou4 = source(ORG4, 5);
        final Source3BasedOnTable ou5e = source(ORG5, 2);
        final Source3BasedOnTable ou5eou4 = source(ORG4, 6);

        final IJoinNode3 sources =
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ou4,
                                        eq(entityProp("parent", ou5, ORG4), idProp(ou4))
                                  ),
                                eq(entityProp("station", veh, ORG5), idProp(ou5))
                          ),
                        ij(
                                ou5e,
                                ou5eou4,
                                eq(entityProp("parent", ou5e, ORG4), idProp(ou5eou4))
                          ),
                        eq(entityProp("station", veh, ORG5), idProp(ou5e))
                  );
        final Conditions3 conditions = or(
                isNotNull(stringProp(KEY, veh)),
                isNotNull(stringProp(KEY, repVeh)),
                isNotNull(dateProp("initDate", veh)),
                isNotNull(stringProp("name", ou5)),
                isNotNull(stringProp("name", ou4)),
                isNotNull(stringProp("name", ou5eou4)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_05() {
        // This test cannot be refactored to a query-to-query comparison.
        // The implicit joins form a bushy tree with deeply nested grouped subtrees hanging off `veh` (e.g. `ij(ou5, ij(ou4, ou3))` for the `station.parent.parent...` chain).
        // The fluent join API builds only left-deep trees, so no explicit-join query can reproduce this shape.
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).
                join(ORG2).as("ou2e").on().prop("station.parent.parent.parent").eq().prop("ou2e.id").
                where().anyOfProps("initDate", "replacedBy.initDate", "station.name", "station.parent.name", "ou2e.parent.key").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 3);
        final Source3BasedOnTable ou5 = source(ORG5, 4);
        final Source3BasedOnTable ou4 = source(ORG4, 5);
        final Source3BasedOnTable ou3 = source(ORG3, 6);
        final Source3BasedOnTable ou2e = source(ORG2, 2);
        final Source3BasedOnTable ou2eou1 = source(ORG1, 7);

        final IJoinNode3 sources =
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ij(
                                                ou4,
                                                ou3,
                                                eq(entityProp("parent", ou4, ORG3), idProp(ou3))
                                          ),
                                        eq(entityProp("parent", ou5, ORG4), idProp(ou4))
                                  ),
                                eq(entityProp("station", veh, ORG5), idProp(ou5))
                          ),
                        ij(
                                ou2e,
                                ou2eou1,
                                eq(entityProp("parent", ou2e, ORG1), idProp(ou2eou1))
                          ),
                        eq(entityProp("parent", ou3, ORG2), idProp(ou2e))
                  );
        final Conditions3 conditions = or(
                isNotNull(dateProp("initDate", veh)),
                isNotNull(dateProp("initDate", repVeh)),
                isNotNull(stringProp("name", ou5)),
                isNotNull(stringProp("name", ou4)),
                isNotNull(stringProp(KEY, ou2eou1)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertAlphaEq(expQry, actQry);
    }

    @Test
    public void caseWhen_returning_only_nulls_cannot_use_plain_end() {
        final var query = select()
                .yield().caseWhen().val(1).isNotNull().then().val(null).end().as("x")
                .modelAsAggregate();

        assertThrows(() -> qry(query), EqlException.class, ex -> {
            assertEquals(
                    "Illegal [caseWhen] expression: at least one returned value must be non-null or a type cast must be specified.",
                    ex.getMessage());
        });
    }

    @Test
    public void union_of_queries_with_different_numbers_of_yields_fails() {
        final var q1 = select().yield().val(200).as("x").modelAsAggregate();
        final var q2 = select().yield().val(100).as("x").yield().val(300).as("y").modelAsAggregate();
        final var union = select(q1, q2).yieldAll().modelAsAggregate();

        Assert.assertThrows(InvalidYieldMatrixException.class, () -> qry(union));
    }

 }
