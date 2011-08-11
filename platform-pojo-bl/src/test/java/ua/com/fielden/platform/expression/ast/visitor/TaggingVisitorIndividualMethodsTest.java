package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor.ABOVE;
import static ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor.THIS;

import org.junit.Test;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class TaggingVisitorIndividualMethodsTest {

    @Test
    public void test_calc_of_name_node_tag_with_collectional_property() throws SemanticException {
	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", "selfProperty.collectional", visitor.determineTagForProperty("selfProperty.collectional.intProperty"));
	assertEquals("Incorrect tag", "selfProperty.collectional.collectional", visitor.determineTagForProperty("selfProperty.collectional.collectional.moneyProperty"));
	assertEquals("Incorrect tag", "selfProperty.selfProperty.collectional", visitor.determineTagForProperty("selfProperty.selfProperty.collectional.intProperty"));

    }

    @Test
    public void test_calc_of_name_node_tag_without_collectional_property() throws SemanticException {
	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", THIS, visitor.determineTagForProperty("selfProperty.strProperty"));
	assertEquals("Incorrect tag", THIS, visitor.determineTagForProperty("selfProperty.selfProperty.entityProperty.intProperty"));
    }

    @Test
    public void test_calc_tag_for_top_collectional_property() throws SemanticException {
	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", "collectional", visitor.determineTagForProperty("collectional.intProperty"));
    }

    @Test
    public void test_calc_tag_for_inner_collectional_property() throws SemanticException {
	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", "collectional.collectional", visitor.determineTagForProperty("collectional.collectional.moneyProperty"));
    }

    @Test
    public void test_calc_of_tag_based_on_tag_agnostic_operands() throws SemanticException {
	final Token intToken = new Token(EgTokenCategory.INT, "3");
	final AstNode intNode = new AstNode(intToken);

	final Token decToken = new Token(EgTokenCategory.DECIMAL, "3.5");
	final AstNode decNode = new AstNode(decToken);

	final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
	final AstNode plusNode = new AstNode(plusToken);
	plusNode.addChild(intNode).addChild(decNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertNull("Incorrect level", visitor.determineTagBasedOnOperands(plusNode));
    }

    @Test
    public void test_calc_of_tag_based_on_operands_with_agnostic_and_assigned_tags() throws SemanticException {
	final Token intToken = new Token(EgTokenCategory.INT, "3");
	final AstNode intNode = new AstNode(intToken);

	final Token propToken = new Token(EgTokenCategory.NAME, "selfProperty.selfProperty.entityProperty.intProperty");
	final AstNode propNode = new AstNode(propToken);
	propNode.setTag(THIS);

	final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
	final AstNode plusNode = new AstNode(plusToken);
	plusNode.addChild(intNode).addChild(propNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect level", THIS, visitor.determineTagBasedOnOperands(plusNode));
    }

    @Test
    public void test_calc_of_tag_based_on_operands_with_different_assigned_tags() throws SemanticException {
	final Token colPropToken = new Token(EgTokenCategory.NAME, "selfProperty.selfProperty.collectional.intProperty");
	final AstNode colPropNode = new AstNode(colPropToken);
	colPropNode.setTag("selfProperty.selfProperty.collectional");

	final Token propToken = new Token(EgTokenCategory.NAME, "selfProperty.selfProperty.entityProperty.intProperty");
	final AstNode propNode = new AstNode(propToken);
	propNode.setTag(THIS);

	final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
	final AstNode plusNode = new AstNode(plusToken);
	plusNode.addChild(colPropNode).addChild(propNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);

	try {
	    visitor.determineTagBasedOnOperands(plusNode);
	    fail("Exception was expected.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect error message.", "Incompatible operand context for operation '+'", ex.getMessage());

	}
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_tag_agnostic_operand() throws SemanticException {
	final Token operandToken = new Token(EgTokenCategory.INT, "2");
	final AstNode operandNode = new AstNode(operandToken);

	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operandNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertNull("Incorrect level", visitor.determineTagBasedOnOperands(avgNode));
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_collectional_operand() throws SemanticException {
	final Token operandToken = new Token(EgTokenCategory.NAME, "selfProperty.selfProperty.collectional.intProperty");
	final AstNode operandNode = new AstNode(operandToken);
	operandNode.setTag("selfProperty.selfProperty.collectional");

	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operandNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect level", THIS, visitor.determineTagForAggregationOperations(avgNode));
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_non_collectional_operand() throws SemanticException {
	final Token operandToken = new Token(EgTokenCategory.NAME, "selfProperty.selfProperty.entityProperty.intProperty");
	final AstNode operandNode = new AstNode(operandToken);
	operandNode.setTag(THIS);

	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operandNode);

	final TaggingVisitor visitor = new TaggingVisitor(EntityLevel1.class);
	assertEquals("Incorrect level", ABOVE, visitor.determineTagForAggregationOperations(avgNode));
    }

}
