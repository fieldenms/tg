package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;

public class ConcatEqlFunctionTest extends AbstractEqlExecutionTestCase {
    private static final DateFormat FORMATTER_WITH_MILLISECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Test
    public void works_with_single_nonnull_argument() {
        final var qry = select().yield().concat().val("a").end().as(RESULT).modelAsAggregate();
        assertEquals("a", retrieveResult(qry));
    }

    @Test
    public void works_with_two_nonnull_arguments() {
        final var qry = select().yield().concat().val("a").with().val("b").end().as(RESULT).modelAsAggregate();
        assertEquals("ab", retrieveResult(qry));
    }

    @Test
    public void works_with_three_nonnull_arguments() {
        final var qry = select().yield().concat().val("a").with().val("b").with().val("c").end().as(RESULT).modelAsAggregate();
        assertEquals("abc", retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_the_single_operand_is_null() {
        final var qry = select().yield().concat().val(null).end().as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_one_of_two_operands_is_null() {
        final var qry = select().yield().concat().val("a").with().val(null).end().as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void evaluates_to_null_when_one_of_three_operands_is_null() {
        final var qry = select().yield().concat().val("a").with().val(null).with().val("c").end().as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_one_operand_is_value_and_another_one_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().concat().val("date is ").with().prop("dateOf20010911").end().as(RESULT).modelAsAggregate();
        assertEquals("date is 11/09/2001", retrieveResult(qry));
    }

    @Test
    public void works_when_one_operand_is_function_and_another_one_is_subquery() {
        final var qry = select(TeNamedValuesVector.class).yield().
                concat().
                    ifNull().prop("dateOfNullValue").then().prop("dateOf20010911").
                with().
                    val(" is a ").
                with().
                    model(select().yield().prop("stringOfSadEvent").modelAsPrimitive()).
                end().as(RESULT).modelAsAggregate();
        assertEquals("11/09/2001 is a sad event", retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().concat().val("a").with().val("bc").end().eq().val("abc").then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }


    // the remaining tests are for indirect testing of implicit conversion of arguments of the several EQL functions, namely concat(), upperCase(), lowerCase().

    @Test
    public void works_with_integer_value() {
        final var qry = select().yield().concat().
                val(100).
                end().as(RESULT).modelAsAggregate();
        assertEquals("100", retrieveResult(qry));
    }

    @Test
    public void works_with_long_value() {
        final var qry = select().yield().concat().
                val(100l).
                end().as(RESULT).modelAsAggregate();
        assertEquals("100", retrieveResult(qry));
    }

    @Test
    public void works_with_big_decimal_value() {
        final var qry = select().yield().concat().
                val(new BigDecimal("100.00")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("100.00", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_zero_seconds_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 00:00:00")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_zero_seconds_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:00:00.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_zero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:00:00.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:00:00.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_nonzero_seconds_and_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 00:00:59")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:00:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_nonzero_seconds_and_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:00:59.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:00:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_zero_minutes_nonzero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:00:59.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:00:59.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_zero_seconds_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 00:22:00")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_zero_seconds_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:22:00.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_zero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:22:00.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22:00.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_nonzero_seconds_and_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 00:22:59")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_nonzero_seconds_and_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:22:59.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_zero_hours_nonzero_minutes_nonzero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 00:22:59.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 00:22:59.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_zero_seconds_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 01:00:00")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_zero_seconds_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:00:00.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_zero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:00:00.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00:00.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_nonzero_seconds_and_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 01:00:59")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_nonzero_seconds_and_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:00:59.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_zero_minutes_nonzero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:00:59.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:00:59.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_zero_seconds_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 01:22:00")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_zero_seconds_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:22:00.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_zero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:22:00.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22:00.999", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_nonzero_seconds_and_none_milliseconds() {
        final var qry = select().yield().concat().
                val(date("2018-10-31 01:22:59")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_nonzero_seconds_and_zero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:22:59.000")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22:59", retrieveResult(qry));
    }

    @Test
    public void works_with_date_value_composed_of_nonzero_hours_nonzero_minutes_nonzero_seconds_and_nonzero_milliseconds() {
        final var qry = select().yield().concat().
                val(dateWithMilliseconds("2018-10-31 01:22:59.999")).
                end().as(RESULT).modelAsAggregate();
        assertEquals("31/10/2018 01:22:59.999", retrieveResult(qry));
    }

    private static final Date dateWithMilliseconds(final String dateTime) {
        try {
            return FORMATTER_WITH_MILLISECONDS.parse(dateTime);
        } catch (ParseException e) {
            throw new DomainDrivenTestException(format("Could not parse value [%s].", dateTime));
        }
    }
}