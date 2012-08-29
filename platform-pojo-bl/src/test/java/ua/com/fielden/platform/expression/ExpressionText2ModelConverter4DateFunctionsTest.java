package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class ExpressionText2ModelConverter4DateFunctionsTest {

    @Test
    public void model_generation_for_days_functions_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "DAYS(dateProperty, dateProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().count().days().between().prop("dateProperty").and().prop("dateProperty").model();
	final ExpressionModel model = expr().expr(func).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_months_functions_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MONTHS('2012-08-29', dateProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final Date date = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime("2012-08-29").toDate();
	final ExpressionModel func = expr().count().months().between().val(date).and().prop("dateProperty").model();
	final ExpressionModel model = expr().expr(func).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_years_functions_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "YEARS(dateProperty, NOW) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().count().years().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ExpressionModel model = expr().expr(func).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_years_and_aggregation_functionas_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(MONTHS(MAX(collectional.dateProperty), NOW)) + SUM(intProperty)) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.dateProperty").model();
	final ExpressionModel fun = expr().count().months().between().expr(max).and().expr(expr().now().model()).model();
	final ExpressionModel countOfDays = expr().countOfDistinct().expr(fun).model();
	final ExpressionModel sum = expr().sumOf().prop("intProperty").model();
	final ExpressionModel plus1 = expr().expr(countOfDays).add().expr(sum).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }
}
