package ua.com.fielden.platform.entity.query;

import java.util.Arrays;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PropertyResolutionTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test_prop1() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "eqClass" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop2() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().anyOfProps("model", "v.eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "v.eqClass" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop3() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().anyOfProps("v.model", "v.eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "v.eqClass" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop4() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().anyOfProps("v.model.make.key", "v.eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "v.eqClass" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop5() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().anyOfProps("v.model.make.kay", "v.eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "v.eqClass", "v.model.make.kay" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop6() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").leftJoin(TgVehicleModel.class).on().prop("v.model").eq().prop("id").where().anyOfProps("v.model.make.kay", "v.eqClass").eq().val("MERC").model();
	try {
	    qb.generateEntQuery(qry).resolveProps();
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_prop7() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).leftJoin(TgVehicleModel.class).as("m").on().prop("model").eq().prop("m.id").where().anyOfProps("model.make.key", "v.eqClass").eq().val("MERC").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] { "v.eqClass" }), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop8() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("price.amount").ge().val(1000).model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] {}), qb.generateEntQuery(qry).resolveProps());
    }

    @Test
    public void test_prop9() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("price.currency").eq().val("AUD").model();
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new String[] {}), qb.generateEntQuery(qry).resolveProps());
    }


}