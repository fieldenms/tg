package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;
import static ua.com.fielden.platform.eql.stage0.YieldBuilder.ABSENT_ALIAS;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.meta.query.PrimTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgPersonName;

public class QmToStage2TransformationTest extends EqlStage2TestCase {
    
    @Test
    public void correlated_source_query_works() {
        final AggregatedResultQueryModel sourceQry = select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate();
        final PrimitiveResultQueryModel qtySubQry = select(sourceQry).yield().prop("qty").modelAsPrimitive();
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().model(qtySubQry).as("qty").modelAsAggregate();

        final ResultQuery2 actQry = qry(qry);
        
        final Source2BasedOnPersistentType modelSource = source(3, MODEL);
        
        final Source2BasedOnPersistentType vehSource = source(1, VEHICLE);
        final IJoinNode2<? extends IJoinNode3> vehSources = sources(vehSource);
        final Prop2 vehModelProp = prop(vehSource, pi(VEHICLE, "model"));
        final Prop2 modelIdProp = prop(modelSource, pi(MODEL, "id"));
        final Conditions2 vehConditions = cond(eq(vehModelProp, modelIdProp));
        final Yields2 vehYields = mkYields(yieldCountAll("qty"));

        final SourceQuery2 vehSourceQry = srcqry(vehSources, vehConditions, vehYields);
        
        final QuerySourceInfo<EntityAggregates> querySourceInfo = new QuerySourceInfo<>(EntityAggregates.class, false);
        querySourceInfo.addProp(new PrimTypePropInfo<>("qty", INTEGER, null));
        
        final Source2BasedOnQueries qtyQrySource = source(querySourceInfo, 2, vehSourceQry);
        final IJoinNode2<? extends IJoinNode3> qtyQrySources = sources(qtyQrySource);
        final Yields2 qtyQryYields = mkYields(QmToStage2TransformationTest.mkYield(prop(qtyQrySource, new PrimTypePropInfo<>("qty", INTEGER, H_INTEGER)), ABSENT_ALIAS));
        
        
        final Yields2 modelQryYields = mkYields(QmToStage2TransformationTest.mkYield(subqry(qtyQrySources, qtyQryYields, INTEGER_PROP_TYPE), "qty"));
        
        final ResultQuery2 expQry = qry(sources(modelSource), modelQryYields);
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void correlated_source_queries_work() {
        final AggregatedResultQueryModel sourceQry1 = select(TeVehicle.class).where().prop("id").isNotNull().and().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate();
        final AggregatedResultQueryModel sourceQry2 = select(TeVehicle.class).where().prop("id").isNull().and().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate();
        final PrimitiveResultQueryModel qtyQry = select(sourceQry1, sourceQry2).yield().prop("qty").modelAsPrimitive();
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().model(qtyQry).as("qty").modelAsAggregate();

        final ResultQuery2 actQry = qry(qry);
        
        final Source2BasedOnPersistentType modelSource = source(4, MODEL);
        final Prop2 modelIdProp = prop(modelSource, pi(MODEL, "id"));
        
        final Source2BasedOnPersistentType vehSource1 = source(1, VEHICLE);
        final IJoinNode2<? extends IJoinNode3> vehSources1 = sources(vehSource1);
        final Prop2 vehModelProp1 = prop(vehSource1, pi(VEHICLE, "model"));
        final Prop2 vehIdProp1 = prop(vehSource1, pi(VEHICLE, "id"));
        final Conditions2 vehConditions1 = or(and(isNotNull(vehIdProp1), eq(vehModelProp1, modelIdProp)));
        final Yields2 vehYields1 = mkYields(yieldCountAll("qty"));

        final SourceQuery2 vehSourceQry1 = srcqry(vehSources1, vehConditions1, vehYields1);

        final Source2BasedOnPersistentType vehSource2 = source(2, VEHICLE);
        final IJoinNode2<? extends IJoinNode3> vehSources2 = sources(vehSource2);
        final Prop2 vehModelProp2 = prop(vehSource2, pi(VEHICLE, "model"));
        final Prop2 vehIdProp2 = prop(vehSource2, pi(VEHICLE, "id"));
        final Conditions2 vehConditions2 = or(and(isNull(vehIdProp2), eq(vehModelProp2, modelIdProp)));
        final Yields2 vehYields2 = mkYields(yieldCountAll("qty"));

        final SourceQuery2 vehSourceQry2 = srcqry(vehSources2, vehConditions2, vehYields2);

        final QuerySourceInfo<EntityAggregates> querySourceInfo = new QuerySourceInfo<>(EntityAggregates.class, false);
        querySourceInfo.addProp(new PrimTypePropInfo<>("qty", INTEGER, null));
        
        final Source2BasedOnQueries qtyQrySource = source(querySourceInfo, 3, vehSourceQry1, vehSourceQry2);
        final IJoinNode2<? extends IJoinNode3> qtyQrySources = sources(qtyQrySource);
        final Yields2 qtyQryYields = mkYields(mkYield(prop(qtyQrySource, new PrimTypePropInfo<>("qty", INTEGER, H_INTEGER)), ABSENT_ALIAS));
        
        
        final Yields2 modelQryYields = mkYields(mkYield(subqry(qtyQrySources, qtyQryYields, INTEGER_PROP_TYPE), "qty"));
        
        final ResultQuery2 expQry = qry(sources(modelSource), modelQryYields);
        assertEquals(expQry, actQry);
    }

    @Test
    public void test01() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop("make").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, MODEL);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 makeProp = prop(source, pi(MODEL, "make"));
        final Conditions2 conditions = cond(isNotNull(makeProp));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void test03() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop("make.key").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, MODEL);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 makeProp = prop(source, pi(MODEL, "make"), pi(MAKE, "key"));
        final Conditions2 conditions = cond(isNotNull(makeProp));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void prop_paths_are_correctly_resolved() {
        final ResultQuery2 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 initDate = prop(source, pi(VEHICLE, "initDate"));
        final Prop2 station_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "name"));
        final Prop2 station_parent_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name"));
        final Prop2 replacedBy_initDate = prop(source, pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate"));
        
        final Conditions2 conditions = or(and(or(
                isNotNull(initDate), 
                isNotNull(station_name), 
                isNotNull(station_parent_name), 
                isNotNull(replacedBy_initDate)
                )));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void prop_paths_without_aliases_with_aliased_source_are_correctly_resolved() {
        final ResultQuery2 actQry = qryCountAll(select(VEHICLE).as("v").where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, VEHICLE, "v");
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 initDate = prop(source, pi(VEHICLE, "initDate"));
        final Prop2 station_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "name"));
        final Prop2 station_parent_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name"));
        final Prop2 replacedBy_initDate = prop(source, pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate"));
        
        final Conditions2 conditions = or(and(or(
                isNotNull(initDate), 
                isNotNull(station_name), 
                isNotNull(station_parent_name), 
                isNotNull(replacedBy_initDate)
                )));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void prop_paths_with_some_aliases_with_aliased_source_are_correctly_resolved() {
        final ResultQuery2 actQry = qryCountAll(select(VEHICLE).as("v").where().anyOfProps("v.initDate", "station.name", "station.parent.name", "v.replacedBy.initDate").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, VEHICLE, "v");
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 initDate = prop(source, pi(VEHICLE, "initDate"));
        final Prop2 station_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "name"));
        final Prop2 station_parent_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name"));
        final Prop2 replacedBy_initDate = prop(source, pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate"));
        
        final Conditions2 conditions = or(and(or(
                isNotNull(initDate), 
                isNotNull(station_name), 
                isNotNull(station_parent_name), 
                isNotNull(replacedBy_initDate)
                )));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void prop_paths_in_qry_with_two_sources_are_correctly_resolved() {
        final ResultQuery2 actQry = qryCountAll(select(VEHICLE).as("v").join(VEHICLE).as("rv").on().prop("v.replacedBy").eq().prop("rv.id").
                where().anyOfProps("v.initDate", "rv.station.name", "v.station.parent.name", "rv.replacedBy.initDate").isNotNull());

        final Source2BasedOnPersistentType source = source(1, VEHICLE, "v");
        final Source2BasedOnPersistentType source2 = source(2, VEHICLE, "rv");
        final IJoinNode2<? extends IJoinNode3> sources = ij(source, source2, or(eq(prop(source, pi(VEHICLE, "replacedBy")), prop(source2, pi(VEHICLE, "id")))));
        final Prop2 initDate = prop(source, pi(VEHICLE, "initDate"));
        final Prop2 station_name = prop(source2, pi(VEHICLE, "station"), pi(ORG5, "name"));
        final Prop2 station_parent_name = prop(source, pi(VEHICLE, "station"), pi(ORG5, "parent"), pi(ORG4, "name"));
        final Prop2 replacedBy_initDate = prop(source2, pi(VEHICLE, "replacedBy"), pi(VEHICLE, "initDate"));
        
        final Conditions2 conditions = or(and(or(
                isNotNull(initDate), 
                isNotNull(station_name), 
                isNotNull(station_parent_name), 
                isNotNull(replacedBy_initDate)
                )));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void test05() {
        final ResultQuery2 actQry = qryCountAll(select(ORG1).where().exists(select(ORG2).where().prop("parent").eq().extProp("id").model()));  

        final Source2BasedOnPersistentType source = source(2, ORG1);
        final Source2BasedOnPersistentType subQrySource = source(1, ORG2);

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> subQrySources = sources(subQrySource);
        final Conditions2 subQryConditions = or(eq(prop(subQrySource, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))));
        final Conditions2 conditions = or(exists(subQrySources, subQryConditions));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void test06() {
        final EntityResultQueryModel<TgOrgUnit2> subqry = select(ORG2).where().prop("parent").eq().extProp("id").model();
        final ResultQuery2 actQry = qryCountAll(select(ORG1).where().exists(subqry).or().notExists(subqry));  

        final Source2BasedOnPersistentType source = source(3, ORG1);
        final Source2BasedOnPersistentType subQrySource1 = source(1, ORG2);
        final Source2BasedOnPersistentType subQrySource2 = source(2, ORG2);

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> subQrySources1 = sources(subQrySource1);
        final IJoinNode2<? extends IJoinNode3> subQrySources2 = sources(subQrySource2);
        final Conditions2 subQryConditions1 = or(eq(prop(subQrySource1, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))));
        final Conditions2 subQryConditions2 = or(eq(prop(subQrySource2, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))));
        final Conditions2 conditions = or(exists(subQrySources1, subQryConditions1), notExists(subQrySources2, subQryConditions2));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void test07() {
        final ResultQuery2 actQry = qryCountAll(//
        select(ORG1).where().exists( //
        select(ORG2).where().prop("parent").eq().extProp("id").and().exists( //
        select(ORG3).where().prop("parent").eq().extProp("id").and().exists( // 
        select(ORG4).where().prop("parent").eq().extProp("id").and().exists( //
        select(ORG5).where().prop("parent").eq().extProp("id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()));

        final Source2BasedOnPersistentType source = source(5, ORG1);
        final Source2BasedOnPersistentType sub1QrySource = source(4, ORG2);
        final Source2BasedOnPersistentType sub2QrySource = source(3, ORG3);
        final Source2BasedOnPersistentType sub3QrySource = source(2, ORG4);
        final Source2BasedOnPersistentType sub4QrySource = source(1, ORG5);

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> sub1QrySources = sources(sub1QrySource);
        final IJoinNode2<? extends IJoinNode3> sub2QrySources = sources(sub2QrySource);
        final IJoinNode2<? extends IJoinNode3> sub3QrySources = sources(sub3QrySource);
        final IJoinNode2<? extends IJoinNode3> sub4QrySources = sources(sub4QrySource);

        final Conditions2 subQryConditions4 = or(and(eq(prop(sub4QrySource, pi(ORG5, "parent")), prop(sub3QrySource, pi(ORG4, "id"))), isNotNull(prop(sub4QrySource, pi(ORG5, "key")))));
        final Conditions2 subQryConditions3 = or(and(eq(prop(sub3QrySource, pi(ORG4, "parent")), prop(sub2QrySource, pi(ORG3, "id"))), exists(sub4QrySources, subQryConditions4)));
        final Conditions2 subQryConditions2 = or(and(eq(prop(sub2QrySource, pi(ORG3, "parent")), prop(sub1QrySource, pi(ORG2, "id"))), exists(sub3QrySources, subQryConditions3)));
        final Conditions2 subQryConditions1 = or(and(eq(prop(sub1QrySource, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))), exists(sub2QrySources, subQryConditions2)));
        
        final Conditions2 conditions = or(exists(sub1QrySources, subQryConditions1));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void test08() {
        final ResultQuery2 actQry = qryCountAll(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().prop("L1.id").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().prop("L2.id").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().prop("L3.id").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().prop("L4.id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()));
        
        final Source2BasedOnPersistentType source = source(5, ORG1, "L1");
        final Source2BasedOnPersistentType sub1QrySource = source(4, ORG2, "L2");
        final Source2BasedOnPersistentType sub2QrySource = source(3, ORG3, "L3");
        final Source2BasedOnPersistentType sub3QrySource = source(2, ORG4, "L4");
        final Source2BasedOnPersistentType sub4QrySource = source(1, ORG5, "L5");

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> sub1QrySources = sources(sub1QrySource);
        final IJoinNode2<? extends IJoinNode3> sub2QrySources = sources(sub2QrySource);
        final IJoinNode2<? extends IJoinNode3> sub3QrySources = sources(sub3QrySource);
        final IJoinNode2<? extends IJoinNode3> sub4QrySources = sources(sub4QrySource);

        final Conditions2 subQryConditions4 = or(and(eq(prop(sub4QrySource, pi(ORG5, "parent")), prop(sub3QrySource, pi(ORG4, "id"))), isNotNull(prop(sub4QrySource, pi(ORG5, "key")))));
        final Conditions2 subQryConditions3 = or(and(eq(prop(sub3QrySource, pi(ORG4, "parent")), prop(sub2QrySource, pi(ORG3, "id"))), exists(sub4QrySources, subQryConditions4)));
        final Conditions2 subQryConditions2 = or(and(eq(prop(sub2QrySource, pi(ORG3, "parent")), prop(sub1QrySource, pi(ORG2, "id"))), exists(sub3QrySources, subQryConditions3)));
        final Conditions2 subQryConditions1 = or(and(eq(prop(sub1QrySource, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))), exists(sub2QrySources, subQryConditions2)));
        
        final Conditions2 conditions = or(exists(sub1QrySources, subQryConditions1));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void test09() {
        final ResultQuery2 actQry = qryCountAll(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().prop("L1").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().prop("L2").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().prop("L3").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().prop("L4").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()));
        
        final Source2BasedOnPersistentType source = source(5, ORG1, "L1");
        final Source2BasedOnPersistentType sub1QrySource = source(4, ORG2, "L2");
        final Source2BasedOnPersistentType sub2QrySource = source(3, ORG3, "L3");
        final Source2BasedOnPersistentType sub3QrySource = source(2, ORG4, "L4");
        final Source2BasedOnPersistentType sub4QrySource = source(1, ORG5, "L5");

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> sub1QrySources = sources(sub1QrySource);
        final IJoinNode2<? extends IJoinNode3> sub2QrySources = sources(sub2QrySource);
        final IJoinNode2<? extends IJoinNode3> sub3QrySources = sources(sub3QrySource);
        final IJoinNode2<? extends IJoinNode3> sub4QrySources = sources(sub4QrySource);

        final Conditions2 subQryConditions4 = or(and(eq(prop(sub4QrySource, pi(ORG5, "parent")), prop(sub3QrySource, pi(ORG4, "id"))), isNotNull(prop(sub4QrySource, pi(ORG5, "key")))));
        final Conditions2 subQryConditions3 = or(and(eq(prop(sub3QrySource, pi(ORG4, "parent")), prop(sub2QrySource, pi(ORG3, "id"))), exists(sub4QrySources, subQryConditions4)));
        final Conditions2 subQryConditions2 = or(and(eq(prop(sub2QrySource, pi(ORG3, "parent")), prop(sub1QrySource, pi(ORG2, "id"))), exists(sub3QrySources, subQryConditions3)));
        final Conditions2 subQryConditions1 = or(and(eq(prop(sub1QrySource, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))), exists(sub2QrySources, subQryConditions2)));
        
        final Conditions2 conditions = or(exists(sub1QrySources, subQryConditions1));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void test10() {
        final ResultQuery2 actQry = qryCountAll(//
        select(ORG1).as("L1").where().exists( //
        select(ORG2).as("L2").where().prop("parent").eq().extProp("L1.id").and().exists( //
        select(ORG3).as("L3").where().prop("parent").eq().prop("L1.id").and().exists( // 
        select(ORG4).as("L4").where().prop("parent").eq().extProp("L1").and().exists( //
        select(ORG5).as("L5").where().prop("parent").eq().prop("L1.id").and().prop("key").isNotNull(). //
        model()). //
        model()). //
        model()). //
        model()));
        
        final Source2BasedOnPersistentType source = source(5, ORG1, "L1");
        final Source2BasedOnPersistentType sub1QrySource = source(4, ORG2, "L2");
        final Source2BasedOnPersistentType sub2QrySource = source(3, ORG3, "L3");
        final Source2BasedOnPersistentType sub3QrySource = source(2, ORG4, "L4");
        final Source2BasedOnPersistentType sub4QrySource = source(1, ORG5, "L5");

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> sub1QrySources = sources(sub1QrySource);
        final IJoinNode2<? extends IJoinNode3> sub2QrySources = sources(sub2QrySource);
        final IJoinNode2<? extends IJoinNode3> sub3QrySources = sources(sub3QrySource);
        final IJoinNode2<? extends IJoinNode3> sub4QrySources = sources(sub4QrySource);
        
        final Conditions2 subQryConditions4 = or(and(eq(prop(sub4QrySource, pi(ORG5, "parent")), prop(source, pi(ORG1, "id"))), isNotNull(prop(sub4QrySource, pi(ORG5, "key")))));
        final Conditions2 subQryConditions3 = or(and(eq(prop(sub3QrySource, pi(ORG4, "parent")), prop(source, pi(ORG1, "id"))), exists(sub4QrySources, subQryConditions4)));
        final Conditions2 subQryConditions2 = or(and(eq(prop(sub2QrySource, pi(ORG3, "parent")), prop(source, pi(ORG1, "id"))), exists(sub3QrySources, subQryConditions3)));
        final Conditions2 subQryConditions1 = or(and(eq(prop(sub1QrySource, pi(ORG2, "parent")), prop(source, pi(ORG1, "id"))), exists(sub2QrySources, subQryConditions2)));
        
        final Conditions2 conditions = or(exists(sub1QrySources, subQryConditions1));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void test11() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop("make").eq().iVal(null));
        
        final Source2BasedOnPersistentType source = source(1, MODEL);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final ResultQuery2 expQry = qryCountAll(sources);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void test12() {
        final ResultQuery2 actQry = qryCountAll(select(ORG1).where().exists(select(ORG2).where().prop("parent").isNotNull().model()));  

        final Source2BasedOnPersistentType source = source(2, ORG1);
        final Source2BasedOnPersistentType subQrySource = source(1, ORG2);

        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final IJoinNode2<? extends IJoinNode3> subQrySources = sources(subQrySource);
        final Conditions2 subQryConditions = or(isNotNull(prop(subQrySource, pi(ORG2, "parent"))));
        
        final Conditions2 conditions = or(exists(subQrySources, subQryConditions));

        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void resolution_context_for_correlated_source_query_skips_qry_sources_declared_within_the_same_from_stmt() {
        try {
            qryCountAll(
                    select(TeVehicle.class).as("veh").
                    join(
                            select(TeVehicleModel.class).
                            where().
                            prop("make").eq().extProp("veh.model.make").model()
                            ).as("mk").
                    on().val(1).eq().val(1).
                    where().val(1).eq().val(1));
            fail("Should have failed while trying to resolve property [veh.model.make]");
        } catch (final EqlStage1ProcessingException e) {
            assertEquals("Can't resolve property [veh.model.make].", e.getMessage());
        }
    }
    
    @Test
    public void test_05() {
        qryCountAll(select(AUTHOR).where().exists(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()));
    }

    @Test
    public void test_06() {
        qryCountAll(select(TgAuthorRoyalty.class).as("ar").where().exists(select(TgAuthorship.class).where().prop("id").eq().extProp("authorship").and().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").model()));
    }

    @Test
    public void test_07() {
        qryCountAll(select(select(TgAuthorship.class).where().prop("title").isNotNull().model()).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris"));
    }

    @Test
    public void test_08() {
        qryCountAll(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().model()).as("ar").where().prop("authorship.author.surname").eq().val("Date").or().prop("ar.authorship.author.name.key").eq().val("Chris"));
    }

    @Test
    @Ignore
    //TODO EQL3+
    public void test_09() {
        qryCountAll(select(select(TgAuthorRoyalty.class).where().prop("authorship.author.surname").isNotNull().yield().prop("authorship").modelAsEntity(TgAuthorship.class)).where().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris"));
    }

    @Test
    public void test_10() {
        qryCountAll(select(select(TgAuthorship.class).where().prop("title").isNotNull().yield().prop("author").as("author").yield().prop("title").as("bookTitle").modelAsAggregate()).where().prop("bookTitle").isNotNull().or().begin().prop("author.surname").eq().val("Date").or().prop("author.name.key").eq().val("Chris").end());
    }

    @Test
    public void test_11() {
        qryCountAll(select(TgAuthorship.class).where().beginExpr().val(100).mult().model(select(AUTHOR).yield().countAll().modelAsPrimitive()).endExpr().ge().val(1000));
    }

    @Test
    public void test_13() {
        qryCountAll(select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2"));
    }

    @Test
    public void test_14() {
        qryCountAll(select(AUTHOR).where().prop("hasMultiplePublications").eq().val(true));
    }

    @Test
    public void test_16() {
        qryCountAll(select(AUTHOR).leftJoin(TgPersonName.class).as("pn").on().prop("name").eq().prop("pn").where().prop("lastRoyalty").eq().val(1));
    }

    @Test
    public void test_17() {
        //transform(select(AUTHOR).where().prop("name").isNotNull().groupBy().prop("name").yield().prop("name").modelAsEntity(TgPersonName.class));
    }

    @Test
    public void test_22() {
        qryCountAll(select(TgAuthorRoyalty.class).where().prop("payment").isNotNull());
    }

    @Test
    public void test_23() {
        qryCountAll(select(TgAuthorRoyalty.class).where().prop("payment.amount").isNotNull());
    }

    @Test
    public void test_24() {
        qryCountAll(select(select(VEHICLE).yield().prop("key").as("key").yield().prop("desc").as("desc").yield().prop("model.make").as("model-make").modelAsAggregate()).where().prop("model-make").isNotNull());
    }
}