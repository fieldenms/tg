package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.type.Day;
import ua.com.fielden.platform.expression.type.Month;
import ua.com.fielden.platform.expression.type.Year;

public class ExpressionText2ModelConverter4LiteralsTest {

    @Test
    public void expression_should_be_recognised_as_valmodel_for_string() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"string\"");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", String.class, root.getType());

        final ExpressionModel model = expr().val("string").model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_empty_string() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"\"");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", String.class, root.getType());

        final ExpressionModel model = expr().val("").model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_int() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "22");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Integer.class, root.getType());

        final ExpressionModel model = expr().val(22).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_decimal() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "22.2");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

        final ExpressionModel model = expr().val(new BigDecimal("22.2")).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_day() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "2d");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Day.class, root.getType());

        final ExpressionModel model = expr().val(2).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_month() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "2m");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Month.class, root.getType());

        final ExpressionModel model = expr().val(2).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_year() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "23y");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Year.class, root.getType());

        final ExpressionModel model = expr().val(23).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_date() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "'2012-08-29'");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Date.class, root.getType());

        final ExpressionModel model = expr().val(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime("2012-08-29").toDate()).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_date_time() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "'2012-08-29 10:00:00'");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Date.class, root.getType());

        final ExpressionModel model = expr().val(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime("2012-08-29 10:00:00").toDate()).model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

}
