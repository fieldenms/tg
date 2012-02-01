package ua.com.fielden.platform.entity.query.generation;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesFinalPropertiesAssociationTest extends BaseEntQueryTCase {

    private final String incP2S = "Inccorect association between properties and query sources";
    private final String incFP2S = "Inccorect association between properties and query sources";


    @Test
    public void test0() {
	final EntityResultQueryModel<TgOrgUnit5> shortcutQry  = select(VEHICLE).as("v").where().prop("v.station.key").eq().val("AA").yield().prop("station").modelAsEntity(ORG5);
	final EntQuery entQry = entQry(shortcutQry);

	final List<PropResolutionInfo> src1FinProps = prepare( //
		propResInf("v.station", "v", "station", false, ORG5), //
		propResInf("station", null, "station", false, ORG5));

	final List<PropResolutionInfo> src2FinProps = prepare( //
		propResInf("v.station.id", "v.station", "id", false, LONG), //
		propResInf("v.station.key", "v.station", "key", false, STRING));
	assertEquals(incP2S, compose(src1FinProps, src2FinProps), getSourcesFinalReferencingProps(entQry));
    }
}