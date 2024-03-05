package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class MonthOfEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void evaluates_to_null_when_operand_is_null() {
        final var qry = select().yield().monthOf().val(null).as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value() {
        final var qry = select().yield().monthOf().val(date("2024-05-29 03:40:04")).as(RESULT).modelAsAggregate();
        assertEquals(valueOf(5), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().monthOf().prop("dateAndTimeOf20010911084640").as(RESULT).modelAsAggregate();
        assertEquals(valueOf(9), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_function() {
        final var qry = select(TeNamedValuesVector.class).yield().
                monthOf().ifNull().prop("dateOfNullValue").then().val(date("2001-01-01 23:59:59")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_subquery() {
        final var qry = select().yield().
                monthOf().model(select(TeNamedValuesVector.class).yield().prop("dateAndTimeOf20010911084640").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(9), retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().val(9).eq().monthOf().prop("dateAndTimeOf20010911084640").then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }
}