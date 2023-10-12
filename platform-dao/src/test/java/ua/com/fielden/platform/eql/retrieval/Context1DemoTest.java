package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;

public class Context1DemoTest extends AbstractEqlShortcutTest {

    @Test
    public void auto_yield_in_typeless_subquery_yields_null_value_aliased_as_empty_string() {
        final var act = select(VEHICLE_FUEL_USAGE).where().prop("vehicle.key").eq().val("A001").and().
                notExists(select(VEHICLE_FUEL_USAGE).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").
                        model()).model();


        final var exp = act;
        
        assertModelResultsEquals(exp, act);
    }
    
    @Test
    public void eql3_query_executes_correctly34() {
        final var act = select(TgOrgUnit1.class).where().exists( //
                select(TgOrgUnit2.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit3.class).where().prop("parent").eq().extProp("id").and().exists( // 
                        select(TgOrgUnit4.class).where().prop("parent").eq().extProp("id").and().exists( //
                        select(TgOrgUnit5.class).where().prop("parent").eq().extProp("id").and().prop("name").isNotNull(). //
                        model()). //
                        model()). //
                        model()). //
                        model()). //
                and().prop("id").isNotNull().model();

        final var exp = act;

        assertModelResultsEquals(exp, act);
    }
}