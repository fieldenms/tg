package ua.com.fielden.platform.eql.execution.functions;

import org.junit.Test;
import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class IfNullTest extends AbstractEqlExecutionTestCase {

    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_01() {
        final var query = select()
                .yield().ifNull().val(null).then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();
        
        assertNull(retrieveResult(query));
    }

    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_02() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(null)
                .as(RESULT)
                .modelAsAggregate();

        assertNull(retrieveResult(query));
    }

    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_03() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive())
                        .then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();

        assertNull(retrieveResult(query));
    }

    @Test
    public void ifNull_executes_when_one_of_the_operands_has_unknown_type_01() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1)
                .as(RESULT)
                .modelAsAggregate();

        assertEquals(1, retrieveResult(query));
    }

    @Test
    public void ifNull_executes_when_one_of_the_operands_has_unknown_type_02() {
        final var query = select()
                .yield().ifNull().val(1).then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();
        
        assertEquals(1, retrieveResult(query));
    }

}
