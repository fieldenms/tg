package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

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
    public void test_expression_level_calc_with_level_compatible_nodes_case_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("(2 + entityProperty.intProperty) / 3").tokenize();
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
	final Token[] tokens = new ExpressionLexer("(2 + ←.entityProperty.intProperty) / (3 - entityProperty.intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");

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
    public void test_expression_level_calc_with_level_compatible_nodes_case_6_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(←.←.collectional.intProperty) + SUM(collectional.intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty.selfProperty");

	new AstWalker(ast, visitor).walk();

	assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
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
    public void test_expression_level_calc_with_level_compatible_nodes_case_9() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(selfProperty.selfProperty.collectional.intProperty) + SUM(selfProperty.entityProperty.intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class);

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
	    assertEquals("Incorrect message", "Incompatible operand nesting level.", ex.getMessage());
	}
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_1_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("←.←.collectional.intProperty + collectional.intProperty / ←.entityProperty.intProperty").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty.selfProperty");
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("Exception expected.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level.", ex.getMessage());
	}
    }

    @Test
    public void test_expression_level_calc_with_level_compatible_nodes_case_2_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(←.←.collectional.intProperty) + SUM(←.entityProperty.intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty.selfProperty");

	new AstWalker(ast, visitor).walk();

	assertEquals("Incorrect level for expression", new Integer(1), ast.getLevel());
    }

    @Test
    public void should_be_able_to_add_calculated_property_to_collectinal_type_when_it_is_a_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("intProperty - SUM(←.←.←.collectional.intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty.selfProperty.collectional");

	new AstWalker(ast, visitor).walk();

	assertEquals("Incorrect level for expression", new Integer(2), ast.getLevel());
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_3_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(←.←.collectional.intProperty) + collectional.intProperty").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty.selfProperty");
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("Exception expected.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level.", ex.getMessage());
	}
    }

    @Test
    public void test_expression_level_calc_with_level_incompatible_nodes_case_4_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
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
