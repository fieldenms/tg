package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesPreliminaryPropertiesAssociationTest extends BaseEntQueryTCase {

    @Test
    public void test_prop_to_source_association1() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("model.make.key"), prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association2() {
	final PrimitiveResultQueryModel qry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("model.id"), prop("model.make.key"), prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association3() {
	final PrimitiveResultQueryModel qry = select(VEHICLE). //
	join(MODEL).as("model").on().prop("model").eq().prop("model.id"). //
	join(MAKE).as("model.make").on().prop("model.make").eq().prop("model.make.id"). //
	where().prop("model.make.key").eq().val("MERC").yield().maxOf().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("model.id"), prop("model.make") });
	final List<EntProp> qrySource3props = Arrays.asList(new EntProp[] { prop("model.make.id"), prop("model.make.key"), prop("model.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props, qrySource3props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association4() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v"). //
	join(MODEL).as("m").on().prop("model").eq().prop("m.id"). //
	where().prop("m.make.key").eq().val("MERC").and().prop("v.model.make.key").like().val("MERC%").yield().maxOf().prop("m.make.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model"), prop("v.model.make.key") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("m.id"), prop("m.make.key"), prop("m.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association5() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v").join(MODEL).as("m").on().prop("model").eq().prop("m").where().prop("m.make.key").eq().val("MERC").yield().maxOf().prop("m.make.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("m"), prop("m.make.key"), prop("m.make.key") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association6() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).groupBy().prop("model").yield().prop("model").as("model").yield().minOf().yearOf().prop("initDate").as("earliestInitYear").modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("model.make.key"), prop("earliestInitYear") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association7() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(VEHICLE). //
	groupBy().prop("model"). //
	yield().prop("model").as("key"). //
	yield().countOf().prop("id").as("count"). //
	modelAsEntity(TgModelCount.class);

	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("key.make.key").eq().val("MERC").and().prop("count").ge().val(2000).modelAsAggregate();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> expReferencingProps = Arrays.asList(new EntProp[] { prop("key.make.key"), prop("count") });
	assertEquals("Incorrect list of unresolved props", expReferencingProps, entQry.getSources().getAllSources().get(0).getReferencingProps());
    }

    @Test
    public void test_prop_to_source_association8() {
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
	prop("earliestInitYear").ge().val(2000).and(). //
	prop("count").ge().val(25).modelAsAggregate();

	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("model"), prop("model.make.key"), prop("earliestInitYear") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("mc.key"), prop("count") });
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props, qrySource2props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association9() {
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

	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("totalVehCount"), prop("make.key"), prop("earliestInitYearPerMake"), prop("totalVehCount")});
	assertEquals("Incorrect list of unresolved props", Arrays.asList(new List[] { qrySource1props }), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_prop_to_source_association10() {
	//select(OrgUnit5.class).where("parent.parent.parent.parent.key").eq().val("NORTH");
	final PrimitiveResultQueryModel qry = select(ORG5). //
	join(ORG4).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(ORG3).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(ORG2).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(ORG1).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").yield().prop("parent.parent.parent.parent.key").modelAsPrimitive();
	final EntQuery entQry = entQuery1(qry);
	final List<EntProp> qrySource1props = Arrays.asList(new EntProp[] { prop("parent") });
	final List<EntProp> qrySource2props = Arrays.asList(new EntProp[] { prop("parent.id"), prop("parent.parent") });
	final List<EntProp> qrySource3props = Arrays.asList(new EntProp[] { prop("parent.parent.id"), prop("parent.parent.parent") });
	final List<EntProp> qrySource4props = Arrays.asList(new EntProp[] { prop("parent.parent.parent.id"), prop("parent.parent.parent.parent") });
	final List<EntProp> qrySource5props = Arrays.asList(new EntProp[] { prop("parent.parent.parent.parent.id"), prop("parent.parent.parent.parent.key"), prop("parent.parent.parent.parent.key") });
	assertEquals("Incorrect association between properties and query sources", Arrays.asList(new List[] { qrySource1props, qrySource2props, qrySource3props, qrySource4props, qrySource5props}), entQry.getSources().getSourcesReferencingProps());
    }

    @Test
    public void test_that_ambiguous_property_parent_parent_parent_is_detected() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(ORG5). //
	join(ORG4).on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent").model();
	try {
	    entQuery1(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	    assertEquals("Incorrect exception message", "Ambiguous property: parent.parent.parent", e.getMessage());
	}
    }

    @Test
    public void test_that_ambiguous_propertyp_parent_is_detected() {
	final EntityResultQueryModel<TgOrgUnit5> qry = select(ORG5). //
	join(ORG4).on().prop("parent").eq().prop("parent"). //
	join(ORG4).as("parent").on().prop("parent").eq().prop("parent.id"). //
	join(ORG3).as("parent.parent").on().prop("parent.parent").eq().prop("parent.parent.id"). //
	join(ORG2).as("parent.parent.parent").on().prop("parent.parent.parent").eq().prop("parent.parent.parent.id"). //
	join(ORG1).as("parent.parent.parent.parent").on().prop("parent.parent.parent.parent").eq().prop("parent.parent.parent.parent.id"). //
	where().prop("parent.parent.parent.parent.key").eq().val("NORTH").model();
	try {
	    entQuery1(qry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	    assertEquals("Incorrect exception message", "Ambiguous property: parent", e.getMessage());
	}
    }
}