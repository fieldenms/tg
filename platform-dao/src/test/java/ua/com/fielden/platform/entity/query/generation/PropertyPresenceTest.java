package ua.com.fielden.platform.entity.query.generation;

import org.hibernate.Hibernate;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.ISource;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    private ISource getMainSource(final QueryModel qry) {
	return entResultQry(qry).getSources().getMain();
    }

    @Test
    public void test1() {
	final QueryModel qry = select(VEHICLE).model();

	assertPropInfoEquals(qry, "id", propResInf("id", null, ppi("id", LONG, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", STRING, Hibernate.STRING, false)));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, ppi("desc", STRING, Hibernate.STRING, true)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", null, ppi("model.key", STRING, Hibernate.STRING, false), ppi("model", MODEL, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, Hibernate.STRING, true), ppi("model", MODEL, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, Hibernate.STRING, true), ppi("model", MODEL, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, Hibernate.STRING, true), ppi("model", MODEL, Hibernate.LONG, false)));
    }

    @Test
    public void test2() {
	final QueryModel qry = select(select(VEHICLE).model()).model();
	assertPropInfoEquals(qry, "id", propResInf("id", null, ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.key", propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test3() {
	final QueryModel qry = select(VEHICLE).as("v").model();
	assertPropInfoEquals(qry, "id", propResInf("id", null, ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test4() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	assertPropInfoEquals(qry, "id", propResInf("id", null, ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test5() {
	final QueryModel qry = select(VEHICLE).as("v").model();

	assertPropInfoEquals(qry, "v", impIdPropResInf("v", "v", ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "v.key", propResInf("v.key", "v", ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "v.desc", propResInf("v.desc", "v", ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "v.model", propResInf("v.model", "v", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "v.model.desc", propResInf("v.model.desc", "v", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "v.model.make.desc", propResInf("v.model.make.desc", "v", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test6() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	assertPropInfoEquals(qry, "v", impIdPropResInf("v", "v", ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "v.key", propResInf("v.key", "v", ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "v.desc", propResInf("v.desc", "v", ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "v.model", propResInf("v.model", "v", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "v.model.desc", propResInf("v.model.desc", "v", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "v.model.make.desc", propResInf("v.model.make.desc", "v", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test7() {
	final QueryModel qry = select(VEHICLE).as("model").model();

	try {
	    assertPropInfoEquals(qry, "model", propResInf("model", "model", ppi("id", LONG, H_LONG, false)));
	    fail("Should have failed!");
	} catch (final AssertionError e) {
	}
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", "model", ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", "model", ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "model.model", propResInf("model.model", "model", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.model.desc", propResInf("model.model.desc", "model", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.model.make.desc", propResInf("model.model.make.desc", "model", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test8() {
	final QueryModel qry = select(select(VEHICLE).model()).as("model").model();

	assertPropInfoDifferent(qry, "model", propResInf("model", "model", ppi("id", LONG, H_LONG, false)));

	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", "model", ppi("key", STRING, H_STRING, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", "model", ppi("desc", STRING, H_STRING, true)));
	assertPropInfoEquals(qry, "model.model", propResInf("model.model", "model", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.model.desc", propResInf("model.model.desc", "model", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.model.make.desc", propResInf("model.model.make.desc", "model", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test9() {
	final QueryModel qry = select(TgModelCount.class).model();

	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "count", propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertPropInfoEquals(qry, "key.desc", propResInf("key.desc", null, ppi("key.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "key.make.desc", propResInf("key.make.desc", null, ppi("key.make.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
    }

    @Test
    public void test10() {
	final QueryModel qry = select(select(TgModelCount.class).model()).model();

	assertPropInfoEquals(qry, "key", propResInf("key", null, ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "count", propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertPropInfoEquals(qry, "key.desc", propResInf("key.desc", null, ppi("key.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "key.make.desc", propResInf("key.make.desc", null, ppi("key.make.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
    }

    @Test
    public void test11() {
	final QueryModel qry = select(TgModelCount.class).as("mc").model();

	assertPropInfoEquals(qry, "mc", impIdPropResInf("mc", "mc", ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.key", propResInf("mc.key", "mc", ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.count", propResInf("mc.count", "mc", ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertPropInfoEquals(qry, "mc.key.desc", propResInf("mc.key.desc", "mc", ppi("key.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.key.make.desc", propResInf("mc.key.make.desc", "mc", ppi("key.make.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
    }

    @Test
    public void test12() {
	final QueryModel qry = select(select(TgModelCount.class).model()).as("mc").model();

	assertPropInfoEquals(qry, "mc", impIdPropResInf("mc", "mc", ppi("id", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.key", propResInf("mc.key", "mc", ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.count", propResInf("mc.count", "mc", ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertPropInfoEquals(qry, "mc.key.desc", propResInf("mc.key.desc", "mc", ppi("key.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "mc.key.make.desc", propResInf("mc.key.make.desc", "mc", ppi("key.make.desc", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)));
    }

    @Test
    public void test13() {
	final QueryModel qry = select(TgModelYearCount.class).model();

	assertPropInfoEquals(qry, "year", propResInf("year", null, ppi("year", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "count", propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));

	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, ppi("model.id", LONG, H_LONG, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test14() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).model();

	assertPropInfoEquals(qry, "year", propResInf("year", null, ppi("year", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "count", propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));

	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, ppi("model.id", LONG, H_LONG, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test15() {
	final QueryModel qry = select(TgModelYearCount.class).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", propResInf("myc.year", "myc", ppi("year", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model", propResInf("myc.model", "myc", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.count", propResInf("myc.count", "myc", ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));

	assertPropInfoEquals(qry, "myc.model.id", propResInf("myc.model.id", "myc", ppi("model.id", LONG, H_LONG, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.key", propResInf("myc.model.key", "myc", ppi("model.key", STRING, H_STRING, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.desc", propResInf("myc.model.desc", "myc", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.make.desc", propResInf("myc.model.make.desc", "myc", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test16() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", propResInf("myc.year", "myc", ppi("year", LONG, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model", propResInf("myc.model", "myc", ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.count", propResInf("myc.count", "myc", ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));

	assertPropInfoEquals(qry, "myc.model.id", propResInf("myc.model.id", "myc", ppi("model.id", LONG, H_LONG, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.key", propResInf("myc.model.key", "myc", ppi("model.key", STRING, H_STRING, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.desc", propResInf("myc.model.desc", "myc", ppi("model.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "myc.model.make.desc", propResInf("myc.model.make.desc", "myc", ppi("model.make.desc", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test17() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();

	assertPropInfoEquals(qry, "model", propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, ppi("model.id", LONG, H_LONG, false), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "model.make", propResInf("model.make", null, ppi("model.make", MAKE, H_LONG, true), ppi("model", MODEL, H_LONG, false)));
    }

    @Test
    public void test18() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
		groupBy().prop("model").
		yield().prop("model").as("model"). //
		yield().minOf().yearOf().prop("initDate").as("aka.earliestInitYear"). //
		modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("aka.earliestInitYear").ge().val(2000).modelAsAggregate();
	assertPropInfoEquals(qry, "model.make.key", propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "aka.earliestInitYear", propResInf("aka.earliestInitYear", null, ppi("aka.earliestInitYear", null, null, true)));
    }

    @Test
    public void test19() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("my.model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("my.model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	assertPropInfoEquals(qry, "my.model.make.key", propResInf("my.model.make.key", null, ppi("my.model.make.key", STRING, H_STRING, true), ppi("my.model", MODEL, H_LONG, false)));
	assertPropInfoEquals(qry, "earliestInitYear", propResInf("earliestInitYear", null, ppi("earliestInitYear", null, null, true)));
    }

    @Test
    public void test20() {
	final QueryModel qry = select(FUEL_USAGE).model();

	assertPropInfoEquals(qry, "id", propResInf("id", null, ppi("id", LONG, Hibernate.LONG, false)));
	assertPropInfoEquals(qry, "vehicle", propResInf("vehicle", null, ppi("vehicle", VEHICLE, H_LONG, false)));
	assertPropInfoEquals(qry, "vehicle.model", propResInf("vehicle.model", null, ppi("vehicle.model", MODEL, H_LONG, false), ppi("vehicle", VEHICLE, H_LONG, false)));
	assertPropInfoEquals(qry, "vehicle.model.key", propResInf("vehicle.model.key", null, ppi("vehicle.model.key", STRING, H_STRING, false), ppi("vehicle", VEHICLE, H_LONG, false)));
	assertPropInfoEquals(qry, "vehicle.model.make", propResInf("vehicle.model.make", null, ppi("vehicle.model.make", MAKE, H_LONG, true), ppi("vehicle", VEHICLE, H_LONG, false)));
//	assertPropInfoEquals(qry, "date", propResInf("date", null, ppi("date", DATE, H_LONG, false)));
    }

}