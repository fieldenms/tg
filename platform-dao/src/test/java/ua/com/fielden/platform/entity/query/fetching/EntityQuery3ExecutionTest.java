package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.dao.EntityWithTaxMoneyDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

public class EntityQuery3ExecutionTest extends AbstractDaoTestCase {
    private final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    @Test
    public void local_id_only_query_returns_entities_with_ID_present() {
        final var query = select(TgVehicle.class).model();
        final var entities = co(TgVehicle.class).getAllEntities(from(query).with(fetchIdOnly(TgVehicle.class)).model());
        assertFalse(entities.isEmpty());
        entities.forEach(entity -> assertNotNull(entity.getId()));
    }

    @Test
    public void foreign_id_only_query_returns_entities_using_the_specified_fetch_model() {
        final var query = select(TgVehicle.class).yield().prop("model").modelAsEntity(TgVehicleModel.class);
        final var entities = co(TgVehicleModel.class).getAllEntities(from(query).with(fetchAllInclCalc(TgVehicleModel.class)).model());
        assertThat(entities)
                .isNotEmpty()
                .allSatisfy(model -> assertThat(model.proxiedPropertyNames()).isEmpty());
    }

    @Test
    public void foreign_id_only_query_with_id_only_fetch_returns_entities_with_ID_present() {
        final var query = select(TgVehicle.class).yield().prop("model").modelAsEntity(TgVehicleModel.class);
        final var entities = co(TgVehicleModel.class).getAllEntities(from(query).with(fetchIdOnly(TgVehicleModel.class)).model());
        assertFalse(entities.isEmpty());
        entities.forEach(entity -> assertNotNull(entity.getId()));
    }

    @Test
    public void eql3_query_executes_correctly_for_vehicle_deep_tree() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("model").as("model").
                yield().prop("model.id").as("model.id").
                yield().prop("model.version").as("model.version").
                yield().prop("model.key").as("model.key").
                yield().prop("model.desc").as("model.desc").
                yield().prop("model.make").as("model.make").
                yield().prop("model.make.id").as("model.make.id").
                yield().prop("model.make.version").as("model.make.version").
                yield().prop("model.make.key").as("model.make.key").
                yield().prop("model.make.desc").as("model.make.desc").
                modelAsEntity(TeVehicle.class);

