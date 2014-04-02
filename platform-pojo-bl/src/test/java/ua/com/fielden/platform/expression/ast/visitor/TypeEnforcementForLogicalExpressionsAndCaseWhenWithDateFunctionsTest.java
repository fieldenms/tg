package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;

public class TypeEnforcementForLogicalExpressionsAndCaseWhenWithDateFunctionsTest {

    @Test
    public void test_type_determination_for_logical_expression_case1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("SUM(intProperty) > AVG(intProperty) * 10").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_DAYS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProp, now)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_comparison_expression_with_DAYS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProp, now) > 10").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void it_should_be_possible_to_compare_DAYS_with_a_day_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProp, now) > 10d").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void it_should_not_be_possible_to_compare_DAYS_with_a_month_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProp, now) > 10m").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("Type incompatibility should have been determined.");
        } catch (final UnsupportedTypeException ex) {
        }
    }

    @Test
    public void it_should_be_possible_to_compare_DAYS_with_a_number() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProp, now) > 10.5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_AVG_DAYS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(days(dateProp, now))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_comparison_expression_with_AVG_DAYS_function_and_YEARS_function() throws RecognitionException, SequenceRecognitionFailed,
            SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(YEARS(dateProp, now)) > 2").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_expression_with_MONTHS_function_and_MONTH_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("MONTHS(dateProp, NOW) + MONTH(NOW)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN \"word\" ELSE \"word\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_case_expression_with_date_functions() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("" + "CASE WHEN MONTHS(dateProp, NOW) > 3m THEN \"Green\" \n"
                + "     WHEN MONTHS(dateProp, NOW) < 3m && MONTHS(dateProp, NOW) > 1m THEN \"Yellow\" \n" + "ELSE \"Red\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(moneyProperty) < 12").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("12.4 >= AVG(moneyProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(2 / (moneyProperty / decimalProperty) / 3.5) > moneyProperty || (23 > 5)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(2 / (moneyProperty / decimalProperty) / 3.5) > moneyProperty || (\"word\" > 5)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("Should have detected incompatible operand types.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void test_type_determination_for_logical_expression_with_date_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("dateProperty > '2012-02-28'").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_with_date_literal_and_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("days(dateProperty, '2012-02-28') < 2d").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", boolean.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }
}
