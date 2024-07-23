package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class YearOfEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void evaluates_to_null_when_operand_is_null() {
        final var qry = select().yield().yearOf().val(null).as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value() {
        final var qry = select().yield().yearOf().val(date("2024-05-29 03:40:04")).as(RESULT).modelAsAggregate();
        assertEquals(valueOf(2024), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().yearOf().prop("dateAndTimeOf20010911084640").as(RESULT).modelAsAggregate();
        assertEquals(valueOf(2001), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_function() {
        final var qry = select(TeNamedValuesVector.class).yield().
                yearOf().ifNull().prop("dateOfNullValue").then().val(date("2001-01-01 23:59:59")).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(2001), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_subquery() {
        final var qry = select().yield().
                yearOf().model(select(TeNamedValuesVector.class).yield().prop("dateAndTimeOf20010911084640").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals(valueOf(2001), retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().val(2001).eq().yearOf().prop("dateAndTimeOf20010911084640").then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }
}