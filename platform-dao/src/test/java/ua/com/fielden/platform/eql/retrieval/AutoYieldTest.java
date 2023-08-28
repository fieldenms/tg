package ua.com.fielden.platform.eql.retrieval;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.stage0.YieldBuilder.ABSENT_ALIAS;

import org.junit.Test;

import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.operands.queries.SubQuery1;
import ua.com.fielden.platform.sample.domain.TeFuelUsageByType;

public class AutoYieldTest extends AbstractEqlShortcutTest {

    @Test
    public void auto_yield_in_typeless_subquery_yields_null_value_aliased_as_empty_string() {
        final var act = select(VEHICLE_FUEL_USAGE).where().prop("vehicle.key").eq().val("A001").and().
                notExists(select(VEHICLE_FUEL_USAGE).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").
                        model()).model();


        final var exp = select(VEHICLE_FUEL_USAGE).where().prop("vehicle.key").eq().val("A001").and().
                notExists(select(VEHICLE_FUEL_USAGE).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").
                        yield().val(null).as(ABSENT_ALIAS).
                        modelAsEntity(VEHICLE_FUEL_USAGE)).model();
        
        assertModelResultsEquals(exp, act);
    }
    
    @Test
    public void auto_yield_in_subquery_yields_id_property_aliased_as_empty_string() {
        final var act = select(VEHICLE).where().prop("model").in().model(select(MODEL).where().prop("make.key").in().values("MERC", "BMW").model()).model();

        final var exp = select(VEHICLE).where().prop("model").in().model(select(MODEL).where().prop("make.key").in().values("MERC", "BMW").yield().prop("id").as(ABSENT_ALIAS).modelAsEntity(MODEL)).model();
        
        assertModelResultsEquals(exp, act);
    }
    
    @Test
    public void auto_yield_in_subquery_should_fail_in_case_that_main_source_has_no_id() {
        try {
            transformToModelResult(select(VEHICLE).where().prop("model").in().model(select(TeFuelUsageByType.class).model()).model());
            fail("Auto-yield for query with main source having no ID property should throw exception.");
        } catch (final EqlStage1ProcessingException e) {
            final String expErrMsg = SubQuery1.ERR_AUTO_YIELD_IMPOSSIBLE_FOR_QUERY_WITH_MAIN_SOURCE_HAVING_NO_ID;
            assertEquals("Unexpected error message.", expErrMsg, e.getMessage());
        }
    }

}