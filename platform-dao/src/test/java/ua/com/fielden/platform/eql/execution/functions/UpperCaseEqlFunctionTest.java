package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class UpperCaseEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void evaluates_to_null_when_operand_is_null() {
        final var qry = select().yield().upperCase().val(null).as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value_with_lowercased_string() {
        final var qry = select().yield().upperCase().val("abc").as(RESULT).modelAsAggregate();
        assertEquals("ABC", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value_with_mixedcased_string() {
        final var qry = select().yield().upperCase().val("aBc").as(RESULT).modelAsAggregate();
        assertEquals("ABC", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().upperCase().prop("lowercasedStringOfAbc").as(RESULT).modelAsAggregate();
        assertEquals("ABC", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_function() {
        final var qry = select(TeNamedValuesVector.class).yield().
                upperCase().concat().val("A").with().val("b").end().
                as(RESULT).modelAsAggregate();
        assertEquals("AB", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_subquery() {
        final var qry = select().yield().
                upperCase().model(select(TeNamedValuesVector.class).yield().prop("lowercasedStringOfAbc").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals("ABC", retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().concat().val("A").with().val("BC").end().eq().upperCase().prop("lowercasedStringOfAbc").then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }
}