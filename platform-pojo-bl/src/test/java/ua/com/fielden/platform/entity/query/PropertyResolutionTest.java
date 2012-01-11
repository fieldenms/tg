package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.utils.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyResolutionTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);


    @Test
    @Ignore
    public void test_prop0() {
	final PrimitiveResultQueryModel qry = select(select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().val("MERC").yield().prop("model.key").as("vehicleModelKey").modelAsAggregate()).where().prop("vehicleModelKey").eq().val("MERC1").yield().prop("vehicleModelKey").modelAsPrimitive(String.class);
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop1() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop2() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").where().anyOfProps("model", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop3() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").where().anyOfProps("v.model", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop4() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").where().anyOfProps("v.model.make.key", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop5() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").where().anyOfProps("v.model.make.kay", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	exp.add(new Pair<EntQuery, String>(entQry, "v.model.make.kay"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop6() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v").leftJoin(TgVehicleModel.class).on().prop("v.model").eq().prop("id").where().anyOfProps("v.model.make.kay", "v.eqClass").eq().val("MERC").model();
	try {
	    qb.generateEntQuery(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_prop7() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgVehicleModel.class).as("m").on().prop("model").eq().prop("m.id").where().anyOfProps("model.make.key", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop8() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("price.amount").ge().val(1000).model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop9() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("price.currency").eq().val("AUD").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop10() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).as("v.ehicle").where().anyOfProps("v.ehicle.model.make.key", "v.eqClass").eq().val("MERC").model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	exp.add(new Pair<EntQuery, String>(entQry, "v.eqClass"));
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }

    @Test
    public void test_prop11() {
	final EntityResultQueryModel<TgVehicle> subQry = select(TgVehicle.class).leftJoin(TgVehicleModel.class).as("m").on().prop("model").eq().prop("m.id").where().anyOfProps("model.make.key", "v.key").eq().val("MERC").model();
	final EntityResultQueryModel<TgModelCount> qry = select(TgModelCount.class).as("v").where().exists(subQry).model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	final List<Pair<EntQuery, String>> exp = new ArrayList<Pair<EntQuery, String>>();
	assertEquals("Incorrect list of unresolved props", exp, entQry.getUnresolvedProps());
    }
}