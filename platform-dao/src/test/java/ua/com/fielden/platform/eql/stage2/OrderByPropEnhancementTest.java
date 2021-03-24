package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.QrySource2BasedOnPersistentType;

public class OrderByPropEnhancementTest extends EqlStage2TestCase {

    @Test
    public void test001() {
        final ResultQuery2 actQry = qry(select(MODEL).model(), orderBy().prop("key").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", MODEL);
        final Prop2 keyProp = prop(source, pi(MODEL, "key"));

        assertEquals(orderBys(orderDesc(keyProp)), actQry.orderings);
    }
    
    @Test
    public void test002() {
        final ResultQuery2 actQry = qry(select(ORG2).model(), orderBy().prop("key").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", ORG2);
        final Prop2 key1Prop = prop(source, pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop = prop(source, pi(ORG2, "name"));
        
        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test003() {
        final ResultQuery2 actQry = qry(select(ORG2).as("o2").model(), orderBy().prop("o2.key").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", ORG2, "o2");
        final Prop2 key1Prop = prop(source, pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop = prop(source, pi(ORG2, "name"));
        
        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test004() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().prop("purchasePrice").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);
        final Prop2 prop = prop(source, pi(VEHICLE, "purchasePrice"), pi(VEHICLE, "purchasePrice", "amount"));
        
        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

    @Test
    public void test005() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().prop("purchasePrice.amount").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);
        final Prop2 prop = prop(source, pi(VEHICLE, "purchasePrice"), pi(VEHICLE, "purchasePrice", "amount"));
        
        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

    @Test
    public void test006() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().prop("lastFuelUsage.key").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);
        final Prop2 key1Prop = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));
        
        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test007() {
        final ResultQuery2 actQry = qry(select(VEHICLE).leftJoin(ORG4).on().val(1).eq().val(1).model(), orderBy().prop("parent.key").desc().prop("lastFuelUsage.key").asc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);
        final QrySource2BasedOnPersistentType source2 = source("2", ORG4);

        final Prop2 key1Prop1 = prop(source2, pi(ORG4, "parent"), pi(ORG3, "parent"), pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop1 = prop(source2, pi(ORG4, "parent"), pi(ORG3, "parent"), pi(ORG2, "name"));
        final Prop2 key3Prop1 = prop(source2, pi(ORG4, "parent"), pi(ORG3, "name"));
        final Prop2 key1Prop2 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop2 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));
        
        assertEquals(orderBys(
                orderDesc(key1Prop1),
                orderDesc(key2Prop1),
                orderDesc(key3Prop1),
                orderAsc(key1Prop2),
                orderAsc(key2Prop2)), actQry.orderings);
    }
    
    @Test
    public void test008() {
        final ResultQuery2 actQry = qry(select(VEHICLE).as("v1").leftJoin(VEHICLE).as("v2").on().val(1).eq().val(1).model(), orderBy().prop("v1.lastFuelUsage.key").desc().prop("v2.lastFuelUsage.key").asc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE, "v1");
        final QrySource2BasedOnPersistentType source2 = source("2", VEHICLE, "v2");

        final Prop2 key1Prop1 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop1 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));
        final Prop2 key1Prop2 = prop(source2, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop2 = prop(source2, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));
        
        assertEquals(orderBys(
                orderDesc(key1Prop1),
                orderDesc(key2Prop1),
                orderAsc(key1Prop2),
                orderAsc(key2Prop2)), actQry.orderings);
    }

    @Test
    public void test009() {
        final ResultQuery2 actQry = qry(select(VEHICLE_FUEL_USAGE).model(), orderBy().prop("vehicle.key").asc().prop("date").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE_FUEL_USAGE);
        final Prop2 prop1 = prop(source, pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 prop2 = prop(source, pi(VEHICLE_FUEL_USAGE, "date"));
        
        assertEquals(orderBys(orderAsc(prop1), orderDesc(prop2)), actQry.orderings);
    }
    
    @Test
    public void test010() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().prop("lastFuelUsageQty").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);

        final Prop2 prop = prop(source, pi(VEHICLE, "lastFuelUsageQty"));
        
        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }
    
    @Test
    public void test012() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().prop("finDetails.key").desc().model());
        
        final QrySource2BasedOnPersistentType source = source("1", VEHICLE);

        final Prop2 prop = prop(source, pi(VEHICLE, "finDetails"), pi(VEHICLE_FIN_DETAILS, "key"), pi(VEHICLE, "key"));
        
        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

}
