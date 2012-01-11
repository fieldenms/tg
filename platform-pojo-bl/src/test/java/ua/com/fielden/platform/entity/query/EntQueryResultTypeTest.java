package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntQueryResultTypeTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test1() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).modelAsAggregate();
	assertEquals("Incorrect result type", EntityAggregates.class, qb.generateEntQuery(qry).getResultType());
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
	assertEquals("Incorrect result type", TgVehicle.class, qb.generateEntQuery(qry).getResultType());
    }

    @Test
    public void test3() {
	//TODO this is invalid query - should throw exception
	final EntityResultQueryModel<TgVehicleModel> qry = select(TgVehicle.class).modelAsEntity(TgVehicleModel.class);
	assertEquals("Incorrect result type", TgVehicleModel.class, qb.generateEntQuery(qry).getResultType());
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(TgVehicle.class).yield().prop("model").modelAsEntity(TgVehicleModel.class);
	assertEquals("Incorrect result type", TgVehicleModel.class, qb.generateEntQuery(qry).getResultType());
    }

    @Test
    public void test5() {
	System.out.println("KEY");
	final PrimitiveResultQueryModel qry = select(TgVehicle.class).yield().prop("key").modelAsPrimitive();
	assertEquals("Incorrect result type", null, qb.generateEntQuery(qry).getResultType());
    }

    @Test
    public void test6() {
	System.out.println("KEY");
	final PrimitiveResultQueryModel qry = select(TgVehicle.class).yield().prop("key").modelAsPrimitive(String.class);
	assertEquals("Incorrect result type", String.class, qb.generateEntQuery(qry).getResultType());
    }

}