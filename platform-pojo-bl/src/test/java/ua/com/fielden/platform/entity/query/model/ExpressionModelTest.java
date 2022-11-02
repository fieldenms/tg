package ua.com.fielden.platform.entity.query.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import org.junit.Test;

public class ExpressionModelTest {

    @Test
    public void expression_with_sumOf_function_contains_aggregations() {
        final var expr = expr().sumOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_sumOfDistinct_function_contains_aggregations() {
        final var expr = expr().sumOfDistinct().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_avgOf_function_contains_aggregations() {
        final var expr = expr().avgOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_avgOfDistinct_function_contains_aggregations() {
        final var expr = expr().avgOfDistinct().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_countAll_function_contains_aggregations() {
        final var expr = expr().countAll().model();
        assertTrue(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_countOf_function_contains_aggregations() {
        final var expr = expr().countOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_countOfDistinct_function_contains_aggregations() {
        final var expr = expr().countOfDistinct().val(1).model();
        assertTrue(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_maxOf_function_contains_aggregations() {
        final var expr = expr().maxOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_minOf_function_contains_aggregations() {
        final var expr = expr().minOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_absOf_function_does_not_contain_aggregations() {
        final var expr = expr().absOf().val(1).model();
        assertFalse(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_aggregate_function_being_part_of_plain_arithmetic_expression_contains_aggregations() {
        final var expr = expr().val(1).add().sumOf().val(1).model();
        assertTrue(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_aggregate_function_being_part_of_another_stand_alone_expression_contains_aggregations() {
        final var expr = expr().caseWhen().expr(expr().sumOf().prop("cost").model()).ne().val(0).then().expr(expr().sumOf().prop("cost").div().sumOf().prop("kms").model()).end().model();
        assertTrue(expr.containsAggregations());
    }

    @Test
    public void expression_with_aggregate_function_being_part_of_another_stand_alone_expression_embedded_into_another_stand_alone_expression_contains_aggregations() {
        final var expr = expr().expr(expr().caseWhen().expr(expr().sumOf().prop("cost").model()).ne().val(0).then().expr(expr().sumOf().prop("cost").div().sumOf().prop("kms").model()).end().model()).model();
        assertTrue(expr.containsAggregations());
    }
    
    @Test
    public void expression_with_aggregate_function_being_part_of_another_stand_alone_expression_enclosed_in_parentheses_contains_aggregations() {
        final var expr = expr().caseWhen().beginExpr().expr(expr().sumOf().val(1).model()).add().val(100).endExpr().gt().val(1000).then().val(0).end().model();
        assertTrue(expr.containsAggregations());
    }
}