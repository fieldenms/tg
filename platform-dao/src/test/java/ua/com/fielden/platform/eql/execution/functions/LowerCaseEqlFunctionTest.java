package ua.com.fielden.platform.eql.execution.functions;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class LowerCaseEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void evaluates_to_null_when_operand_is_null() {
        final var qry = select().yield().lowerCase().val(null).as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value_with_uppercased_string() {
        final var qry = select().yield().lowerCase().val("ABC").as(RESULT).modelAsAggregate();
        assertEquals("abc", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value_with_mixedcased_string() {
        final var qry = select().yield().lowerCase().val("aBc").as(RESULT).modelAsAggregate();
        assertEquals("abc", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().lowerCase().prop("uppercasedStringOfAbc").as(RESULT).modelAsAggregate();
        assertEquals("abc", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_function() {
        final var qry = select(TeNamedValuesVector.class).yield().
                lowerCase().concat().val("A").with().val("b").end().
                as(RESULT).modelAsAggregate();
        assertEquals("ab", retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_subquery() {
        final var qry = select().yield().
                lowerCase().model(select(TeNamedValuesVector.class).yield().prop("uppercasedStringOfAbc").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals("abc", retrieveResult(qry));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().concat().val("a").with().val("bc").end().eq().lowerCase().prop("uppercasedStringOfAbc").then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(valueOf(1), retrieveResult(qry));
    }
}