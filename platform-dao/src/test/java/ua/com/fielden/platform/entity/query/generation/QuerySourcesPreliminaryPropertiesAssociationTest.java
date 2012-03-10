package ua.com.fielden.platform.entity.query.generation;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesPreliminaryPropertiesAssociationTest extends BaseEntQueryTCase {

    private final String incP2S = "Inccorect association between properties and query sources";
    private final String incFP2S = "Inccorect association between properties and query sources";

    @Test
    public void test0() {
	final EntityResultQueryModel<TgOrgUnit5> shortcutQry  = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").yield().prop("station").modelAsEntity(ORG5);
	final EntQuery entQry = entResultQry(shortcutQry);

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("v.station", "v", ppi("station", ORG5, H_LONG, true)), //
		propResInf("station", null, ppi("station", ORG5, H_LONG, true)));

	final List<PropResolutionInfo> src2FinProps = prepare( //
		propResInf("v.station.id", "v.station", ppi("id", LONG, H_LONG, true)), //
		propResInf("v.station.key", "v.station", ppi("key", STRING, H_STRING, true)));
	assertEquals(incP2S, compose(src1FinProps, src2FinProps), getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test1() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").model();
	final PrimitiveResultQueryModel shortcutQry = select(sourceQry). //
	where().prop("model.make.key").eq().val("MERC").yield().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(shortcutQry);
	final List<PropResolutionInfo> src1Props = prepare( //
		propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)), //
		propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2FinProps = prepare(  //
		propResInf("model.id", "model", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make", "model", ppi("make", MAKE, H_LONG, true)));
	final List<PropResolutionInfo> src3FinProps = prepare(  //
		propResInf("model.make.id", "model.make", ppi("id", LONG, H_LONG, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)));
	assertEquals(incFP2S, compose(src1FinProps, src2FinProps, src3FinProps),
		getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test2() {
	final PrimitiveResultQueryModel shortcutQry  = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").yield().prop("v.station.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(shortcutQry);
	final List<PropResolutionInfo> src1Props = prepare( //
		propResInf("v.station.key", "v", ppi("station.key", STRING, H_STRING, true), ppi("station", ORG5, H_LONG, true)), //
		propResInf("v.station.key", "v", ppi("station.key", STRING, H_STRING, true), ppi("station", ORG5, H_LONG, true)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("v.station", "v", ppi("station", ORG5, H_LONG, true)));
	final List<PropResolutionInfo> src2FinProps = prepare(  //
		propResInf("v.station.id", "v.station", ppi("id", LONG, H_LONG, true)), //
		propResInf("v.station.key", "v.station", ppi("key", STRING, H_STRING, true)), //
		propResInf("v.station.key", "v.station", ppi("key", STRING, H_STRING, true)));
	assertEquals(incFP2S, compose(src1FinProps, src2FinProps),
		getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test3() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( //
		propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)), //
		propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2FinProps = prepare(  //
		propResInf("model.id", "model", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make", "model", ppi("make", MAKE, H_LONG, true)));
	final List<PropResolutionInfo> src3FinProps = prepare(  //
		propResInf("model.make.id", "model.make", ppi("id", LONG, H_LONG, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)));
	assertEquals(incFP2S, compose(src1FinProps, src2FinProps, src3FinProps),
		getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test4() {
	final PrimitiveResultQueryModel qry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("model.id", "model", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make.key", "model", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)), //
		propResInf("model.make.key", "model", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)));
	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test5() {
	final PrimitiveResultQueryModel qry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("model.id", "model", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make", "model", ppi("make", MAKE, H_LONG, true)));
	final List<PropResolutionInfo> src3Props = prepare( propResInf("model.make.id", "model.make", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, false)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, false)));
	assertEquals(incP2S, compose(src1Props, src2Props, src3Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test6() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
	join(MODEL).as("m").on().prop("model").eq().prop("m.id"). //
	where().prop("m.make.key").eq().val("MERC").and().prop("v.model.make.key").like().val("MERC%").yield().maxOf().prop("m.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)), //
		propResInf("v.model.make.key", "v", ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("m.id", "m", ppi("id", LONG, H_LONG, false)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)));

	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test7() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
	join(MODEL).as("m").on().prop("model").eq().prop("m"). //
	where().prop("m.make.key").eq().val("MERC").yield().maxOf().prop("m.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2Props = prepare( impIdPropResInf("m", "m", ppi("id", LONG, H_LONG, false)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)));
	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test8() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
	join(MODEL).as("m").on().prop("model").eq().prop("m.id"). //
	where().prop("m.make.key").eq().val("MERC").yield().maxOf().prop("m.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("m.id", "m", ppi("id", LONG, H_LONG, false)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)), //
		propResInf("m.make.key", "m", ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)));
	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test9() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).groupBy().prop("model"). //
		yield().prop("model").as("model"). //
		yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
		modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)), //
		propResInf("earliestInitYear", null, ppi("earliestInitYear", null, null, true)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test10() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("key.make.key").eq().val("MERC").and().prop("count").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("key.make.key", null, ppi("key.make.key", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)), //
		propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
    }

    // also think of the case when SE is declaring prop as nullable, but according to the model behind it appears to be not-nullable

    @Test
    public void test11() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();

	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel qry = select(sourceQry).
	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
	where(). //
	prop("model.make.key").eq().val("MERC").and(). //
	prop("mc.key.make.key").eq().val("MERC").and(). //
	prop("earliestInitYear").ge().val(2000).and(). //
	prop("count").ge().val(25).modelAsAggregate();

	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("model", null, ppi("model", MODEL, H_LONG, false)), //
		propResInf("model.make.key", null, ppi("model.make.key", STRING, H_STRING, true), ppi("model", MODEL, H_LONG, false)), //
		propResInf("earliestInitYear", null, ppi("earliestInitYear", null, null, true)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("mc.key", "mc", ppi("key", MODEL, H_LONG, false)), //
		propResInf("mc.key.make.key", "mc", ppi("key.make.key", STRING, H_STRING, true), ppi("key", MODEL, H_LONG, false)), //
		propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));
	assertEquals(incP2S, compose(src1Props, src2Props), getSourcesReferencingProps(entQry));

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model", null, ppi("model", MODEL, H_LONG, false)), //
		propResInf("model", null, ppi("model", MODEL, H_LONG, false)), //
		propResInf("earliestInitYear", null, ppi("earliestInitYear", null, null, true)));
	final List<PropResolutionInfo> src2FinProps = prepare(//
		propResInf("model.id", "model", ppi("id", LONG, H_LONG, false)), //
		propResInf("model.make", "model", ppi("make", MAKE, H_LONG, true)));
	final List<PropResolutionInfo> src3FinProps = prepare( //
		propResInf("model.make.id", "model.make", ppi("id", LONG, H_LONG, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)));

	final List<PropResolutionInfo> src4FinProps = prepare( //
		propResInf("mc.key", "mc", ppi("key", MODEL, H_LONG, false)), //
		propResInf("mc.key", "mc", ppi("key", MODEL, H_LONG, false)), //
		propResInf("count", null, ppi("count", BIG_INTEGER, H_BIG_INTEGER, true)));

	final List<PropResolutionInfo> src5FinProps = prepare( //
		propResInf("mc.key.id", "mc.key", ppi("id", LONG, H_LONG, false)), //
		propResInf("mc.key.make", "mc.key", ppi("make", MAKE, H_LONG, true)));
	final List<PropResolutionInfo> src6FinProps = prepare( //
		propResInf("mc.key.make.id", "mc.key.make", ppi("id", LONG, H_LONG, true)), //
		propResInf("mc.key.make.key", "mc.key.make", ppi("key", STRING, H_STRING, true)));
	assertEquals(incFP2S, compose(src1FinProps, src4FinProps, src2FinProps, src3FinProps, src5FinProps, src6FinProps), getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test12() {
	final AggregatedResultQueryModel sourceQry1 = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();

	final EntityResultQueryModel<TgModelCount> sourceQry2 = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel sourceQry3 = select(sourceQry1).
	join(sourceQry2).as("mc").on().prop("model").eq().prop("mc.key").
	where(). //
	prop("model.make.key").eq().val("MERC").and(). //
	prop("earliestInitYear").ge().val(2000).and(). //
	prop("count").ge().val(25). //
	groupBy().prop("model.make"). //
	yield().prop("model.make").as("make"). //
	yield().minOf().prop("earliestInitYear").as("earliestInitYearPerMake"). //
	yield().sumOf().prop("count").as("totalVehCount"). //
	modelAsAggregate();

	final AggregatedResultQueryModel qry = select(sourceQry3). //
	where().prop("totalVehCount").lt().val(100). //
	yield().prop("make.key").as("a1"). //
	yield().prop("earliestInitYearPerMake").as("a2"). //
	yield().prop("totalVehCount").as("a3"). //
	modelAsAggregate();

	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( //
		propResInf("totalVehCount", null, ppi("totalVehCount", null, null, true)), //
		propResInf("make.key", null, ppi("make.key", STRING, H_STRING, true), ppi("make", MAKE, H_LONG, true)), //
		propResInf("earliestInitYearPerMake", null, ppi("earliestInitYearPerMake", null, null, true)), //
		propResInf("totalVehCount", null, ppi("totalVehCount", null, null, true)));
	assertEquals(incP2S, compose(src1Props), getSourcesReferencingProps(entQry));
    }

    @Test
    public void test13() {
	//select(OrgUnit5.class).where("parent.parent.parent.parent.key").eq().val("NORTH");
	final PrimitiveResultQueryModel qry = select(ORG5). //
	join(ORG4).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(ORG3).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(ORG2).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(ORG1).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").yield().prop("parent.parent.parent.parent.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
	final List<PropResolutionInfo> src1Props = prepare( propResInf("parent", null, ppi("parent", ORG4, H_LONG, true)));
	final List<PropResolutionInfo> src2Props = prepare( propResInf("parent.id", "parent", ppi("id", LONG, H_LONG, false)), //
		propResInf("parent.parent", "parent", ppi("parent", ORG3, H_LONG, true)));
	final List<PropResolutionInfo> src3Props = prepare( propResInf("parent.parent.id", "parent.parent", ppi("id", LONG, H_LONG, false)), //
		propResInf("parent.parent.parent", "parent.parent", ppi("parent", ORG2, H_LONG, true)));
	final List<PropResolutionInfo> src4Props = prepare( propResInf("parent.parent.parent.id", "parent.parent.parent", ppi("id", LONG, H_LONG, false)), //
		propResInf("parent.parent.parent.parent", "parent.parent.parent", ppi("parent", ORG1, H_LONG, true)));
	final List<PropResolutionInfo> src5Props = prepare( propResInf("parent.parent.parent.parent.id", "parent.parent.parent.parent", ppi("id", LONG, H_LONG, false)), //
		propResInf("parent.parent.parent.parent.key", "parent.parent.parent.parent", ppi("key", STRING, H_STRING, false)), //
		propResInf("parent.parent.parent.parent.key", "parent.parent.parent.parent", ppi("key", STRING, H_STRING, false)));
	assertEquals(incP2S, compose(src1Props, src2Props, src3Props, src4Props, src5Props), getSourcesReferencingProps(entQry));
    }

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