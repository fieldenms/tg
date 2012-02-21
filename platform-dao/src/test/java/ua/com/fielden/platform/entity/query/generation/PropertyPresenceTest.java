package ua.com.fielden.platform.entity.query.generation;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.IEntQuerySource;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    private IEntQuerySource getMainSource(final QueryModel qry) {
	return entResultQry(qry).getSources().getMain();
    }

    @Test
    public void test1() {
	final QueryModel qry = select(VEHICLE).model();

	assertPropInfoEquals(qry, "id", propResInf("id", null, "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test2() {
	final QueryModel qry = select(select(VEHICLE).model()).model();
	assertPropInfoEquals(qry, "id", propResInf("id", null, "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test3() {
	final QueryModel qry = select(VEHICLE).as("v").model();
	assertPropInfoEquals(qry, "id", propResInf("id", null, "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test4() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	assertPropInfoEquals(qry, "id", propResInf("id", null, "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "desc", propResInf("desc", null, "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test5() {
	final QueryModel qry = select(VEHICLE).as("v").model();

	assertPropInfoEquals(qry, "v", impIdPropResInf("v", "v", "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "v.key", propResInf("v.key", "v", "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "v.desc", propResInf("v.desc", "v", "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "v.model", propResInf("v.model", "v", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "v.model.desc", propResInf("v.model.desc", "v", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "v.model.make.desc", propResInf("v.model.make.desc", "v", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test6() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	//assertPropInfoEquals(qry, "v", propResInf("v", "v", null, LONG, "", null));
	assertPropInfoEquals(qry, "v", impIdPropResInf("v", "v", "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "v.key", propResInf("v.key", "v", "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "v.desc", propResInf("v.desc", "v", "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "v.model", propResInf("v.model", "v", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "v.model.desc", propResInf("v.model.desc", "v", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "v.model.make.desc", propResInf("v.model.make.desc", "v", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test7() {
	final QueryModel qry = select(VEHICLE).as("model").model();

	try {
	    assertPropInfoEquals(qry, "model", propResInf("model", "model", null, LONG, "", null));
	    fail("Should have failed!");
	} catch (final AssertionError e) {
	}
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", "model", "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", "model", "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model.model", propResInf("model.model", "model", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.model.desc", propResInf("model.model.desc", "model", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.model.make.desc", propResInf("model.model.make.desc", "model", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test8() {
	final QueryModel qry = select(select(VEHICLE).model()).as("model").model();

	assertPropInfoDifferent(qry, "model", propResInf("model", "model", null, LONG, "", null));

	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", "model", "key", STRING, "key", STRING));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", "model", "desc", STRING, "desc", STRING));
	assertPropInfoEquals(qry, "model.model", propResInf("model.model", "model", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.model.desc", propResInf("model.model.desc", "model", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.model.make.desc", propResInf("model.model.make.desc", "model", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test9() {
	final QueryModel qry = select(TgModelCount.class).model();

	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", MODEL, "key", MODEL));
	assertPropInfoEquals(qry, "count", propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));
	assertPropInfoEquals(qry, "key.desc", propResInf("key.desc", null, "key.desc", STRING, "key", MODEL));
	assertPropInfoEquals(qry, "key.make.desc", propResInf("key.make.desc", null, "key.make.desc", STRING, "key", MODEL));
    }

    @Test
    public void test10() {
	final QueryModel qry = select(select(TgModelCount.class).model()).model();

	assertPropInfoEquals(qry, "key", propResInf("key", null, "key", MODEL, "key", MODEL));
	assertPropInfoEquals(qry, "count", propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));
	assertPropInfoEquals(qry, "key.desc", propResInf("key.desc", null, "key.desc", STRING, "key", MODEL));
	assertPropInfoEquals(qry, "key.make.desc", propResInf("key.make.desc", null, "key.make.desc", STRING, "key", MODEL));
    }

    @Test
    public void test11() {
	final QueryModel qry = select(TgModelCount.class).as("mc").model();

	//assertPropInfoEquals(qry, "mc", propResInf("mc", "mc", null, LONG, "", null));
	assertPropInfoEquals(qry, "mc", impIdPropResInf("mc", "mc", "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "mc.key", propResInf("mc.key", "mc", "key", MODEL, "key", MODEL));
	assertPropInfoEquals(qry, "mc.count", propResInf("mc.count", "mc", "count", BIG_INTEGER, "count", BIG_INTEGER));
	assertPropInfoEquals(qry, "mc.key.desc", propResInf("mc.key.desc", "mc", "key.desc", STRING, "key", MODEL));
	assertPropInfoEquals(qry, "mc.key.make.desc", propResInf("mc.key.make.desc", "mc", "key.make.desc", STRING, "key", MODEL));
    }

    @Test
    public void test12() {
	final QueryModel qry = select(select(TgModelCount.class).model()).as("mc").model();

	//assertPropInfoEquals(qry, "mc", propResInf("mc", "mc", null, LONG, "", null));
	assertPropInfoEquals(qry, "mc", impIdPropResInf("mc", "mc", "id", LONG, "id", LONG));
	assertPropInfoEquals(qry, "mc.key", propResInf("mc.key", "mc", "key", MODEL, "key", MODEL));
	assertPropInfoEquals(qry, "mc.count", propResInf("mc.count", "mc", "count", BIG_INTEGER, "count", BIG_INTEGER));
	assertPropInfoEquals(qry, "mc.key.desc", propResInf("mc.key.desc", "mc", "key.desc", STRING, "key", MODEL));
	assertPropInfoEquals(qry, "mc.key.make.desc", propResInf("mc.key.make.desc", "mc", "key.make.desc", STRING, "key", MODEL));
    }

    @Test
    public void test13() {
	final QueryModel qry = select(TgModelYearCount.class).model();

	assertPropInfoEquals(qry, "year", propResInf("year", null, "year", LONG, "year", LONG));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "count", propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));

	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, "model.id", LONG, "model", MODEL));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", null, "model.key", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test14() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).model();

	assertPropInfoEquals(qry, "year", propResInf("year", null, "year", LONG, "year", LONG));
	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "count", propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));

	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, "model.id", LONG, "model", MODEL));
	assertPropInfoEquals(qry, "model.key", propResInf("model.key", null, "model.key", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.desc", propResInf("model.desc", null, "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "model.make.desc", propResInf("model.make.desc", null, "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test15() {
	final QueryModel qry = select(TgModelYearCount.class).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", propResInf("myc.year", "myc", "year", LONG, "year", LONG));
	assertPropInfoEquals(qry, "myc.model", propResInf("myc.model", "myc", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "myc.count", propResInf("myc.count", "myc", "count", BIG_INTEGER, "count", BIG_INTEGER));

	assertPropInfoEquals(qry, "myc.model.id", propResInf("myc.model.id", "myc", "model.id", LONG, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.key", propResInf("myc.model.key", "myc", "model.key", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.desc", propResInf("myc.model.desc", "myc", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.make.desc", propResInf("myc.model.make.desc", "myc", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test16() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", propResInf("myc.year", "myc", "year", LONG, "year", LONG));
	assertPropInfoEquals(qry, "myc.model", propResInf("myc.model", "myc", "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "myc.count", propResInf("myc.count", "myc", "count", BIG_INTEGER, "count", BIG_INTEGER));

	assertPropInfoEquals(qry, "myc.model.id", propResInf("myc.model.id", "myc", "model.id", LONG, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.key", propResInf("myc.model.key", "myc", "model.key", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.desc", propResInf("myc.model.desc", "myc", "model.desc", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "myc.model.make.desc", propResInf("myc.model.make.desc", "myc", "model.make.desc", STRING, "model", MODEL));
    }

    @Test
    public void test17() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();

	assertPropInfoEquals(qry, "model", propResInf("model", null, "model", MODEL, "model", MODEL));
	assertPropInfoEquals(qry, "model.id", propResInf("model.id", null, "model.id", LONG, "model", MODEL));
	assertPropInfoEquals(qry, "model.make", propResInf("model.make", null, "model.make", MAKE, "model", MODEL));
    }

    @Test
    public void test18() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
		groupBy().prop("model").
		yield().prop("model").as("model"). //
		yield().minOf().yearOf().prop("initDate").as("aka.earliestInitYear"). //
		modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("aka.earliestInitYear").ge().val(2000).modelAsAggregate();
	assertPropInfoEquals(qry, "model.make.key", propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL));
	assertPropInfoEquals(qry, "aka.earliestInitYear", propResInf("aka.earliestInitYear", null, "aka.earliestInitYear", null, "aka.earliestInitYear", null));
    }

    @Test
    public void test19() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("my.model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("my.model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	assertPropInfoEquals(qry, "my.model.make.key", propResInf("my.model.make.key", null, "my.model.make.key", STRING, "my.model", MODEL));
	assertPropInfoEquals(qry, "earliestInitYear", propResInf("earliestInitYear", null, "earliestInitYear", null, "earliestInitYear", null));
    }
}