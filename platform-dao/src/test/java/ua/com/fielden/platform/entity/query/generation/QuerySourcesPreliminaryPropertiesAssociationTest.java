package ua.com.fielden.platform.entity.query.generation;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesPreliminaryPropertiesAssociationTest extends BaseEntQueryTCase {

    private final String incP2S = "Inccorect association between properties and query sources";
    private final String incFP2S = "Inccorect association between properties and query sources";

//    @Test
//    public void test0() {
//	final EntityResultQueryModel<TgOrgUnit5> shortcutQry  = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").yield().prop("station").modelAsEntity(ORG5);
//	final EntQuery entQry = entResultQry(shortcutQry);
//
//	final List<PropResolutionInfo> src1FinProps = prepare( //
//		propResInf("v.station", "v", "station", ORG5, "station", ORG5), //
//		propResInf("station", null, "station", ORG5, "station", ORG5));
//
//	final List<PropResolutionInfo> src2FinProps = prepare( //
//		propResInf("v.station.id", "v.station", "id", LONG, "id", LONG), //
//		propResInf("v.station.key", "v.station", "key", STRING, "key", STRING));
//	assertEquals(incP2S, compose(src1FinProps, src2FinProps), getSourcesFinalReferencingProps(entQry));
//    }
//
//    @Test
//    public void test1() {
//	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").model();
//	final PrimitiveResultQueryModel shortcutQry = select(sourceQry). //
//	where().prop("model.make.key").eq().val("MERC").yield().prop("model.make.key").modelAsPrimitive(STRING);
//	final EntQuery entQry = entResultQry(shortcutQry);
//	final List<PropResolutionInfo> src1Props = prepare( //
//		propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL), //
//		propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//
//	final List<PropResolutionInfo> src1FinProps = prepare( //
//		propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2FinProps = prepare(  //
//		propResInf("model.id", "model", "id", LONG, "id", LONG), //
//		propResInf("model.make", "model", "make", MAKE, "make", MAKE));
//	final List<PropResolutionInfo> src3FinProps = prepare(  //
//		propResInf("model.make.id", "model.make", "id", LONG, "id", LONG), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING));
//	assertEquals(incFP2S, compose(src1FinProps, src2FinProps, src3FinProps),
//		getSourcesFinalReferencingProps(entQry));
//    }
//
//    @Test
//    public void test2() {
//	final PrimitiveResultQueryModel shortcutQry  = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").yield().prop("v.station.key").modelAsPrimitive(STRING);
//	final EntQuery entQry = entResultQry(shortcutQry);
//	final List<PropResolutionInfo> src1Props = prepare( //
//		propResInf("v.station.key", "v", "station.key", STRING, "station", ORG5), //
//		propResInf("v.station.key", "v", "station.key", STRING, "station", ORG5));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//
//	final List<PropResolutionInfo> src1FinProps = prepare( //
//		propResInf("v.station", "v", "station", ORG5, "station", ORG5));
//	final List<PropResolutionInfo> src2FinProps = prepare(  //
//		propResInf("v.station.id", "v.station", "id", LONG, "id", LONG), //
//		propResInf("v.station.key", "v.station", "key", STRING, "key", STRING), //
//		propResInf("v.station.key", "v.station", "key", STRING, "key", STRING));
//	assertEquals(incFP2S, compose(src1FinProps, src2FinProps),
//		getSourcesFinalReferencingProps(entQry));
//    }
//
//    @Test
//    public void test3() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE).where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( //
//		propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL), //
//		propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//
//	final List<PropResolutionInfo> src1FinProps = prepare( //
//		propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2FinProps = prepare(  //
//		propResInf("model.id", "model", "id", LONG, "id", LONG), //
//		propResInf("model.make", "model", "make", MAKE, "make", MAKE));
//	final List<PropResolutionInfo> src3FinProps = prepare(  //
//		propResInf("model.make.id", "model.make", "id", LONG, "id", LONG), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING));
//	assertEquals(incFP2S, compose(src1FinProps, src2FinProps, src3FinProps),
//		getSourcesFinalReferencingProps(entQry));
//    }
//
//    @Test
//    public void test4() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE). //
//	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
//	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("model.id", "model", "id", LONG, "id", LONG), //
//		propResInf("model.make.key", "model", "make.key", STRING, "make", MAKE), //
//		propResInf("model.make.key", "model", "make.key", STRING, "make", MAKE));
//	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test5() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE). //
//	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
//	join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
//	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("model.id", "model", "id", LONG, "id", LONG), //
//		propResInf("model.make", "model", "make", MAKE, "make", MAKE));
//	final List<PropResolutionInfo> src3Props = prepare( propResInf("model.make.id", "model.make", "id", LONG, "id", LONG), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING));
//	assertEquals(incP2S, compose(src1Props, src2Props, src3Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test6() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
//	join(MODEL).as("m").on().prop("model").eq().prop("m.id"). //
//	where().prop("m.make.key").eq().val("MERC").and().prop("v.model.make.key").like().val("MERC%").yield().maxOf().prop("m.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL), //
//		propResInf("v.model.make.key", "v", "model.make.key", STRING, "model",  MODEL));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("m.id", "m", "id", LONG, "id", LONG), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE));
//
//	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test7() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
//	join(MODEL).as("m").on().prop("model").eq().prop("m"). //
//	where().prop("m.make.key").eq().val("MERC").yield().maxOf().prop("m.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2Props = prepare( impIdPropResInf("m", "m", "id", LONG, "id", LONG), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE));
//	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test8() {
//	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
//	join(MODEL).as("m").on().prop("model").eq().prop("m.id"). //
//	where().prop("m.make.key").eq().val("MERC").yield().maxOf().prop("m.make.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("m.id", "m", "id", LONG, "id", LONG), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE), //
//		propResInf("m.make.key", "m", "make.key", STRING, "make", MAKE));
//	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test9() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE).groupBy().prop("model"). //
//		yield().prop("model").as("model"). //
//		yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
//		modelAsAggregate();
//	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL), //
//		propResInf("earliestInitYear", null, "earliestInitYear", null, "earliestInitYear", null));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test10() {
//	final EntityResultQueryModel<TgModelCount> sourceQry = select(VEHICLE). //
//	groupBy().prop("model"). //
//	yield().prop("model").as("key"). //
//	yield().countOf().prop("id").as("count"). //
//	modelAsEntity(TgModelCount.class);
//
//	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("key.make.key").eq().val("MERC").and().prop("count").ge().val(2000).modelAsAggregate();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("key.make.key", null, "key.make.key", STRING, "key", MODEL), //
//		propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test11() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE). //
//	groupBy().prop("model"). //
//	yield().prop("model").as("model"). //
//	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
//	modelAsAggregate();
//
//	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(VEHICLE). //
//	groupBy().prop("model"). //
//	yield().prop("model").as("key"). //
//	yield().countOf().prop("id").as("count"). //
//	modelAsEntity(TgModelCount.class);
//
//	final AggregatedResultQueryModel qry = select(sourceQry).
//	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
//	where(). //
//	prop("model.make.key").eq().val("MERC").and(). //
//	prop("mc.key.make.key").eq().val("MERC").and(). //
//	prop("earliestInitYear").ge().val(2000).and(). //
//	prop("count").ge().val(25).modelAsAggregate();
//
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, "model", MODEL, "model", MODEL), //
//		propResInf("model.make.key", null, "model.make.key", STRING, "model", MODEL), //
//		propResInf("earliestInitYear", null, "earliestInitYear", null, "earliestInitYear", null));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("mc.key", "mc", "key", MODEL, "key", MODEL), //
//		propResInf("mc.key.make.key", "mc", "key.make.key", STRING, "key", MODEL), //
//		propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));
//	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
//
//	final List<PropResolutionInfo> src1FinProps = prepare( //
//		propResInf("model", null, "model", MODEL, "model", MODEL), //
//		propResInf("model", null, "model", MODEL, "model", MODEL), //
//		propResInf("earliestInitYear", null, "earliestInitYear", null, "earliestInitYear", null));
//	final List<PropResolutionInfo> src2FinProps = prepare(//
//		propResInf("model.id", "model", "id", LONG, "id", LONG), //
//		propResInf("model.make", "model", "make", MAKE, "make", MAKE));
//	final List<PropResolutionInfo> src3FinProps = prepare( //
//		propResInf("model.make.id", "model.make", "id", LONG, "id", LONG), //
//		propResInf("model.make.key", "model.make", "key", STRING, "key", STRING));
//
//	final List<PropResolutionInfo> src4FinProps = prepare( //
//		propResInf("mc.key", "mc", "key", MODEL, "key", MODEL), //
//		propResInf("mc.key", "mc", "key", MODEL, "key", MODEL), //
//		propResInf("count", null, "count", BIG_INTEGER, "count", BIG_INTEGER));
//
//	final List<PropResolutionInfo> src5FinProps = prepare( //
//		propResInf("mc.key.id", "mc.key", "id", LONG, "id", LONG), //
//		propResInf("mc.key.make", "mc.key", "make", MAKE, "make", MAKE));
//	final List<PropResolutionInfo> src6FinProps = prepare( //
//		propResInf("mc.key.make.id", "mc.key.make", "id", LONG, "id", LONG), //
//		propResInf("mc.key.make.key", "mc.key.make", "key", STRING, "key", STRING));
//	assertEquals(incFP2S, compose(src1FinProps, src4FinProps, src2FinProps, src3FinProps, src5FinProps, src6FinProps), getSourcesFinalReferencingProps(entQry));
//    }
//
//    @Test
//    public void test12() {
//	final AggregatedResultQueryModel sourceQry1 = select(VEHICLE). //
//	groupBy().prop("model"). //
//	yield().prop("model").as("model"). //
//	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
//	modelAsAggregate();
//
//	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(VEHICLE). //
//	groupBy().prop("model"). //
//	yield().prop("model").as("key"). //
//	yield().countOf().prop("id").as("count"). //
//	modelAsEntity(TgModelCount.class);
//
//	final AggregatedResultQueryModel sourceQry3 = select(sourceQry1).
//	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
//	where(). //
//	prop("model.make.key").eq().val("MERC").and(). //
//	prop("earliestInitYear").ge().val(2000).and(). //
//	prop("count").ge().val(25). //
//	groupBy().prop("model.make"). //
//	yield().prop("model.make").as("make"). //
//	yield().minOf().prop("earliestInitYear").as("earliestInitYearPerMake"). //
//	yield().sumOf().prop("count").as("totalVehCount"). //
//	modelAsAggregate();
//
//	final AggregatedResultQueryModel qry = select(sourceQry3). //
//	where().prop("totalVehCount").lt().val(100). //
//	yield().prop("make.key").as("a1"). //
//	yield().prop("earliestInitYearPerMake").as("a2"). //
//	yield().prop("totalVehCount").as("a3"). //
//	modelAsAggregate();
//
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("totalVehCount", null, "totalVehCount", null, "totalVehCount", null), //
//		propResInf("make.key", null, "make.key", STRING, "make", MAKE), //
//		propResInf("earliestInitYearPerMake", null, "earliestInitYearPerMake", null, "earliestInitYearPerMake", null), //
//		propResInf("totalVehCount", null, "totalVehCount", null, "totalVehCount", null));
//	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
//    }
//
//    @Test
//    public void test13() {
//	//select(OrgUnit5.class).where("parent.parent.parent.parent.key").eq().val("NORTH");
//	final PrimitiveResultQueryModel qry = select(ORG5). //
//	join(ORG4).as("parent").on().prop("parent").eq().prop("parent.id"). //
//	join(ORG3).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
//	join(ORG2).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
//	join(ORG1).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
//	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").yield().prop("parent.parent.parent.parent.key").modelAsPrimitive();
//	final EntQuery entQry = entResultQry(qry);
//	final List<PropResolutionInfo> src1Props = prepare( propResInf("parent", null, "parent", ORG4, "parent", ORG4));
//	final List<PropResolutionInfo> src2Props = prepare( propResInf("parent.id", "parent", "id", LONG, "id", LONG), //
//		propResInf("parent.parent", "parent", "parent", ORG3, "parent", ORG3));
//	final List<PropResolutionInfo> src3Props = prepare( propResInf("parent.parent.id", "parent.parent", "id", LONG, "id", LONG), //
//		propResInf("parent.parent.parent", "parent.parent", "parent", ORG2, "parent", ORG2));
//	final List<PropResolutionInfo> src4Props = prepare( propResInf("parent.parent.parent.id", "parent.parent.parent", "id", LONG, "id", LONG), //
//		propResInf("parent.parent.parent.parent", "parent.parent.parent", "parent", ORG1, "parent", ORG1));
//	final List<PropResolutionInfo> src5Props = prepare( propResInf("parent.parent.parent.parent.id", "parent.parent.parent.parent", "id", LONG, "id", LONG), //
//		propResInf("parent.parent.parent.parent.key", "parent.parent.parent.parent", "key", STRING, "key", STRING), //
//		propResInf("parent.parent.parent.parent.key", "parent.parent.parent.parent", "key", STRING, "key", STRING));
//	assertEquals(incP2S, compose(src1Props, src2Props, src3Props, src4Props, src5Props), getSourcesReferencingProps(entQry));
//    }

    @Test
    public void test_that_ambiguous_property_parent_parent_parent_is_detected() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(ORG5). //
	join(ORG4).on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent").model();
	try {
	    entResultQry(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	    assertEquals("Incorrect exception message", "Ambiguous property: parent.parent.parent", e.getMessage());
	}
    }

    @Test
    public void test_that_ambiguous_property_parent_is_detected() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(ORG5). //
	join(ORG4).on().prop("parent").eq().prop("parent"). //
	join(ORG4).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(ORG3).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(ORG2).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(ORG1).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").model();
	try {
	    entResultQry(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	    assertEquals("Incorrect exception message", "Ambiguous property: parent", e.getMessage());
	}
    }
}