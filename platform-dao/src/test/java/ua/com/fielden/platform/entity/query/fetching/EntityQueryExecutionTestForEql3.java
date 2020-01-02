package ua.com.fielden.platform.entity.query.fetching;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class EntityQueryExecutionTestForEql3 extends AbstractDaoTestCase {
    private final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    private void run(final AggregatedResultQueryModel qry) {
        aggregateDao.getAllEntities(from(qry).with("EQL3", null).model());
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
        run(select(TeWorkOrder.class).where().prop("makeKey").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly9() {
        run(select(TeWorkOrder.class).where().prop("make.key").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly10() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.makeKey").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly11() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.makeKey2").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly12() {
        run(select(TeWorkOrder.class).where().anyOfProps("model.makeKey2").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly13() {
        run(select(TeWorkOrder.class).where().anyOfProps("makeKey2").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly14() {
        run(select(TeVehicle.class).where().anyOfProps("calcModel").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly15() {
        run(select(TeVehicleMake.class).where().anyOfProps("c7").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly16() {
        run(select(TeVehicleMake.class).where().anyOfProps("c6").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly17() {
        run(select(TeVehicleMake.class).where().anyOfProps("c3").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly18() {
        run(select(TeVehicleMake.class).where().anyOfProps("c8").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }

    @Test
    public void eql3_query_executes_correctly19() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.makeKey", "vehicle.model.make.key").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly20() {
        run(select(TeWorkOrder.class).where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly21() {
        run(select(TeVehicle.class).where().anyOfProps("modelKey", "modelDesc").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
    
    @Test
    public void eql3_query_executes_correctly22() {
        run(select(TeVehicle.class).where().anyOfProps("makeKey2", "make.key").isNotNull().yield().countAll().as("KOUNT").modelAsAggregate());
    }
}