        getInstance(ITeVehicle.class).getAllEntities(from(qry).with("EQL3", null).model());
    }

    @Test
    public void eql3_query_executes_correctly_for_vehicle_deep_tree_without_headers() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("model.id").as("model.id").
                yield().prop("model.version").as("model.version").
                yield().prop("model.key").as("model.key").
                yield().prop("model.desc").as("model.desc").
                yield().prop("model.make.id").as("model.make.id").
                yield().prop("model.make.version").as("model.make.version").
                yield().prop("model.make.key").as("model.make.key").
                yield().prop("model.make.desc").as("model.make.desc").
                modelAsEntity(TeVehicle.class);

        getInstance(ITeVehicle.class).getAllEntities(from(qry).with("EQL3", null).model());
    }

    @Test
    public void test_complex_money_yielding_() {
        final EntityResultQueryModel<EntityWithTaxMoney> qry = select(EntityWithTaxMoney.class).model();
        getInstance(EntityWithTaxMoneyDao.class).getAllEntities(from(qry).model());
    }

    @Test
    public void test_complex_money_yielding2_() {
        final EntityResultQueryModel<EntityWithTaxMoney> qry = select(EntityWithTaxMoney.class).
                yield().prop("money.amount").as("money.amount").
                yield().prop("money.taxAmount").as("money.taxAmount").
                yield().prop("money.currency").as("money.currency").
                modelAsEntity(EntityWithTaxMoney.class);
        getInstance(EntityWithTaxMoneyDao.class).getAllEntities(from(qry).model());
    }

    @Test
    public void test_complex_money_yielding3_() {
        final EntityResultQueryModel<EntityWithTaxMoney> qry = select().
                yield().val(new BigDecimal("100.6")).as("money.amount").
                yield().val(new BigDecimal("20.2")).as("money.taxAmount").
                yield().val(Currency.getAvailableCurrencies().iterator().next()).as("money.currency").
                modelAsEntity(EntityWithTaxMoney.class);
        getInstance(EntityWithTaxMoneyDao.class).getAllEntities(from(qry).model());
    }

    private List<EntityAggregates> run(final AggregatedResultQueryModel qry) {
        return aggregateDao.getAllEntities(from(qry).with("EQL3", null).model());
    }

    private void run(final ICompoundCondition0<? extends AbstractEntity<?>> qryStart) {
        run(qryStart.yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").leftJoin(TeVehicle.class).as("rbv").
                on().prop("veh.replacedBy").eq().prop("rbv.id")./*or().prop("veh.replacedBy").ne().prop("rbv.id").*/
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("rbv.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("rbv.key").then().prop("veh.key").otherwise().prop("rbv.key").endAsStr(5).as("cwts").
                modelAsAggregate();
        run(qry);
    }

    @Test
    public void eql3_query_executes_correctly2() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();

        run(qry);
    }

    @Test
    public void eql3_query_executes_correctly3() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.key").as("vehiclekey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();

        run(qry);
    }

    @Test
    public void eql3_query_executes_correctly4() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.key").as("vehicleKey").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();

        final AggregatedResultQueryModel qry2 = select(qry).yield().prop("vehicleKey").as("vk").modelAsAggregate();

        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly5() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                join(TgOrgUnit1.class).as("st").on().prop("station.parent.parent.parent.parent").eq().prop("st.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "st.key", "st.id", "model.make.key", "replacedBy.model.make.key").isNotNull().
                yield().prop("veh.replacedBy").as("vehicle").
                yield().prop("veh.replacedBy.key").as("replacedByVehiclekey").
                yield().caseWhen().prop("veh.key").eq().prop("veh.replacedBy.key").then().prop("veh.key").otherwise().prop("veh.replacedBy.key").endAsStr(5).as("cwts").
                modelAsAggregate();

        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle").isNotNull().yield().prop("vehicle.key").as("vk").modelAsAggregate();

        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly6() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.replacedBy").as("vehicle").
                modelAsAggregate();

        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle").isNotNull().yield().prop("vehicle.key").as("vk").modelAsAggregate();

        run(qry2);
    }

    @Test
    public void eql3_query_executes_correctly7() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).as("veh").
                where().prop("veh.replacedBy").isNotNull().and().notExists(
                        select(TeVehicle.class).where().prop("replacedBy").eq().extProp("veh.id").model()).
                yield().prop("veh.replacedBy").as("vehicle").
                modelAsAggregate();

        final AggregatedResultQueryModel qry2 = select(qry).where().prop("vehicle.station.name").isNull().yield().prop("vehicle").as("vk").modelAsAggregate();

        final AggregatedResultQueryModel qry3 = select(qry2).where().prop("vk.model").isNotNull().yield().prop("vk.model.make.key").as("makeKey").modelAsAggregate();

        run(qry3);
    }

    @Test
    public void eql3_query_executes_correctly8() {
        run(select(TeWorkOrder.class).where().prop("makeKey").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly9() {
        run(select(TeWorkOrder.class).where().prop("make.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly10() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly11() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly12() {
        run(select(TeWorkOrder.class).where().anyOfProps("model.makeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly13() {
        run(select(TeWorkOrder.class).where().anyOfProps("makeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly14() {
        run(select(TeVehicle.class).where().anyOfProps("calcModel").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly15() {
        run(select(TeVehicleMake.class).where().anyOfProps("c7").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly16() {
        run(select(TeVehicleMake.class).where().anyOfProps("c6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly17() {
        run(select(TeVehicleMake.class).where().anyOfProps("c3").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly18() {
        run(select(TeVehicleMake.class).where().anyOfProps("c8").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly19() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly20() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly21() {
        run(select(TeVehicle.class).where().anyOfProps("modelKey", "modelDesc").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly22() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey2", "make.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly23() {
        run(select(TeVehicle.class).where().anyOfProps("replacedBy.modelMakeKey2", "modelMakeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly24() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey2").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly25() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey3").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly26() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey4").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly27() {
        run(select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly28() {
        run(select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key",
                "replacedBy.modelKey",
                "replacedBy.modelKey2",
                "replacedBy.modelDesc",
                "replacedBy.stationName",
                "replacedBy.modelMakeDesc",
                "replacedBy.modelMake",
                "replacedBy.modelMake2",
                "replacedBy.modelMakeKey",
                "replacedBy.modelMakeKey2",
                "replacedBy.modelMakeKey3",
                "replacedBy.modelMakeKey4",
                "replacedBy.modelMakeKey5",
                "replacedBy.modelMakeKey6",
                "replacedBy.modelMakeKey7",
                "replacedBy.modelMakeKey8",
                "replacedBy.make.key"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly29() {
        run(select(TeVehicle.class).where().anyOfProps(
                "modelKey",
                "modelKey2",
                "modelDesc",
                "stationName",
                "modelMakeDesc",
                "modelMake",
                "modelMake2",
                "modelMakeKey",
                "modelMakeKey2",
                "modelMakeKey3",
                "modelMakeKey4",
                "modelMakeKey5",
                "modelMakeKey6",
                "modelMakeKey7",
                "modelMakeKey8",
                "make.key",
                "replacedBy.modelKey",
                "replacedBy.modelKey2",
                "replacedBy.modelDesc",
                "replacedBy.stationName",
                "replacedBy.modelMakeDesc",
                "replacedBy.modelMake",
                "replacedBy.modelMake2",
                "replacedBy.modelMakeKey",
                "replacedBy.modelMakeKey2",
                "replacedBy.modelMakeKey3",
                "replacedBy.modelMakeKey4",
                "replacedBy.modelMakeKey5",
                "replacedBy.modelMakeKey6",
                "replacedBy.modelMakeKey7",
                "replacedBy.modelMakeKey8",
                "replacedBy.make.key",
                "replacedBy.replacedBy.modelKey",
                "replacedBy.replacedBy.modelKey2",
                "replacedBy.replacedBy.modelDesc",
                "replacedBy.replacedBy.stationName",
                "replacedBy.replacedBy.modelMakeDesc",
                "replacedBy.replacedBy.modelMake",
                "replacedBy.replacedBy.modelMake2",
                "replacedBy.replacedBy.modelMakeKey",
                "replacedBy.replacedBy.modelMakeKey2",
                "replacedBy.replacedBy.modelMakeKey3",
                "replacedBy.replacedBy.modelMakeKey4",
                "replacedBy.replacedBy.modelMakeKey5",
                "replacedBy.replacedBy.modelMakeKey6",
                "replacedBy.replacedBy.modelMakeKey7",
                "replacedBy.replacedBy.modelMakeKey8",
                "replacedBy.replacedBy.make.key"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly30() {
        run(select(TeVehicle.class).where().anyOfProps(
                "lastFuelUsage",
                "lastFuelUsageQty",
                "calc2",
                "calc3",
                "calc4",
                "replacedBy.calc2",
                "replacedBy.calc3",
                "replacedBy.calc4",
                "replacedBy.lastFuelUsage",
                "replacedBy.lastFuelUsageQty"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly31() {
        run(select(TeVehicle.class).where().anyOfProps(
                "lastFuelUsageQty"
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly32() {
        run(select(TeVehicle.class).where().expr(
                expr().model(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model()
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly33() {
        run(select(TeVehicle.class).where().expr(
                expr().model(select(TeVehicleFuelUsage.class).as("fu").where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).and().prop("qty").isNotNull().yield().prop("fu.qty").modelAsPrimitive()).model()
                ).isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly34() {
        run(select(TgOrgUnit1.class).where().exists( //
                select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit3.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit4.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit5.class).where().prop("parent").eq().extProp("id").and().prop("name").isNotNull(). //
                        model()). //
                        model()). //
                        model()). //
                        model()). //
                and().prop("id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly35() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelMakeKey6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly36() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly37() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicleReplacedBy.replacedBy.modelMake2.key", "vehicle.replacedBy.modelMake2.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly38() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).where().prop("model.make.key").isNotNull().model();

        run(select(qry).where().anyOfProps("id", "modelMake").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly39() {
        final AggregatedResultQueryModel qry = select(TeWorkOrder.class).where().prop("actCost.amount").isNotNull().yield().prop("vehicle").as("vh").modelAsAggregate();

        run(select(qry).where().prop("vh.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly40() {
        final AggregatedResultQueryModel qry1 = select(TeWorkOrder.class).where().prop("actCost.amount").isNotNull().yield().prop("vehicle").as("vh").modelAsAggregate();
        final AggregatedResultQueryModel qry2 = select(TeWorkOrder.class).where().prop("estCost.amount").isNotNull().yield().prop("vehicle").as("vh").modelAsAggregate();

        run(select(qry1, qry2).where().prop("vh.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    @Ignore //TODO EQL3+
    public void eql3_query_executes_correctly41() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeWorkOrder.class).yield().prop("vehicle").modelAsEntity(TeVehicle.class);

        run(select(qry).where().prop("lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
        run(select(qry).where().prop("key").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly41_2() {
        final EntityResultQueryModel<TeVehicle> qry = select(TeWorkOrder.class).yield().prop("vehicle").modelAsEntity(TeVehicle.class);

        run(select(TeVehicle.class).where().prop("replacedBy").in().model(qry).yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly42() {
        run(select(TeWorkOrder.class).where().prop("vehicle.modelMakeKey6").eq().prop("makeKey"));
    }

    @Test
    public void eql3_query_executes_correctly43() {
        run(select(TeWorkOrder.class).where().prop("vehicle.modelMakeKey6").eq().iVal(null));
    }

    @Test
    @Ignore //TODO EQL3+
    public void eql3_query_executes_correctly44() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).yield().prop("id").as("vehicle").modelAsAggregate();

        run(select(qry).where().prop("vehicle.lastFuelUsageQty").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly45() {
        run(select(TeVehicleFuelUsage.class).where().prop("key").isNull().or().prop("key").eq().val("HOH").or().prop("qty").gt().val(0).or().prop("vehicle.active").eq().val(true));
    }

    @Test
    public void eql3_query_executes_correctly46() {
        run(select(TeVehicleFuelUsage.class).where().exists(select(TeVehicle.class).where().prop("key").eq().val("A101").model()));
    }

    @Test
    public void eql3_query_executes_correctly47() {
        //
        final AggregatedResultQueryModel qry1 = select().yield().expr(intValue(1)).as("position").yield().expr(intValue(100)).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry2 = select().yield().expr(intValue(2)).as("position").yield().expr(intValue(200)).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry3 = select().yield().expr(intValue(3)).as("position").yield().expr(intValue(300)).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry4 = select().yield().expr(intValue(4)).as("position").yield().expr(intValue(400)).as("value").modelAsAggregate();
        final AggregatedResultQueryModel qry5 = select().yield().expr(intValue(5)).as("position").yield().expr(intValue(500)).as("value").modelAsAggregate();

        final List<EntityAggregates> result = run(select(qry1, qry2, qry3, qry4, qry5).where().prop("position").gt().val(2).yield().sumOf().prop("value").as("QTY").modelAsAggregate());
        assertEquals("1200", result.get(0).get("QTY").toString());
    }

    //wrapping into CASEWHEN to make H2 happy with value type
    private static ExpressionModel intValue(final int value) {
        return EntityQueryUtils.expr().caseWhen().val(0).isNotNull().then().val(value).endAsInt().model();
    }

    @Test
    public void eql3_query_executes_correctly48() {
        run(select(TeVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly49() {
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        final List<TeVehicleModel> models = getInstance(ITeVehicleModel.class).getAllEntities(from(qry).with(fetchAll(TeVehicleModel.class).with("makeKey")).with("EQL3", null).model());
        for (final TeVehicleModel item : models) {
            System.out.println(item.getId() + " : " + item.getKey() + " : " + item.getMake() + " : " + item.getMakeKey());
        }
    }

    @Test
    public void eql3_query_executes_correctly50() {
        run(select(TeVehicleModel.class).where().prop("make.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly51() {
        run(select(TeVehicle.class).where().prop("model.make.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly52() {
        run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage1").on().prop("lastFuelUsage").eq().prop("lastFuelUsage1.id").where().prop("lastFuelUsage.qty").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly53() {
        run(select(TeVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly54() {
        run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage1").on().prop("lastFuelUsage").eq().prop("lastFuelUsage1.id").where().prop("lastFuelUsage1.qty").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly55() {
        try {
            run(select(TeVehicle.class).leftJoin(TeVehicleFuelUsage.class).as("lastFuelUsage").on().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").where().prop("lastFuelUsage.qty").gt().val(100));
            fail("Should have failed while trying to resolve property [lastFuelUsage.qty]");
        } catch (final Exception e) {
        }
    }

    @Test
    @Ignore // h2 doesn't allow correlated subqueries in FROM stmt
    public void eql3_query_executes_correctly56() {

        final PrimitiveResultQueryModel qtyQry = select(select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate()).yield().prop("qty").modelAsPrimitive();
        //final PrimitiveResultQueryModel qtyQry = select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().modelAsPrimitive();
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().prop("key").as("key").yield().model(qtyQry).as("qty").modelAsAggregate();

        run(qry);
    }

    @Test
    public void eql3_query_executes_correctly57() {
        run(select(TeAverageFuelUsage.class).where().prop("key.modelMakeKey6").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly58() {
        final EntityResultQueryModel<TeAverageFuelUsage> qry = select(TeAverageFuelUsage.class).model();
        final List<TeAverageFuelUsage> models = getInstance(ITeAverageFuelUsage.class).getAllEntities(from(qry).with(fetchAll(TeAverageFuelUsage.class)).with("EQL3", null).model());
        for (final TeAverageFuelUsage item : models) {
            System.out.println(item.getId() + " : " + item.getKey() + " : " + item.getQty());
        }
    }

    @Test
    public void eql3_query_executes_correctly59() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TeVehicleModel.class).with("makeKey2")).model());
    }

    @Test
    public void eql3_query_executes_correctly60() {
        final ITgEntityWithComplexSummaries co = getInstance(ITgEntityWithComplexSummaries.class);
        final EntityResultQueryModel<TgEntityWithComplexSummaries> qry = select(TgEntityWithComplexSummaries.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).model());
    }

    @Test
    public void eql3_query_executes_correctly61() {
        final TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesCo co = co(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class);
        final EntityResultQueryModel<TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries> qry = select(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchIdOnly(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class).without("id").with("costPerKm")).model());
    }

    @Test
    public void eql3_query_executes_correctly62() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).model();
        qry.setFilterable(true);
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TeVehicleModel.class).with("makeKey2")).model());
    }

    @Test
    public void eql3_query_executes_correctly63() {
        final List<EntityAggregates> result = run(select(TgVehicle.class).where().prop("key").eq().val("CAR1").yield().prop("active").as("active").modelAsAggregate());
        assertEquals(true, result.get(0).get("active"));
    }

    @Test
    public void eql3_query_executes_correctly64() {
        run(select(TgVehicle.class).where().prop("finDetails.capitalWorksNo").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly65() {
        run(select(TgBogie.class).where().anyOfProps("location.workshop.key", "location.wagonSlot.wagon", "location.wagonSlot.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly66() {
        run(select(TgBogie.class).where().anyOfProps("location.key", "location.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly67() {
        run(select(TgWagonSlot.class).where().anyOfProps("bogie.location.key", "bogie.location.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly68() {
        run(select(TgWagon.class).where().anyOfProps("firstSlot.bogie.location.key", "firstSlot.bogie.location.id").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly69() {
        run(select(TgWagon.class).where().anyOfProps("firstSlot.bogie.location.workshop.key", "firstSlot.bogie.location.wagonSlot.wagon", "firstSlot.bogie.location.wagonSlot.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly70() {
        final ITgBogie co = getInstance(ITgBogie.class);
        final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
    }

    @Test
    public void eql3_query_executes_correctly71() {
        final ITgBogie co = getInstance(ITgBogie.class);
        final EntityResultQueryModel<TgBogie> qry = select(select(TgBogie.class).model()).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("wagonSlot").with("workshop"))).model());
    }

    @Test
    public void eql3_query_executes_correctly72() {
        run(select(TeAverageFuelUsage.class).where().prop("cost.amount").gt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly73() {
        run(select(TeAverageFuelUsage.class).where().prop("cost").lt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly74() {
        run(select(TgVehicle.class).where().prop("sumOfPrices").lt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly75() {
        run(select(TgVehicle.class).where().prop("sumOfPrices.amount").lt().val(100));
    }

    @Test
    public void eql3_query_executes_correctly76() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        final List<TgVehicle> items = co.getAllEntities(from(qry).with("EQL3", null).with(fetch(TgVehicle.class).with("constValueProp")).model());
        System.out.println(items.size());
        System.out.println(items.get(0).getConstValueProp());
    }

    @Test
    public void eql3_query_executes_correctly77() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).as("a").model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("a.key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly78() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly79() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("lastFuelUsage.key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly80() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("a").model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("a.lastFuelUsage.key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly81() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("a").leftJoin(TgOrgUnit4.class).as("b").on().val(1).eq().val(1).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("parent.key").desc().prop("lastFuelUsage.key").asc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly82() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("purchasePrice").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly83() {
        final ITeVehicle co = getInstance(ITeVehicle.class);
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly84() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("date").desc().prop("vehicle.initDate").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly85() {
        final ITeVehicle co = getInstance(ITeVehicle.class);
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly86() {
        final ITeVehicle co = getInstance(ITeVehicle.class);
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("purchasePrice").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly87() {
        final ITeVehicle co = getInstance(ITeVehicle.class);
        final EntityResultQueryModel<TeVehicle> qry = select(TeVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("purchasePrice.amount").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly88() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("vehicle.purchasePrice").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly89() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("vehicle.purchasePrice.amount").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly90() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly91() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("lastFuelUsage.key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly92() {
        final ITeVehicleFuelUsage co = getInstance(ITeVehicleFuelUsage.class);
        final EntityResultQueryModel<TeVehicleFuelUsage> qry = select(TeVehicleFuelUsage.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("vehicle.key").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly93() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("lastFuelUsageQty").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly94() {
        final ITgVehicle co = getInstance(ITgVehicle.class);
        final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
        co.getAllEntities(from(qry).with("EQL3", null)
                .with(fetch(TgVehicle.class).with("lastFuelUsageQty"))
                .with(orderBy().yield("lastFuelUsageQty").desc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly95() {
        final ITgOrgUnit5 co = getInstance(ITgOrgUnit5.class);
        final EntityResultQueryModel<TgOrgUnit5> qry = select(TgOrgUnit5.class).where().anyOfProps("averageVehPrice", "averageVehPurchasePrice").gt().val(0).model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchKeyAndDescOnly(TgOrgUnit5.class)).model());
    }

    @Test
    public void eql3_query_executes_correctly96() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).where().anyOfProps("make", "make.id", "make.key").isNotNull().model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchKeyAndDescOnly(TeVehicleModel.class)).model());
    }

    @Test
    public void eql3_query_executes_correctly97() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).where().anyOfProps("make.id", "make", "make.key").isNotNull().model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchKeyAndDescOnly(TeVehicleModel.class)).model());
    }

    @Test
    public void eql3_query_executes_correctly98() {
        final ITeVehicleModel co = getInstance(ITeVehicleModel.class);
        final EntityResultQueryModel<TeVehicleModel> qry = select(TeVehicleModel.class).where().anyOfProps("make", "make.id", "make.key").isNotNull().model();
        co.getAllEntities(from(qry).with("EQL3", null).with(fetchKeyAndDescOnly(TeVehicleModel.class)).model());
    }

    @Test
    public void eql3_query_executes_correctly99() {
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().prop("make.id").as("makeid").yield().prop("make").as("make").modelAsAggregate();
        final List<EntityAggregates> res = run(qry);
        System.out.println(res.get(0).get("makeid").toString());
        System.out.println(res.get(0).get("make").toString());
    }

    @Test
    public void eql3_query_executes_correctly100() {
        final AggregatedResultQueryModel qry = select(TeVehicle.class).yield().prop("modelMake2").as("make").yield().prop("modelMake2.id").as("makeid").modelAsAggregate();
        final List<EntityAggregates> res = run(qry);
        System.out.println(res.get(0).get("makeid").toString());
        System.out.println(res.get(0).get("make").toString());
    }

    @Test
    public void eql3_query_executes_correctly101() {
        run(select(TeVehicleModel.class).where().prop("make").isNotNull().yield().prop("make.id").as("make").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly102() {
        final AggregatedResultQueryModel qry = select(TeWorkOrder.class).groupBy().prop("vehicle.lastFuelUsage.key").yield().val(1).as("a").modelAsAggregate();
        final List<EntityAggregates> res = aggregateDao.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("vehicle.lastFuelUsage.key").asc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly103() {
        final AggregatedResultQueryModel qry = select(TeVehicleFuelUsage.class).groupBy().prop("key").yield().val(1).as("a").modelAsAggregate();
        final List<EntityAggregates> res = aggregateDao.getAllEntities(from(qry).with("EQL3", null).with(orderBy().prop("key").asc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly104_() {
        final AggregatedResultQueryModel qry = select(TgVehicle.class).yield().prop("lastFuelUsageQty").as("a").modelAsAggregate();
        aggregateDao.getAllEntities(from(qry).with("EQL3", null).with(orderBy().yield("a").asc().model()).model());
    }

    @Test
    public void eql3_query_executes_correctly105() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey7").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly106() {
        run(select(TeVehicle.class).where().anyOfProps("modelMake2.key").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly107() {
        run(select(TgBogie.class).where().prop("location.fuelType").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly108() {
        run(select(TeVehicle.class).where().anyOfProps("avgRepPrice").isNotNull());
    }

    @Test
    public void eql3_query_executes_correctly109() {
        run(select(TeVehicle.class).where().anyOfProps("modelMakeKey8").isNotNull());
    }

    @Test
    public void union_members_can_be_yielded_with_dot_expression() {
        final var query = select(TgBogie.class)
                .where().prop("key").eq().val("BOGIE1")
                .yield().prop("location.workshop").as("workshop")
                .modelAsAggregate();
        final var fetch = fetchAggregates().with("workshop", fetch(TgWorkshop.class).with("key"));
        final var result = aggregateDao.getEntity(from(query).with(fetch).model());
        assertNotNull(result);
        assertEquals("WSHOP1", result.<TgWorkshop>get("workshop").getKey());
    }

    @Test
    @Ignore
    public void union_property_type_is_preserved_when_yielded_in_entity_aggregates() {
        final AggregatedResultQueryModel qry = select(TgBogie.class).where().prop("key").eq().val("BOGIE1").yield().prop("location").as("l").modelAsAggregate();
        final EntityAggregates location = aggregateDao.getEntity(from(qry).with("EQL3", null).with(fetchAggregates().with("l", fetch(TgBogieLocation.class).with("workshop"))).model());
        assertTrue(location.get("l").getClass().getName().startsWith(TgBogieLocation.class.getName()));
        assertEquals("WSHOP1", ((TgWorkshop) location.get("l.workshop")).getKey());
    }

    @Test
    @Ignore
    public void union_property_type_is_preserved_when_yielded_from_query_based_qry_source() {
        final AggregatedResultQueryModel srcQry = select(TgBogie.class).where().prop("key").eq().val("BOGIE1").yield().prop("location").as("l").modelAsAggregate();
        final AggregatedResultQueryModel qry = select(srcQry).yield().prop("l").as("loc").modelAsAggregate();
        final EntityAggregates location = aggregateDao.getEntity(from(qry).with("EQL3", null).with(fetchAggregates().with("loc", fetch(TgBogieLocation.class).with("workshop").with("key"))).model());
        assertTrue(location.get("loc").getClass().getName().startsWith(TgBogieLocation.class.getName()));
        assertEquals("WSHOP1", ((TgWorkshop) location.get("loc.workshop")).getKey());
        assertEquals("WSHOP1", location.get("l.key"));
    }

    @Test
    @Ignore
    public void union_property_type_calc_prop_is_preserved_when_yielded_from_query_based_qry_source() {
        final AggregatedResultQueryModel srcQry = select(TgBogie.class).where().prop("key").eq().val("BOGIE1").yield().prop("location").as("l").modelAsAggregate();
        final AggregatedResultQueryModel qry = select(srcQry).yield().prop("l.key").as("lockey").modelAsAggregate();
        final EntityAggregates location = aggregateDao.getEntity(from(qry).with("EQL3", null).with(fetchAggregates().with("lockey")).model());
        assertEquals("WSHOP1", location.get("lockey"));
    }

    @Test
    public void caseWhen_can_be_used_with_multiple_conditions() {
        run(select(TgBogie.class).where()
                .caseWhen()
                    .prop("key").isNull().and().prop("desc").isNull().or().val(1).gt().val(2)
                    .then().val(1)
                    .otherwise().val(2)
                .end()
                .isNotNull());
    }

    @Test
    public void nulls_can_be_used_in_contexts_of_multiple_values() {
        run(select(TgVehicle.class).where().prop("id").in().values(1, null));
        run(select(TgVehicle.class).where().prop("id").eq().anyOfValues(1, null));
        run(select(TgVehicle.class).where().prop("id").eq().allOfValues(1, null));
    }

    /**
     * @see <a href="https://github.com/fieldenms/tg/issues/2213">Issue #2213</a>
     */
    @Test
    public void union_query_with_nulls_correctly_matches_column_types_01() {
        // null UNION null UNION not-null
        final var qNull = select()
                .yield().val(null).as("a")
                .yield().val(5).as("b")
                .modelAsAggregate();

        final var qNotNull = select()
                .yield().val(10).as("a")
                .yield().val(20).as("b")
                .modelAsAggregate();

        final var qUnion = select(qNull, qNull, qNotNull)
                .yieldAll().modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(qUnion).model());
        assertEqualByContents(
                List.of(listOf(null, 5), listOf(null, 5), listOf(10, 20)),
                entities.stream().map(ent -> listOf(ent.get("a"), ent.get("b"))).toList());
    }

    /**
     * @see <a href="https://github.com/fieldenms/tg/issues/2213">Issue #2213</a>
     */
    @Test
    public void union_query_with_nulls_correctly_matches_column_types_02() {
        // null UNION (null UNION null) UNION not-null
        final var qNull = select()
                .yield().val(null).as("a")
                .yield().val(5).as("b")
                .modelAsAggregate();

        final var qNotNull = select()
                .yield().val(10).as("a")
                .yield().val(20).as("b")
                .modelAsAggregate();

        final var qUnion = select(qNull, select(qNull, qNull).modelAsAggregate(), qNotNull)
                .yieldAll().modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(qUnion).model());
        assertEqualByContents(
                List.of(listOf(null, 5), listOf(null, 5), listOf(null, 5), listOf(10, 20)),
                entities.stream().map(ent -> listOf(ent.get("a"), ent.get("b"))).toList());
    }

    /**
     * @see <a href="https://github.com/fieldenms/tg/issues/2213">Issue #2213</a>
     */
    @Test
    public void union_query_with_nulls_correctly_matches_column_types_03() {
        // (null UNION (not-null UNION null)) UNION (null UNION null)
        final var qNull = select()
                .yield().val(null).as("a")
                .yield().val(5).as("b")
                .modelAsAggregate();

        final var qNotNull = select()
                .yield().val(10).as("a")
                .yield().val(20).as("b")
                .modelAsAggregate();

        final var qUnion = select(
                select(qNull, select(qNotNull, qNull).modelAsAggregate()).modelAsAggregate(),
                select(qNull, qNull).modelAsAggregate())
                .yieldAll().modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(qUnion).model());
        final List<?> qNullResult = listOf(null, 5);
        assertEqualByContents(
                List.of(qNullResult, listOf(10, 20), qNullResult, qNullResult, qNullResult),
                entities.stream().map(ent -> listOf(ent.get("a"), ent.get("b"))).toList());
    }
    
    @Test
    public void nulls_can_be_compared_to_nonNulls_in_join_conditions() {
        // @formatter:off
        final var query =
                select(select().yield().val(null).as("a1").modelAsAggregate())
                 .join(select().yield().val(1).as("b1").modelAsAggregate())
                 .on().prop("a1").eq().prop("b1")
                 .modelAsAggregate();
        // @formatter:on

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void nulls_can_be_compared_to_nulls_in_join_conditions() {
        // @formatter:off
        final var query =
                select(select().yield().val(null).as("a1").modelAsAggregate())
                 .join(select().yield().val(null).as("b1").modelAsAggregate())
                 .on().prop("a1").eq().prop("b1")
                 .modelAsAggregate();
        // @formatter:on

        co(EntityAggregates.class).getAllEntities(from(query).model());
        // don't assert anything about the result set because the boolean result of (NULL = NULL) may vary across SQL implementations
    }

    @Test
    public void nonNulls_can_be_compared_to_nonNulls_in_join_conditions() {
        // @formatter:off
        final var query =
                select(select().yield().val(1).as("a1").modelAsAggregate())
                 .join(select().yield().val(5).as("b1").modelAsAggregate())
                 .on().prop("a1").eq().prop("b1")
                 .modelAsAggregate();
        // @formatter:on

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void like_is_applicable_to_nonString_values() {
        final var sourceQuery = select()
                .yield().val(55).as("myInt")
                .yield().val(new BigDecimal("33.3")).as("myBigDecimal")
                .yield().val(date("1936-05-31 11:00:00")).as("myDate")
                .modelAsAggregate();

        final var co = co(EntityAggregates.class);
        assertTrue(co.exists(select(sourceQuery).where()
                .prop("myInt").like().val("5%").and().prop("myInt").notLike().val("abc")
                .and()
                .prop("myInt").iLike().val("5%").and().prop("myInt").notILike().val("abc")
                .model()));
        assertTrue(co.exists(select(sourceQuery).where()
                .prop("myBigDecimal").like().val("33%").and().prop("myBigDecimal").notLike().val("abc")
                .and()
                .prop("myBigDecimal").iLike().val("33%").and().prop("myBigDecimal").notILike().val("abc")
                .model()));
        // NOTE assume dd/MM/YYYY date format
        assertTrue(co.exists(select(sourceQuery).where()
                .prop("myDate").like().val("%31/05%").and().prop("myDate").notLike().val("abc")
                .and()
                .prop("myDate").iLike().val("%31/05%").and().prop("myDate").notILike().val("abc")
                .model()));
    }

    @Test
    public void like_produces_false_if_either_operand_is_null() {
        final var sourceQuery = select().yield().val("hello").as("s").modelAsAggregate();

        assertFalse(co(EntityAggregates.class).exists(
                select(sourceQuery)
                        .where()
                        .val("one").like().val(null)
                        .model()));

        assertFalse(co(EntityAggregates.class).exists(
                select(sourceQuery)
                        .where()
                        .val(null).like().val("%")
                        .model()));

        assertFalse(co(EntityAggregates.class).exists(
                select(sourceQuery)
                        .where()
                        .val(null).like().val(null)
                        .model()));

        assertFalse(co(EntityAggregates.class).exists(
                select(sourceQuery)
                        .where()
                        .val("one").like().caseWhen().val(1).gt().val(0).then().val(null).otherwise().val("ok").end()
                        .model()));
    }

    @Test
    public void caseWhen_returning_only_nulls_can_be_used_in_comparison_with_a_nonNull_and_nonString() {
        final var sourceQuery = select().yield().val(55).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                // endAs* must be used, end() is illegal when there are only nulls
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(null).endAsInt()
                .model();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void caseWhen_returning_a_null_and_a_nonNull_can_be_used_in_comparison_with_a_nonNull() {
        final BigDecimal n = new BigDecimal("1487432.56788765");
        final var sourceQuery = select().yield().val(n).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(n).end()
                .model();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(n), entities.stream().map(ent -> ent.get("n")).toList());
    }

    @Test
    public void caseWhen_endAsInt_returns_integer_values() {
        final Integer n = 15;
        final var sourceQuery = select().yield().val(n).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(n.toString()).endAsInt()
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEqualByContents(
                List.of(n),
                entities.stream().map(ent -> ent.get("n")).toList());
    }

    @Test
    public void caseWhen_endAsDecimal_returns_decimal_value() {
        final BigDecimal n = new BigDecimal("123.456789");
        final var sourceQuery = select().yield().val(n).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(n.toString()).endAsDecimal(n.precision(), n.scale())
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEqualByContents(
                List.of(n),
                entities.stream().map(ent -> ent.get("n")).toList());
    }

    @Test
    public void caseWhen_endAsDecimal_returns_decimal_value_when_all_values_are_null() {
        final BigDecimal n = new BigDecimal("123.456789");
        final var sourceQuery = select().yield().val(n).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(null).otherwise().val(null).endAsDecimal(n.precision(), n.scale())
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void caseWhen_endAsString_returns_string_value() {
        final String n = "15";
        final var sourceQuery = select().yield().val(n).as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(15).endAsStr(16)
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEqualByContents(
                List.of(n),
                entities.stream().map(ent -> ent.get("n")).toList());
    }

    @Test
    public void caseWhen_endAsString_returns_string_value_when_all_values_are_null() {
        final var sourceQuery = select().yield().val("15").as("n").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("n").eq().caseWhen().val(1).isNotNull().then().val(null).otherwise().val(null).endAsStr(16)
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void caseWhen_endAsBool_returns_boolean_value_when_all_values_are_null() {
        final var sourceQuery = select().yield().val("word").as("x").modelAsAggregate();
        final var query = select(sourceQuery).where()
                .prop("x").eq().caseWhen().val(1).isNotNull().then().val(null).otherwise().val(null).endAsBool()
                .yieldAll()
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEquals(List.of(), entities);
    }

    @Test
    public void Date_val_can_be_used_in_isNull_expression_01() {
        final var query = select()
                .yield().caseWhen().val(date("2024-04-18 14:10:00")).isNull().then().val("is").otherwise().val("isnt").end().as("s")
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEqualByContents(
                List.of("isnt"),
                entities.stream().map(ent -> ent.get("s")).toList());
    }

    @Test
    public void Date_val_can_be_used_in_isNull_expression_02() {
        // tricky case, Date val is fetched instead of being constructed ad-hoc
        final var fetchedDate = co(EntityAggregates.class).getEntity(
                        from(select().yield().val(date("2024-04-18 14:10:00")).as("myDate").modelAsAggregate()).model())
                .get("myDate");
        final var query = select()
                .yield().caseWhen().val(fetchedDate).isNull().then().val("is").otherwise().val("isnt").end().as("s")
                .modelAsAggregate();

        final List<EntityAggregates> entities = co(EntityAggregates.class).getAllEntities(from(query).model());
        assertEqualByContents(
                List.of("isnt"),
                entities.stream().map(ent -> ent.get("s")).toList());
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));

        final TgBogieLocation location = co$(TgBogieLocation.class).new_();
        location.setWorkshop(workshop1);
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2"));
        final TgBogie bogie3 = save(new_(TgBogie.class, "BOGIE3", "Bogie 3"));
        final TgBogie bogie4 = save(new_(TgBogie.class, "BOGIE4", "Bogie 4"));
        final TgBogie bogie5 = save(new_(TgBogie.class, "BOGIE5", "Bogie 5"));
        final TgBogie bogie6 = save(new_(TgBogie.class, "BOGIE6", "Bogie 6"));
        final TgBogie bogie7 = save(new_(TgBogie.class, "BOGIE7", "Bogie 7"));

        final TgWagon wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final TgWagon wagon2 = save(new_(TgWagon.class, "WAGON2", "Wagon 2"));

        save(new_composite(TgWagonSlot.class, wagon1, 5));
        save(new_composite(TgWagonSlot.class, wagon1, 6));
        save(new_composite(TgWagonSlot.class, wagon1, 7));
        save(new_composite(TgWagonSlot.class, wagon1, 8));
        save(new_composite(TgWagonSlot.class, wagon1, 4).setBogie(bogie1));
        save(new_composite(TgWagonSlot.class, wagon1, 3).setBogie(bogie2));
        save(new_composite(TgWagonSlot.class, wagon1, 2).setBogie(bogie3));
        save(new_composite(TgWagonSlot.class, wagon1, 1).setBogie(bogie4));

        save(new_composite(TgWagonSlot.class, wagon2, 1).setBogie(bogie5));
        save(new_composite(TgWagonSlot.class, wagon2, 2).setBogie(bogie6));

        final TgWagonSlot wagonSlot = save(new_composite(TgWagonSlot.class, wagon2, 3).setBogie(bogie7).setFuelType(petrolFuelType));
        final TgBogieLocation slotLocation = co$(TgBogieLocation.class).new_();
        slotLocation.setWagonSlot(wagonSlot);
        save(bogie7.setLocation(slotLocation));

        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5"));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
        final TgVehicleMake subaro = save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TeVehicleMake merc_ = save(new_(TeVehicleMake.class, "MERC", "Mercedes"));
        final TeVehicleMake audi_ = save(new_(TeVehicleMake.class, "AUDI", "Audi"));
        final TeVehicleMake bmw_ = save(new_(TeVehicleMake.class, "BMW", "BMW"));
        final TeVehicleMake subaro_ = save(new_(TeVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final TgVehicleModel m317 = save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        final TgVehicleModel m319 = save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        final TgVehicleModel m320 = save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        final TgVehicleModel m321 = save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        final TgVehicleModel m322 = save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TeVehicleModel m316_ = save(new_(TeVehicleModel.class, "316", "316").setMake(merc_));
        final TeVehicleModel m317_ = save(new_(TeVehicleModel.class, "317", "317").setMake(audi_));
        final TeVehicleModel m318_ = save(new_(TeVehicleModel.class, "318", "318").setMake(audi_));
        final TeVehicleModel m319_ = save(new_(TeVehicleModel.class, "319", "319").setMake(bmw_));
        final TeVehicleModel m320_ = save(new_(TeVehicleModel.class, "320", "320").setMake(bmw_));
        final TeVehicleModel m321_ = save(new_(TeVehicleModel.class, "321", "321").setMake(bmw_));
        final TeVehicleModel m322_ = save(new_(TeVehicleModel.class, "322", "322").setMake(bmw_));

        final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5).setReplacedBy(car1));

        final TeVehicle car1_ = save(new_(TeVehicle.class, "CAR11", "CAR11 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318_).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
        final TeVehicle car2_ = save(new_(TeVehicle.class, "CAR22", "CAR22 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316_).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5).setReplacedBy(car1_));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

        final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
        final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
        final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
        final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
        final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
        final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

        final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true));
        final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1));
        final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1));

        save(new_composite(UserAndRoleAssociation.class, user1, managerRole));
        save(new_composite(UserAndRoleAssociation.class, user1, analyticRole));
        save(new_composite(UserAndRoleAssociation.class, user2, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user2, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user2, warehouseOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, dataEntryRole));
        save(new_composite(UserAndRoleAssociation.class, user3, fleetOperatorRole));
        save(new_composite(UserAndRoleAssociation.class, user3, warehouseOperatorRole));

        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        final TgAuthor chrisDate = save(new_composite(TgAuthor.class, chris, "Date", null));

        final TgPersonName yurij = save(new_(TgPersonName.class, "Yurij", "Yurij"));
        final TgAuthor yurijShcherbyna = save(new_composite(TgAuthor.class, yurij, "Shcherbyna", "Mykolajovych"));

        save(new_composite(TgAuthorship.class, chrisDate, "An Introduction to Database Systems").setYear(2003));
        save(new_composite(TgAuthorship.class, chrisDate, "Database Design and Relational Theory").setYear(2012));
        save(new_composite(TgAuthorship.class, chrisDate, "SQL and Relational Theory").setYear(2015));
        save(new_composite(TgAuthorship.class, yurijShcherbyna, "Дискретна математика").setYear(2007));
    }
}
