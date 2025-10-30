package ua.com.fielden.platform.eql.execution.functions;

import org.junit.Test;
import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.test.WithDbVersion;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class IfNullTest extends AbstractEqlExecutionTestCase {

    /// The fist operant is value `null` and the second operand is a subquery yielding `null`.
    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_01() {
        final var query = select()
                .yield().ifNull().val(null).then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();
        
        assertNull(retrieveResult(query));
    }

    /// The fist operant is a query yielding `null` and the second operand is value `null`.
    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_02() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(null)
                .as(RESULT)
                .modelAsAggregate();

        assertNull(retrieveResult(query));
    }

    /// Both operands are subqueries yielding `null`.
    @Test
    public void ifNull_executes_when_both_operands_have_unknown_type_03() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive())
                        .then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();

        assertNull(retrieveResult(query));
    }

    /// Both operands are values of `null`.
    @WithDbVersion(MSSQL)
    @Test
    public void MSSQL_ifNull_cannot_have_both_operands_as_null_vals() {
        final var query = select()
                .yield().ifNull().val(null).then().val(null)
                .as(RESULT)
                .modelAsAggregate();

        assertThrows(EntityRetrievalException.class, () -> retrieveResult(query));
    }

    @Test
    public void ifNull_executes_if_first_operand_has_unknown_type() {
        final var query = select()
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1)
                .as(RESULT)
                .modelAsAggregate();

        assertEquals(1, retrieveResult(query));
    }

    @Test
    public void ifNull_executes_if_second_operand_has_unknown_type() {
        final var query = select()
                .yield().ifNull().val(1).then().model(select().yield().val(null).modelAsPrimitive())
                .as(RESULT)
                .modelAsAggregate();
        
        assertEquals(1, retrieveResult(query));
    }

    @Test
    public void ifNull_with_a_subquery_can_be_used_in_a_join_condition() {
        var query = select(select().yield().val(null).as("x").modelAsAggregate())
                .as("q1")
                .join(select().yield().val(1).as("x").modelAsAggregate())
                .as("q2")
                .on().ifNull().model(select().yield().prop("q1.x").modelAsPrimitive()).then().val(1).eq().prop("q2.x")
                     .and()
                     .ifNull().val(1).then().model(select().yield().prop("q1.x").modelAsPrimitive()).eq().prop("q2.x")
                     .and()
                     .ifNull().model(select().yield().prop("q1.x").modelAsPrimitive())
                              .then().model(select().yield().prop("q2.x").modelAsPrimitive()).eq().prop("q2.x")
                .yield().prop("q2.x").as(RESULT)
                .modelAsAggregate();

        assertEquals(1, retrieveResult(query));
    }

    @Test
    public void ifNull_with_a_subquery_can_be_used_in_order_by() {
        var query = select(select().yield().val(null).as("x")
                                   .yield().val(50).as("n")
                                   .modelAsAggregate())
                .orderBy().ifNull().model(select().yield().prop("x").modelAsPrimitive()).then().val(1).asc()
                          .ifNull().prop("x").then().model(select().yield().prop("x").modelAsPrimitive()).asc()
                          .ifNull().model(select().yield().prop("x").modelAsPrimitive())
                                   .then().model(select().yield().val(1).modelAsPrimitive()).asc()
                .yield().prop("n").as(RESULT)
                .modelAsAggregate();

        assertEquals(50, retrieveResult(query));
    }

    /// In general, SQL Server prohibits the use of [subqueries as grouping expressions](https://learn.microsoft.com/en-us/sql/t-sql/queries/select-group-by-transact-sql?view=sql-server-ver16#column-expression).
    ///
    @WithDbVersion(MSSQL)
    @Test
    public void MSSQL_ifNull_with_a_subquery_cannot_be_used_in_group_by() {
        var query = select()
                .groupBy().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1)
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1).as(RESULT)
                .modelAsAggregate();

        assertThrows(Throwable.class, () -> retrieveResult(query));
    }

    @WithDbVersion(POSTGRESQL)
    @Test
    public void POSTGRESQL_ifNull_with_a_subquery_can_be_used_in_group_by() {
        var query = select()
                .groupBy().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1)
                .yield().ifNull().model(select().yield().val(null).modelAsPrimitive()).then().val(1).as(RESULT)
                .modelAsAggregate();

        assertEquals(1, retrieveResult(query));
    }

}
