package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.types.Money;

public class TypeEnforcementForCaseWhenTest {

    @Test
    public void CASE_with_single_WHEN_THEN_and_ELSE_having_String_operands_resolves_to_String() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN \"word\" ELSE \"word\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void CASE_with_single_WHEN_THEN_and_ELSE_having_Integer_and_BigDecimal_operands_due_to_AVG_resolves_to_BigDecimal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN AVG(decimalProperty) ELSE 0 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void CASE_with_single_WHEN_THEN_and_ELSE_having_Integer_operands_and_SUM_resolves_to_Integer() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN SUM(intProperty) ELSE 0 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void CASE_with_single_WHEN_THEN_and_ELSE_having_Money_and_Integer_operands_with_SUM_resolves_to_Money() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN SUM(moneyProperty) ELSE 0 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void CASE_with_nested_WHEN_THEN_and_ELSE_having_Integer_and_BigDecimal_operands_resolves_to_BigDecimal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN MONTHS(dateProp, NOW) > 3m THEN 42 \n"
                + "     WHEN MONTHS(dateProp, NOW) < 3m && MONTHS(dateProp, NOW) > 1m THEN 12 / 4 \n" + "ELSE 23 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test(expected = TypeCompatibilityException.class)
    public void CASE_with_incompatible_operand_types_fails_type_resolution() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN MONTHS(dateProp, NOW) > 3m THEN \"string\" \n"
                + "     WHEN MONTHS(dateProp, NOW) < 3m && MONTHS(dateProp, NOW) > 1m THEN 12 / 4 \n" + "ELSE 23 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
    }

}
