package ua.com.fielden.platform.entity.query.metadata;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.generateUnionEntityPropertyExpression;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;

public class DomainMetadataUtilityTest extends BaseEntQueryTCase {

    @Test
    public void test_union_entity_key_prop_model_generation() throws Exception {
        final ExpressionModel exp = expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.key").when().prop("workshop").isNotNull().then().prop("workshop.key").otherwise().val(null).end().model();
        final ExpressionModel act = generateUnionEntityPropertyExpression(TgBogieLocation.class, "key");
        assertEquals(exp, act);
    }

    @Test
    public void test_union_entity_id_prop_model_generation() throws Exception {
        final ExpressionModel exp = expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot.id").when().prop("workshop").isNotNull().then().prop("workshop.id").otherwise().val(null).end().model();
        final ExpressionModel act = generateUnionEntityPropertyExpression(TgBogieLocation.class, "id");
        assertEquals(exp, act);
    }
}