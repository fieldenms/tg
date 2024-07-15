package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1.ERR_CANNOT_FIND_YIELD_FOR_ORDER_BY;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class OrderByYieldEnhancementTest extends EqlStage2TestCase {

    @Test
    public void test001() {
        final ResultQuery2 actQry = qry(select(MODEL).model(), orderBy().yield("key").desc().model());

        final Source2BasedOnPersistentType source = source(1, MODEL);
        final Prop2 keyProp = prop(source, pi(MODEL, "key"));

        assertEquals(orderBys(orderDesc(keyProp)), actQry.orderings);
    }

    @Test
    public void test002() {
        final ResultQuery2 actQry = qry(select(ORG2).model(), orderBy().yield("key").desc().model());

        final Source2BasedOnPersistentType source = source(1, ORG2);
        final Prop2 key1Prop = prop(source, pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop = prop(source, pi(ORG2, "name"));

        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test003() {
        final ResultQuery2 actQry = qry(select(ORG2).as("o2").model(), orderBy().yield("o2.key").desc().model());

        final Source2BasedOnPersistentType source = source(1, ORG2, "o2");
        final Prop2 key1Prop = prop(source, pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop = prop(source, pi(ORG2, "name"));

        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test004() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().yield("purchasePrice").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);
        final Prop2 prop = prop(source, pi(VEHICLE, "purchasePrice"), pi(VEHICLE, "purchasePrice", "amount"));

        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

    @Test
    public void test005() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().yield("purchasePrice.amount").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);
        final Prop2 prop = prop(source, pi(VEHICLE, "purchasePrice"), pi(VEHICLE, "purchasePrice", "amount"));

        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

    @Test
    public void test006() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().yield("lastFuelUsage.key").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);
        final Prop2 key1Prop = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));

        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void test007() {
        final ResultQuery2 actQry = qry(select(VEHICLE).leftJoin(ORG4).on().val(1).eq().val(1).model(), orderBy().yield("lastFuelUsage.key").asc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);

        final Prop2 key1Prop2 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop2 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));

        assertEquals(orderBys(
                orderAsc(key1Prop2),
                orderAsc(key2Prop2)), actQry.orderings);
    }

    @Test
    public void test008() {
        final ResultQuery2 actQry = qry(select(VEHICLE).as("v1").leftJoin(VEHICLE).as("v2").on().val(1).eq().val(1).model(), orderBy().yield("v1.lastFuelUsage.key").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE, "v1");

        final Prop2 key1Prop1 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 key2Prop1 = prop(source, pi(VEHICLE, "lastFuelUsage"), pi(VEHICLE_FUEL_USAGE, "date"));

        assertEquals(orderBys(
                orderDesc(key1Prop1),
                orderDesc(key2Prop1)), actQry.orderings);
    }

    @Test
    public void test009() {
        final ResultQuery2 actQry = qry(select(VEHICLE_FUEL_USAGE).model(), orderBy().yield("vehicle.key").asc().yield("date").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE_FUEL_USAGE);
        final Prop2 prop1 = prop(source, pi(VEHICLE_FUEL_USAGE, "vehicle"), pi(VEHICLE, "key"));
        final Prop2 prop2 = prop(source, pi(VEHICLE_FUEL_USAGE, "date"));

        assertEquals(orderBys(orderAsc(prop1), orderDesc(prop2)), actQry.orderings);
    }

    @Test
    public void test010() {
        final ResultQuery2 actQry = qry(select(VEHICLE).yield().prop("lastFuelUsageQty").as("lfuq").modelAsAggregate(), orderBy().yield("lfuq").desc().model());

        assertEquals(orderBys(orderDesc("lfuq")), actQry.orderings);
    }

    @Test
    public void test011() {
        final ResultQuery2 actQry = qry(select(VEHICLE).model(), orderBy().yield("lastFuelUsageQty").desc().model());

        final Source2BasedOnPersistentType source = source(1, VEHICLE);

        final Prop2 prop = prop(source, pi(VEHICLE, "lastFuelUsageQty"));

        assertEquals(orderBys(orderDesc(prop)), actQry.orderings);
        //assertNotEquals(orderBys(orderDesc(prop)), actQry.orderings);
    }

    @Test
    public void test012() {
        final ResultQuery2 actQry = qry(select(ORG2).yield().prop("key").as("k").modelAsAggregate(), orderBy().yield("k").desc().model());

        final Source2BasedOnPersistentType source = source(1, ORG2);
        final Prop2 key1Prop = prop(source, pi(ORG2, "parent"), pi(ORG1, "key"));
        final Prop2 key2Prop = prop(source, pi(ORG2, "name"));

        assertEquals(orderBys(orderDesc(key1Prop), orderDesc(key2Prop)), actQry.orderings);
    }

    @Test
    public void ordering_by_non_existing_yield_throws_exception() {
        try {
            qry(select(MODEL).yield().prop("key").as("k").modelAsAggregate(), orderBy().yield("k1").desc().model());
            fail("Should have failed while trying to order by non-existing yield [k1]");
        } catch (final EqlException e) {
            assertEquals(ERR_CANNOT_FIND_YIELD_FOR_ORDER_BY.formatted("k1"), e.getMessage());
        }
    }
}