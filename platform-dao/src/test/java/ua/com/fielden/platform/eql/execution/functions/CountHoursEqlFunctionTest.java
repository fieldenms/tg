package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class CountHoursEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_hour() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-01 18:55:40")).
                and().
                val(date("2007-01-01 18:01:15")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(0), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_hour_and_the_end_date_precedes_the_start_date() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-01 18:01:15")).
                and().
                val(date("2007-01-01 18:55:40")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(0), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_day() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-01 20:10:40")).
                and().
                val(date("2007-01-01 18:30:15")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(2), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_day_and_the_end_date_precedes_the_start_date() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-01 18:30:15")).
                and().
                val(date("2007-01-01 20:10:40")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(-2), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_month() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-03 20:31:05")).
                and().
                val(date("2007-01-01 18:30:15")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(50), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_month_and_the_end_date_precedes_the_start_date() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-01 18:30:15")).
                and().
                val(date("2007-01-03 20:31:05")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(-50), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_year() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-02-01 12:00:00")).
                and().
                val(date("2007-01-30 18:30:15")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(42), retrieveResult(qry));
    }

    @Test
    public void works_when_both_the_start_and_end_date_is_within_the_same_year_and_the_end_date_precedes_the_start_date() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-01-30 18:30:15")).
                and().
                val(date("2007-02-01 12:00:00")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(-42), retrieveResult(qry));
    }

    @Test
    public void works_when_the_start_date_year_differs_from_the_end_date_year() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2008-01-01 12:01:02")).
                and().
                val(date("2007-12-30 00:00:00")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(60), retrieveResult(qry));
    }

    @Test
    public void works_when_the_start_date_year_differs_from_the_end_date_year_and_the_end_date_precedes_the_start_date() {
        final var qry = select().yield().
                count().hours().between().
                val(date("2007-12-30 00:00:00")).
                and().
                val(date("2008-01-01 12:01:02")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(-60), retrieveResult(qry));
    }

    @Test
    public void works_when_the_start_date_is_value_and_the_end_date_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().
                count().hours().between().
                val(date("2001-09-11 01:01:01")).
                and().
                prop("dateOf20010911").
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }

    @Test
    public void works_when_the_start_date_is_function_and_the_end_date_is_subquery() {
        final var qry = select(TeNamedValuesVector.class).yield().
                count().hours().between().
                ifNull().prop("dateOfNullValue").then().val(date("2001-09-11 10:01:01")).
                and().
                model(select().yield().prop("dateOf20010911").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(10), retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_the_start_date_is_null() {
        final var qry = select(TeNamedValuesVector.class).yield().
                count().hours().between().
                val(date("2001-09-11 01:01:01")).
                and().
                prop("dateOfNullValue").
                as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_the_end_date_is_null() {
        final var qry = select().yield().
                count().hours().between().
                val(null).
                and().
                val(date("2007-12-30 00:00:00")).
                as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_both_the_start_and_end_date_is_null() {
        final var qry = select(TeNamedValuesVector.class).yield().
                count().hours().between().
                val(null).
                and().
                prop("dateOfNullValue").
                as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().
                caseWhen().count().hours().between().val(date("2001-01-01 01:01:01")).and().val(date("2001-01-01 00:00:00")).eq().val(1).then().val(1).end().
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }
}