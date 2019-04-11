package ua.com.fielden.platform.entity.query.metadata;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.EMPTY_STRING;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.getVirtualKeyPropForEntityWithCompositeKey;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.ENTITY;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.NON_STRING;
import static ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo.STRING;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.metadata.EntityKeyExpressionGenerator.TypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class EntityKeyExpressionGeneratorTest {
    private static final String separator = " ";
    private static final String prop1 = "prop1";
    private static final String prop2 = "prop2";
    private static final String prop3 = "prop3";

    private static EntityKeyExpressionGenerator.KeyMemberInfo kmi(final String name, final TypeInfo type, final boolean optional) {
        return new EntityKeyExpressionGenerator.KeyMemberInfo(name, type, optional);
    }
    
    @Test
    public void tes_1sk_1() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(kmi(prop1, STRING, false)));
        ExpressionModel exp = expr().prop(prop1).model();
        assertEquals(exp, act);
    }

    @Test
    public void tes_1sk_2() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(kmi(prop1, ENTITY, false)));
        ExpressionModel exp = expr().prop(prop1 + ".key").model();
        assertEquals(exp, act);
    }
    
    @Test
    public void tes_1sk_3() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(kmi(prop1, NON_STRING, false)));
        ExpressionModel exp = expr().concat().prop(prop1).with().val(EMPTY_STRING).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void tes_2op_1() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true)));
        ExpressionModel exp = expr().
                concat().
                expr(expr().prop("prop1").model()).
                with().
                expr(expr().caseWhen().prop("prop2").isNotNull().then().concat().val(separator).with().expr(expr().prop("prop2").model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }

    @Test
    public void tes_2op_2() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, NON_STRING, true)));
        ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2).model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void tes_2op_3() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, ENTITY, true)));
        ExpressionModel exp = expr().
                concat().
                expr(expr().prop(prop1).model()).
                with().
                expr(expr().caseWhen().prop(prop2).isNotNull().then().concat().val(separator).with().expr(expr().prop(prop2 +".key").model()).end().otherwise().val(EMPTY_STRING).end().model()).end().model();
        assertEquals(exp, act);
    }
    
    @Test
    public void tes_3_1op() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true), 
                kmi(prop3, STRING, false)));
        ExpressionModel exp = expr().
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
    public void tes_3_2op() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, STRING, true), 
                kmi(prop3, STRING, true)));
        ExpressionModel exp = expr().
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
    public void tes1() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, STRING, false), 
                kmi(prop2, NON_STRING, false), 
                kmi(prop3, ENTITY, false)));
        ExpressionModel exp = expr().
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
    public void tes_3ns() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, NON_STRING, false), 
                kmi(prop2, NON_STRING, false), 
                kmi(prop3, NON_STRING, false)));
        ExpressionModel exp = expr().
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
    public void tes_1_2op() {
        ExpressionModel act = getVirtualKeyPropForEntityWithCompositeKey(separator, listOf(
                kmi(prop1, ENTITY, true), 
                kmi(prop2, ENTITY, true)));
        ExpressionModel exp = expr().caseWhen().condition(cond().expr(expr().prop(prop1 + ".key").model()).isNotNull().and().expr(expr().prop(prop2 +".key").model()).isNotNull().model()).
        then().expr(expr().concat().expr(expr().prop(prop1 +".key").model()).with().expr(expr().val(separator).model()).with().expr(expr().prop(prop2 +".key").model()).end().model()).
        when().condition(cond().expr(expr().prop(prop1 + ".key").model()).isNotNull().and().expr(expr().prop(prop2 + ".key").model()).isNull().model()).
        then().expr(expr().prop(prop1 + ".key").model()).
        when().expr(expr().prop(prop2 + ".key").model()).isNotNull().
        then().expr(expr().prop(prop2 + ".key").model()).
        otherwise().val(null).end().model();

        assertEquals(exp, act);
    }
}