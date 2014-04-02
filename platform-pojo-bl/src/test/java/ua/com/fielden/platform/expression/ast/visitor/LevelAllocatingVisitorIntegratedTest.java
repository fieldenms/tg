package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class LevelAllocatingVisitorIntegratedTest {

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + 6) / 3").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();

        new AstWalker(ast, new LevelAllocatingVisitor(EntityLevel1.class)).walk();

        assertNull("Incorrect level for expression", ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_1_logical_expression() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + 6) > 3").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();

        new AstWalker(ast, new LevelAllocatingVisitor(EntityLevel1.class)).walk();

        assertNull("Incorrect level for expression", ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + entityProperty.intProperty) / 3").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();

        new AstWalker(ast, new LevelAllocatingVisitor(EntityLevel1.class)).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_10() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("COUNT(MONTH(collectional.dateProperty) + MONTH(selfProperty.collectional.dateProperty))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();

        new AstWalker(ast, new LevelAllocatingVisitor(EntityLevel1.class)).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_10_logical_expression() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("COUNT(MONTH(collectional.dateProperty) + MONTH(selfProperty.collectional.dateProperty)) >= 200").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();

        new AstWalker(ast, new LevelAllocatingVisitor(EntityLevel1.class)).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + entityProperty.intProperty) / (3 - selfProperty.entityProperty.intProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_3_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + ←.intProp) / (3 - intProp)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation");

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG((2 + entityProperty.intProperty) / (3 - selfProperty.entityProperty.intProperty))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(0), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_4_case_when() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("CASE WHEN AVG((2 + entityProperty.intProperty) / (3 - selfProperty.entityProperty.intProperty)) > 23 && SUM(entityProperty.intProperty) < 1000  THEN \"word\" "
                + "WHEN SUM(entityProperty.intProperty) > 1000  THEN \"word\" ELSE \"word\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(0), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(selfProperty.selfProperty.collectional.intProperty) / selfProperty.entityProperty.intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_5_within_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(selfProperty.collectional.intProperty) / entityProperty.intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(selfProperty.selfProperty.collectional.intProperty) + SUM(selfProperty.selfProperty.collectional.intProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_with_one2many_associations() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("←.moneyProp + SUM(one2manyAssociationCollectional.key2)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional");

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(2), ast.getLevel());
    }

    @Test
    public void test_expression_level_for_DAYS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(days(dateProp, now))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect level for expression", new Integer(0), ast.getLevel());
    }

    @Test
    public void test_expression_level_for_MONTHS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("months(dateProp, anotherDateProp)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_for_YEARS_function() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("Years(NOW, dateProp) <= 1y").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional");
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect level for expression", new Integer(2), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_7() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(2) + SUM(selfProperty.selfProperty.collectional.intProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_8() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG( SUM(selfProperty.selfProperty.collectional.intProperty) - SUM(selfProperty.selfProperty.collectional.intProperty) )").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(0), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_in_one2many_associations() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("AVG(one2manyAssociationSpecialCase.one2manyAssociationCollectional.key2) + SUM(one2manyAssociationCollectional.key2)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class);

        new AstWalker(ast, visitor).walk();

        assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("selfProperty.selfProperty.collectional.intProperty + selfProperty.selfProperty.collectional.intProperty / selfProperty.entityProperty.intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("Exception expected.");
        } catch (final IncompatibleOperandException ex) {
            assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '/'.", ex.getMessage());
        }
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_1_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("←.moneyProp / one2manyAssociationCollectional.key2").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase");
        try {
            new AstWalker(ast, visitor).walk();
            fail("Exception expected.");
        } catch (final IncompatibleOperandException ex) {
            assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '/'.", ex.getMessage());
        }
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_3_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("collectional.intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("Exception expected.");
        } catch (final IncompatibleOperandException ex) {
            assertEquals("Incorrect message", "Resultant expression level is incompatible with the context.", ex.getMessage());
        }
    }

}
