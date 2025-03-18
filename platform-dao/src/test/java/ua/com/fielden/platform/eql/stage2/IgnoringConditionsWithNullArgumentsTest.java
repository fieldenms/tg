package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

public class IgnoringConditionsWithNullArgumentsTest extends EqlStage2TestCase {

    @Test
    public void condition_is_correctly_ignored_01() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop(KEY).eq().iVal(null));

        final ResultQuery2 expQry = qryCountAll(sources(source(1, MODEL)));

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void condition_is_correctly_ignored_02() {
        final var params = mapOf(t2("keyValue", null));
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop(KEY).eq().iParam("keyValue"), params);

        final ResultQuery2 expQry = qryCountAll(sources(source(1, MODEL)));
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void condition_is_correctly_ignored_03() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().
                prop(KEY).eq().val(1).
                and().
                prop(KEY).gt().iVal(null));

        final var source = source(1, MODEL);
        final ResultQuery2 expQry = qryCountAll(sources(source), cond(eq(prop(source, KEY), val(1))));

        assertEquals(expQry, actQry);
    }

    @Test
    public void condition_is_correctly_ignored_04() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().
                prop(KEY).eq().val(1).
                and().
                prop(KEY).ne().val(0).
                or().
                prop(KEY).gt().iVal(null) // ignore
        );

        final var source = source(1, MODEL);
        final ResultQuery2 expQry = qryCountAll(
                sources(source),
                or(and(eq(prop(source, KEY), val(1)), ne(prop(source, KEY), val(0)))));

        assertEquals(expQry, actQry);
    }

    @Test
    public void condition_is_correctly_ignored_05() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().
                prop(KEY).eq().val(1).
                and().
                begin().
                    prop(KEY).ne().val(0).
                    or().
                    prop(KEY).gt().iVal(null). //ignore
                end());

        final var source = source(1, MODEL);
        final ResultQuery2 expQry = qryCountAll(
                sources(source),
                or(and(eq(prop(source, KEY), val(1)), cond(ne(prop(source, KEY), val(0))))));

        assertEquals(expQry, actQry);
    }

    @Test
    public void condition_is_correctly_ignored_06() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().
                prop(KEY).eq().val(1).
                and().
                begin().
                    // ignore begin
                    begin().
                        prop(KEY).gt().iVal(null).
                    end().
                    // ignore end
                    or().
                    prop(KEY).ne().val(0).
                end());

        final var source = source(1, MODEL);
        final ResultQuery2 expQry = qryCountAll(
                sources(source),
                or(and(eq(prop(source, KEY), val(1)), cond(ne(prop(source, KEY), val(0))))));

        assertEquals(expQry, actQry);
    }

}
