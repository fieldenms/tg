package ua.com.fielden.platform.entity.query.metadata;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.EMPTY_STRING;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.NON_STRING;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo.STRING;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.TypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class CompositeKeyEqlExpressionGeneratorTest {
    private static final String separator = " ";
    private static final String prop1 = "prop1";
    private static final String prop2 = "prop2";
    private static final String prop3 = "prop3";

    private static CompositeKeyEqlExpressionGenerator.KeyMemberInfo kmi(final String name, final TypeInfo type, final boolean optional) {
        return new CompositeKeyEqlExpressionGenerator.KeyMemberInfo(name, type, optional);
    }
    
    @Test
    public void key_with_single_not_optional_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, STRING, false)));
        final ExpressionModel exp = expr().prop(prop1).model();
        assertEquals(exp, act);
    }

    @Test
    public void key_with_single_not_optional_entity_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, ENTITY, false)));
        final ExpressionModel exp = expr().prop(prop1 + ".key").model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_single_not_optional_non_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, NON_STRING, false)));
        final ExpressionModel exp = expr().concat().prop(prop1).with().val(EMPTY_STRING).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_single_optional_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, STRING, true)));
        final ExpressionModel exp = expr().caseWhen().prop(prop1).isNotNull().then().expr(expr().prop(prop1).model()).end().model();
        assertEquals(exp, act);
    }

    @Test
    public void key_with_single_optional_entity_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, ENTITY, true)));
        final ExpressionModel exp = expr().caseWhen().prop(prop1).isNotNull().then().expr(expr().prop(prop1 + ".key").model()).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_single_optional_non_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(kmi(prop1, NON_STRING, true)));
        final ExpressionModel exp = expr().caseWhen().prop(prop1).isNotNull().then().expr(expr().concat().prop(prop1).with().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }

    @Test
    public void key_with_first_not_optional_string_member_and_second_optional_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop("prop1").model()).
                with().
                expr(expr().caseWhen().prop("prop2").isNotNull().then().concat().val(separator).with().expr(expr().prop("prop2").model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }

    @Test
    public void key_with_first_not_optional_string_member_and_second_optional_non_string_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, NON_STRING, true)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2).model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_first_not_optional_string_member_and_second_optional_entity_member_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, ENTITY, true)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2 +".key").model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_three_string_members_where_second_one_is_optional_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true), 
                kmi(prop3, STRING, false)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2).model()).end().otherwise().val(EMPTY_STRING).end().model()).
                with().
                expr(expr().val(separator).model()).
                with().
                expr(expr().prop(prop3).model()).
                end().model();
        assertEquals(exp, act);
    }

    @Test
    public void key_with_three_string_members_where_only_first_one_is_not_optional_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true), 
                kmi(prop3, STRING, true)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2).model()).end().otherwise().val(EMPTY_STRING).end().model()).
                with().
                expr(expr().caseWhen().prop(prop3).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop3).model()).end().otherwise().val(EMPTY_STRING).end().model()).
                end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_all_key_members_not_optional_and_of_different_types_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, NON_STRING, false), 
                kmi(prop3, ENTITY, false)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().val(separator).model()).
                with().
                expr(expr().prop(prop2).model()).
                with().
                expr(expr().val(separator).model()).
                with().
                expr(expr().prop(prop3 + ".key").model()).
                end().
                model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_three_string_not_optional_members_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, NON_STRING, false), 
                kmi(prop2, NON_STRING, false), 
                kmi(prop3, NON_STRING, false)));
        final ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().val(separator).model()).
                with().
                expr(expr().prop(prop2).model()).
                with().
                expr(expr().val(separator).model()).
                with().
                expr(expr().prop(prop3).model()).
                end().
                model();
        assertEquals(exp, act);
    }
    
    @Test
    public void key_with_two_entity_optional_members_works() {
        final ExpressionModel act = generateCompositeKeyEqlExpression(separator, listOf(
                kmi(prop1, ENTITY, true), 
                kmi(prop2, ENTITY, true)));
        final ExpressionModel exp = expr().caseWhen().condition(cond().expr(expr().prop(prop1 + ".key").model()).isNotNull().and().expr(expr().prop(prop2 +".key").model()).isNotNull().model()).
        then().expr(expr().concat().expr(expr().prop(prop1 +".key").model()).with().expr(expr().val(separator).model()).with().expr(expr().prop(prop2 +".key").model()).end().model()).
        when().condition(cond().expr(expr().prop(prop1 + ".key").model()).isNotNull().and().expr(expr().prop(prop2 + ".key").model()).isNull().model()).
        then().expr(expr().prop(prop1 + ".key").model()).
        when().expr(expr().prop(prop2 + ".key").model()).isNotNull().
        then().expr(expr().prop(prop2 + ".key").model()).
        otherwise().val(null).end().model();

        assertEquals(exp, act);
    }
}