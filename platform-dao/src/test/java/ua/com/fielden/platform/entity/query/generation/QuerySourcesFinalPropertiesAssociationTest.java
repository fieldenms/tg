package ua.com.fielden.platform.entity.query.generation;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QuerySourcesFinalPropertiesAssociationTest extends BaseEntQueryTCase {

    private final String incP2S = "Inccorect association between properties and query sources";
    private final String incFP2S = "Inccorect association between properties and query sources";


    @Test
    public void test0() {
	final PrimitiveResultQueryModel shortcutQry  = select(VEHICLE).as("v").where().prop("station").isNotNull().and().prop("v.station.key").eq().val("AA").yield().prop("v.station.key").modelAsPrimitive(STRING);
	final EntQuery entQry = entQry(shortcutQry);
	System.out.println(entQry.getSources().getAllSources().get(0).getFinalReferencingProps());
	System.out.println(entQry.getSources().getAllSources().get(1).getFinalReferencingProps());
    }
}