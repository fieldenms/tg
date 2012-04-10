package ua.com.fielden.platform.expression;

import java.math.BigDecimal;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ExpressionText2ModelConverter4Subqueries {
    @Test
    @Ignore
    public void test_model_creation_for_vehicle_property_as_sum_of_collectional_fuel_usages_association() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(TgVehicle.class, "SUM(fuelUsages.qty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	final ExpressionModel sum = expr().sumOf().prop("qty").model();
	final ExpressionModel em = expr().model(select(TgFuelUsage.class).where().prop("vehicle").eq().prop("$$$1"). //
		yield().expr(sum).modelAsPrimitive()).model();
	assertEquals("Incorrect model.", em, root.getModel());
    }

    @Test
    @Ignore
    public void test_case_37a() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		EntityLevel1.class, // higher-order type
		"selfProperty.collectional", // expression context
		"2 * intProperty + ←.intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression collectional context", "selfProperty.collectional", root.getTag());

	final ExpressionModel mult = expr().val(2).mult().prop("selfProperty.collectional.intProperty").model();
	final ExpressionModel plus = expr().expr(mult).add().prop("selfProperty.intProperty").model();
	assertEquals("Incorrect model.", plus, root.getModel());
    }

}
