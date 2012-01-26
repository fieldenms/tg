package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntQueryResultTypeTest extends BaseEntQueryTCase {

    @Test
    public void test1() {
	// TODO this is irrational combination
	final AggregatedResultQueryModel qry = select(VEHICLE).modelAsAggregate();
	assertEquals("Incorrect result type", EntityAggregates.class, entQuery1(qry).getResultType());
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).model();
	assertEquals("Incorrect result type", VEHICLE, entQuery1(qry).getResultType());
    }

    @Test
    public void test3() {
	try {
	    entQuery1(select(TgVehicle.class).modelAsEntity(MODEL));
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).yield().prop("model").modelAsEntity(MODEL);
	assertEquals("Incorrect result type", MODEL, entQuery1(qry).getResultType());
    }

    @Test
    public void test5() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).yield().prop("key").modelAsPrimitive();
	assertEquals("Incorrect result type", null, entQuery1(qry).getResultType());
    }

    @Test
    public void test6() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).yield().prop("key").modelAsPrimitive(String.class);
	assertEquals("Incorrect result type", String.class, entQuery1(qry).getResultType());
    }
}