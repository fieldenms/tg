package ua.com.fielden.platform.entity.query.generation;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
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

    private static PropResolutionInfo pri(final EntProp entProp, final String aliasPart, final String propPart, final boolean implicitId, final Class propType, final String explicitPropPart) {
	return new PropResolutionInfo(entProp, aliasPart, propPart, implicitId, propType, explicitPropPart);
    }

    @Test
    public void test1() {
	final QueryModel qry = select(VEHICLE).model();

	assertPropInfoEquals(qry, "id", pri(prop("id"), null, "id", false, LONG, "id"));
	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "desc", pri(prop("desc"), null, "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test2() {
	final QueryModel qry = select(select(VEHICLE).model()).model();
	assertPropInfoEquals(qry, "id", pri(prop("id"), null, "id", false, LONG, "id"));
	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "desc", pri(prop("desc"), null, "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test3() {
	final QueryModel qry = select(VEHICLE).as("v").model();
	assertPropInfoEquals(qry, "id", pri(prop("id"), null, "id", false, LONG, "id"));
	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "desc", pri(prop("desc"), null, "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test4() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	assertPropInfoEquals(qry, "id", pri(prop("id"), null, "id", false, LONG, "id"));
	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "desc", pri(prop("desc"), null, "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test5() {
	final QueryModel qry = select(VEHICLE).as("v").model();

	assertPropInfoEquals(qry, "v", pri(prop("v"), "v", null, true, LONG, ""));
	assertPropInfoEquals(qry, "v.key", pri(prop("v.key"), "v", "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "v.desc", pri(prop("v.desc"), "v", "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "v.model", pri(prop("v.model"), "v", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "v.model.desc", pri(prop("v.model.desc"), "v", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "v.model.make.desc", pri(prop("v.model.make.desc"), "v", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test6() {
	final QueryModel qry = select(select(VEHICLE).model()).as("v").model();

	assertPropInfoEquals(qry, "v", pri(prop("v"), "v", null, true, LONG, ""));
	assertPropInfoEquals(qry, "v.key", pri(prop("v.key"), "v", "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "v.desc", pri(prop("v.desc"), "v", "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "v.model", pri(prop("v.model"), "v", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "v.model.desc", pri(prop("v.model.desc"), "v", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "v.model.make.desc", pri(prop("v.model.make.desc"), "v", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test7() {
	final QueryModel qry = select(VEHICLE).as("model").model();

	try {
	    assertPropInfoEquals(qry, "model", pri(prop("model"), "model", null, true, LONG, ""));
	    fail("Should have failed!");
	} catch (final AssertionError e) {
	}
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.key", pri(prop("model.key"), "model", "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), "model", "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model.model", pri(prop("model.model"), "model", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.model.desc", pri(prop("model.model.desc"), "model", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.model.make.desc", pri(prop("model.model.make.desc"), "model", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test8() {
	final QueryModel qry = select(select(VEHICLE).model()).as("model").model();

	assertPropInfoDifferent(qry, "model", pri(prop("model"), "model", null, true, LONG, ""));

	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.key", pri(prop("model.key"), "model", "key", false, STRING, "key"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), "model", "desc", false, STRING, "desc"));
	assertPropInfoEquals(qry, "model.model", pri(prop("model.model"), "model", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.model.desc", pri(prop("model.model.desc"), "model", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.model.make.desc", pri(prop("model.model.make.desc"), "model", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test9() {
	final QueryModel qry = select(TgModelCount.class).model();

	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, MODEL, "key"));
	assertPropInfoEquals(qry, "count", pri(prop("count"), null, "count", false, BIG_INTEGER, "count"));
	assertPropInfoEquals(qry, "key.desc", pri(prop("key.desc"), null, "key.desc", false, STRING, "key"));
	assertPropInfoEquals(qry, "key.make.desc", pri(prop("key.make.desc"), null, "key.make.desc", false, STRING, "key"));
    }

    @Test
    public void test10() {
	final QueryModel qry = select(select(TgModelCount.class).model()).model();

	assertPropInfoEquals(qry, "key", pri(prop("key"), null, "key", false, MODEL, "key"));
	assertPropInfoEquals(qry, "count", pri(prop("count"), null, "count", false, BIG_INTEGER, "count"));
	assertPropInfoEquals(qry, "key.desc", pri(prop("key.desc"), null, "key.desc", false, STRING, "key"));
	assertPropInfoEquals(qry, "key.make.desc", pri(prop("key.make.desc"), null, "key.make.desc", false, STRING, "key"));
    }

    @Test
    public void test11() {
	final QueryModel qry = select(TgModelCount.class).as("mc").model();

	assertPropInfoEquals(qry, "mc", pri(prop("mc"), "mc", null, true, LONG, ""));
	assertPropInfoEquals(qry, "mc.key", pri(prop("mc.key"), "mc", "key", false, MODEL, "key"));
	assertPropInfoEquals(qry, "mc.count", pri(prop("mc.count"), "mc", "count", false, BIG_INTEGER, "count"));
	assertPropInfoEquals(qry, "mc.key.desc", pri(prop("mc.key.desc"), "mc", "key.desc", false, STRING, "key"));
	assertPropInfoEquals(qry, "mc.key.make.desc", pri(prop("mc.key.make.desc"), "mc", "key.make.desc", false, STRING, "key"));
    }

    @Test
    public void test12() {
	final QueryModel qry = select(select(TgModelCount.class).model()).as("mc").model();

	assertPropInfoEquals(qry, "mc", pri(prop("mc"), "mc", null, true, LONG, ""));
	assertPropInfoEquals(qry, "mc.key", pri(prop("mc.key"), "mc", "key", false, MODEL, "key"));
	assertPropInfoEquals(qry, "mc.count", pri(prop("mc.count"), "mc", "count", false, BIG_INTEGER, "count"));
	assertPropInfoEquals(qry, "mc.key.desc", pri(prop("mc.key.desc"), "mc", "key.desc", false, STRING, "key"));
	assertPropInfoEquals(qry, "mc.key.make.desc", pri(prop("mc.key.make.desc"), "mc", "key.make.desc", false, STRING, "key"));
    }

    @Test
    public void test13() {
	final QueryModel qry = select(TgModelYearCount.class).model();

	assertPropInfoEquals(qry, "year", pri(prop("year"), null, "year", false, LONG, "year"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "count", pri(prop("count"), null, "count", false, BIG_INTEGER, "count"));

	assertPropInfoEquals(qry, "model.id", pri(prop("model.id"), null, "model.id", false, LONG, "model.id"));
	assertPropInfoEquals(qry, "model.key", pri(prop("model.key"), null, "model.key", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test14() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).model();

	assertPropInfoEquals(qry, "year", pri(prop("year"), null, "year", false, LONG, "year"));
	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "count", pri(prop("count"), null, "count", false, BIG_INTEGER, "count"));

	assertPropInfoEquals(qry, "model.id", pri(prop("model.id"), null, "model.id", false, LONG, "model.id"));
	assertPropInfoEquals(qry, "model.key", pri(prop("model.key"), null, "model.key", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.desc", pri(prop("model.desc"), null, "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "model.make.desc", pri(prop("model.make.desc"), null, "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test15() {
	final QueryModel qry = select(TgModelYearCount.class).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", pri(prop("myc.year"), "myc", "year", false, LONG, "year"));
	assertPropInfoEquals(qry, "myc.model", pri(prop("myc.model"), "myc", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "myc.count", pri(prop("myc.count"), "myc", "count", false, BIG_INTEGER, "count"));

	assertPropInfoEquals(qry, "myc.model.id", pri(prop("myc.model.id"), "myc", "model.id", false, LONG, "model.id"));
	assertPropInfoEquals(qry, "myc.model.key", pri(prop("myc.model.key"), "myc", "model.key", false, STRING, "model"));
	assertPropInfoEquals(qry, "myc.model.desc", pri(prop("myc.model.desc"), "myc", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "myc.model.make.desc", pri(prop("myc.model.make.desc"), "myc", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test16() {
	final QueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").model();

	assertPropInfoEquals(qry, "myc.year", pri(prop("myc.year"), "myc", "year", false, LONG, "year"));
	assertPropInfoEquals(qry, "myc.model", pri(prop("myc.model"), "myc", "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "myc.count", pri(prop("myc.count"), "myc", "count", false, BIG_INTEGER, "count"));

	assertPropInfoEquals(qry, "myc.model.id", pri(prop("myc.model.id"), "myc", "model.id", false, LONG, "model.id"));
	assertPropInfoEquals(qry, "myc.model.key", pri(prop("myc.model.key"), "myc", "model.key", false, STRING, "model"));
	assertPropInfoEquals(qry, "myc.model.desc", pri(prop("myc.model.desc"), "myc", "model.desc", false, STRING, "model"));
	assertPropInfoEquals(qry, "myc.model.make.desc", pri(prop("myc.model.make.desc"), "myc", "model.make.desc", false, STRING, "model"));
    }

    @Test
    public void test17() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();

	assertPropInfoEquals(qry, "model", pri(prop("model"), null, "model", false, MODEL, "model"));
	assertPropInfoEquals(qry, "model.id", pri(prop("model.id"), null, "model.id", false, LONG, "model.id"));
	assertPropInfoEquals(qry, "model.make", pri(prop("model.make"), null, "model.make", false, MAKE, "model"));
    }

    @Test
    @Ignore
    public void test18() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
		groupBy().prop("model").
		yield().prop("model").as("model"). //
		yield().minOf().yearOf().prop("initDate").as("aka.earliestInitYear"). //
		modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("aka.earliestInitYear").ge().val(2000).modelAsAggregate();
	entResultQry(qry);
    }

    @Test
    @Ignore
    public void test19() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("my.model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("my.model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	entResultQry(qry);
    }
}