package ua.com.fielden.platform.entity.query.generation;

import java.util.List;

import org.hibernate.Hibernate;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesFinalPropertiesAssociationTest extends BaseEntQueryTCase {

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
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA"). //
		yield().prop("id").as("id"). //
		yield().prop("key").as("key"). //
		yield().prop("model").as("model"). //
		yield().prop("model.make").as("model.make"). //
		yield().prop("model.key").as("model.key"). //
		modelAsEntity(VEHICLE);
	final PrimitiveResultQueryModel qry = select(sourceQry).where().prop("model.key").eq().val("AA").yield().prop("model.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false)),
		propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false))
		);

	assertEquals(incP2S, compose(src1FinProps), getSourcesFinalReferencingProps(entQry));
    }

    @Test
    @Ignore
    public void test2() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA").yield().prop("model").modelAsEntity(VEHICLE);
	final PrimitiveResultQueryModel qry = select(sourceQry).where().prop("key").eq().val("AA").yield().prop("key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);

//	final PrimitiveResultQueryModel explicitQry = select(sourceQry).join(MODEL).on().prop("id").eq().prop("").where().prop("key").eq().val("AA").yield().prop("key").modelAsPrimitive();
	System.out.println(entQry.sql());

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false)),
		propResInf("model.key", null, ppi("model.key", STRING, H_STRING, false))
		);

	assertEquals(incP2S, compose(src1FinProps), getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test3() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA"). //
		yield().prop("id").as("id"). //
		yield().prop("key").as("key"). //
		yield().prop("model").as("model"). //
		yield().prop("model.make").as("model.make"). //
		yield().prop("model.key").as("model.key"). //
		modelAsEntity(VEHICLE);
	final PrimitiveResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("AA").yield().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("model.make", null, ppi("model.make", MAKE, Hibernate.LONG, true))
		);

	final List<PropResolutionInfo> src2FinProps = prepare( //
		propResInf("model.make.id", "model.make", ppi("id", LONG, H_LONG, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)), //
		propResInf("model.make.key", "model.make", ppi("key", STRING, H_STRING, true)));
	assertEquals(incP2S, compose(src1FinProps, src2FinProps), getSourcesFinalReferencingProps(entQry));
    }

    @Test
    public void test4() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).where().prop("station.key").eq().val("AA"). //
		yield().prop("id").as("id"). //
		yield().prop("key").as("key"). //
		yield().prop("model").as("model"). //
		modelAsAggregate();
	final PrimitiveResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("AA").yield().prop("model.make.key").modelAsPrimitive();
	final EntQuery entQry = entResultQry(qry);
    }
}