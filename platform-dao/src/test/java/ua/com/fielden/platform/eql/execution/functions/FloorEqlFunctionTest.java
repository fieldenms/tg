package ua.com.fielden.platform.eql.execution.functions;

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;

public class FloorEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void evaluates_to_null_when_operand_is_null() {
        final var qry = select().yield().floor().val(null).as(RESULT).modelAsAggregate();
        assertNull(retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_value() {
        final var qry = select().yield().floor().val(valueOf(3)).as(RESULT).modelAsAggregate();
        assertEquals(valueOf(3), retrieveResult(qry));
    }

    @Test
    public void rounds_down_positive_value() {
        final var qry = select().yield().floor().val(new BigDecimal("3.8")).as(RESULT).modelAsAggregate();
        assertEquals(valueOf(3), retrieveResult(qry));
    }

    @Test
    public void rounds_down_negative_value_away_from_zero() {
        final var qry = select().yield().floor().val(new BigDecimal("-3.2")).as(RESULT).modelAsAggregate();
        assertEquals(valueOf(-4), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_property() {
        final var qry = select(TeNamedValuesVector.class).yield().floor().prop("integerOfZero").as(RESULT).modelAsAggregate();
        assertEquals(0, ((Number) retrieveResult(qry)).intValue());
    }

    @Test
    public void works_when_operand_is_function() {
        final var qry = select(TeNamedValuesVector.class).yield().
                floor().ifNull().prop("integerOfNullValue").then().val(new BigDecimal("3.7")).
                as(RESULT).modelAsAggregate();
        assertEquals(Integer.valueOf(3), retrieveResult(qry));
    }

    @Test
    public void works_when_operand_is_subquery() {
        final var qry = select().yield().
                floor().model(select(TeNamedValuesVector.class).yield().prop("integerOfZero").modelAsPrimitive()).
                as(RESULT).modelAsAggregate();
        assertEquals(0, ((Number) retrieveResult(qry)).intValue());
    }

    @Test
    public void works_when_operand_is_parameter() {
        final var qry = select().yield().floor().param("paramName").as(RESULT).modelAsAggregate();
        assertEquals(valueOf(3), retrieveResult(qry, Map.of("paramName", new BigDecimal("3.8"))));
    }

    @Test
    public void works_when_the_function_result_is_used_within_other_query_expressions_and_conditions() {
        final var qry = select(TeNamedValuesVector.class).yield().caseWhen().val(3).eq().floor().val(new BigDecimal("3.8")).then().val(1).end().as(RESULT).modelAsAggregate();
        assertEquals(Integer.valueOf(1), retrieveResult(qry));
    }
}
