package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.SUPER;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.THIS;

import org.junit.Test;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class CollectionalContextVisitorIndividualMethodsTest {

    @Test
    public void test_calc_of_name_node_tag_with_collectional_property() throws SemanticException {
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", "selfProperty.collectional", visitor.determineTagForProperty("selfProperty.collectional.intProperty"));
	assertEquals("Incorrect tag", "selfProperty.collectional.collectional", visitor.determineTagForProperty("selfProperty.collectional.collectional.moneyProperty"));
	assertEquals("Incorrect tag", "selfProperty.selfProperty.collectional", visitor.determineTagForProperty("selfProperty.selfProperty.collectional.intProperty"));
	assertEquals("Incorrect tag", "selfProperty.selfProperty.collectional", visitor.determineTagForProperty("selfProperty.selfProperty.collectional.selfProperty.intProperty"));
	assertEquals("Incorrect tag", "selfProperty.selfProperty.collectional.selfProperty.collectional", visitor.determineTagForProperty("selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty"));
    }

    @Test
    public void test_calc_of_name_node_tag_without_collectional_property() throws SemanticException {
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", THIS, visitor.determineTagForProperty("selfProperty.strProperty"));
	assertEquals("Incorrect tag", THIS, visitor.determineTagForProperty("selfProperty.selfProperty.entityProperty.intProperty"));
    }

    @Test
    public void test_calc_tag_for_top_collectional_property() throws SemanticException {
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
	assertEquals("Incorrect tag", "collectional", visitor.determineTagForProperty("collectional.intProperty"));
    }

    @Test
    public void test_calc_tag_for_inner_collectional_property() throws SemanticException {
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
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

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
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

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
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

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);

	try {
	    visitor.determineTagBasedOnOperands(plusNode);
	    fail("Exception was expected.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect error message.", "Incompatible operand context for operation '+': 'this' is not compatible with 'selfProperty.selfProperty.collectional'.", ex.getMessage());

	}
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_tag_agnostic_operand() throws SemanticException {
	final Token operandToken = new Token(EgTokenCategory.INT, "2");
	final AstNode operandNode = new AstNode(operandToken);

	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operandNode);

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
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

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
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

	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
	assertEquals("Incorrect level", SUPER, visitor.determineTagForAggregationOperations(avgNode));
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_two_level_deep_collectional_operand_case_01() throws SemanticException {
	//////////////// create the property AST node associated with property of two-level deep collectional context ////////////////////
	final String property =  "selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty";
	final Token operandToken = new Token(EgTokenCategory.NAME, property);
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class);
	final String tag = visitor.determineTagForProperty(property);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional.selfProperty.collectional", tag);

	final AstNode operandNode = new AstNode(operandToken);
	operandNode.setTag(tag);

	///////////// create AVG function specific AST node /////////////////
	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operandNode);

	/////////////////// now check whether collectional context is calculated correctly for the AVG node ///////////////////
	assertEquals("Incorrect level", "selfProperty.selfProperty.collectional", visitor.determineTagForAggregationOperations(avgNode));
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_two_level_deep_collectional_operand_case_02() throws SemanticException {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// selfProperty.selfProperty.collectional.intProperty - AVG ( selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty ) ///
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class, "selfProperty.selfProperty.collectional");

	//////////////// create a property AST node associated with property of one-level deep collectional context ////////////////////
	final String property1 =  "intProperty";
	final Token operandToken1 = new Token(EgTokenCategory.NAME, property1);
	final String tag1 = visitor.determineTagForProperty(property1);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional", tag1);

	final AstNode operand1Node = new AstNode(operandToken1);
	operand1Node.setTag(tag1);

	//////////////// create a property AST node associated with property of two-level deep collectional context ////////////////////
	final String property2 =  "selfProperty.collectional.intProperty";
	final Token operandToken2 = new Token(EgTokenCategory.NAME, property2);
	final String tag2 = visitor.determineTagForProperty(property2);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional.selfProperty.collectional", tag2);

	final AstNode operand2Node = new AstNode(operandToken2);
	operand2Node.setTag(tag2);

	///////////// create AVG function specific AST node for operand 2 /////////////////
	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operand2Node);
	avgNode.setTag(visitor.determineTagForAggregationOperations(avgNode));

	///////////// create - operation specific AST node for operands 1 and 2 /////////////////
	final Token minusToken = new Token(EgTokenCategory.MINUS, "-");
	final AstNode minusNode = new AstNode(minusToken);
	minusNode.addChild(operand1Node);
	minusNode.addChild(avgNode);

	/////////////////// now check whether collectional context is calculated correctly for the AVG node ///////////////////
	assertEquals("Incorrect level", "selfProperty.selfProperty.collectional", visitor.determineTagBasedOnOperands(minusNode));
    }

    @Test
    public void test_calc_of_tag_for_operational_functions_nodes_with_two_level_deep_collectional_operand_case_03() throws SemanticException {
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// selfProperty.selfProperty.collectional.intProperty - selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty ///
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class, "selfProperty.selfProperty.collectional.selfProperty.collectional");

	//////////////// create a property AST node associated with property of two-levels up in respect to the expression context ////////////////////
	final String property1 =  "←.←.intProperty";
	final Token operandToken1 = new Token(EgTokenCategory.NAME, property1);
	final String tag1 = visitor.determineTagForProperty(property1);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional", tag1);

	final AstNode operand1Node = new AstNode(operandToken1);
	operand1Node.setTag(tag1);

	//////////////// create a property AST node associated with property of two-level deep collectional context ////////////////////
	final String property2 =  "intProperty";
	final Token operandToken2 = new Token(EgTokenCategory.NAME, property2);
	final String tag2 = visitor.determineTagForProperty(property2);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional.selfProperty.collectional", tag2);

	final AstNode operand2Node = new AstNode(operandToken2);
	operand2Node.setTag(tag2);

	///////////// create - operation specific AST node for operands 1 and 2 /////////////////
	final Token minusToken = new Token(EgTokenCategory.MINUS, "-");
	final AstNode minusNode = new AstNode(minusToken);
	minusNode.addChild(operand1Node);
	minusNode.addChild(operand2Node);

	/////////////////// now check whether collectional context is calculated correctly for the AVG node ///////////////////
	assertEquals("Incorrect level", "selfProperty.selfProperty.collectional.selfProperty.collectional", visitor.determineTagBasedOnOperands(minusNode));
    }

    @Test
    public void test_calc_of_tag_for_aggregation_functions_nodes_with_two_level_deep_collectional_operand_case_04() throws SemanticException {
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/// selfProperty.selfProperty.collectional.intProperty - selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty + ///
	/// AVG ( selfProperty.selfProperty.collectional.selfProperty.collectional.intProperty )                                                ///
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final CollectionalContextVisitor visitor = new CollectionalContextVisitor(EntityLevel1.class, "selfProperty.selfProperty.collectional.selfProperty.collectional");

	//////////////// create a property AST node associated with property of two-levels up in respect to the expression context ////////////////////
	final String property1 =  "←.←.intProperty";
	final Token operandToken1 = new Token(EgTokenCategory.NAME, property1);
	final String tag1 = visitor.determineTagForProperty(property1);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional", tag1);

	final AstNode operand1Node = new AstNode(operandToken1);
	operand1Node.setTag(tag1);

	//////////////// create a property AST node associated with property of two-level deep collectional context ////////////////////
	final String property2 =  "intProperty";
	final Token operandToken2 = new Token(EgTokenCategory.NAME, property2);
	final String tag2 = visitor.determineTagForProperty(property2);
	// make sure the tag has been calculated correctly
	assertEquals("Incorrect tag.", "selfProperty.selfProperty.collectional.selfProperty.collectional", tag2);

	final AstNode operand2Node = new AstNode(operandToken2);
	operand2Node.setTag(tag2);

	//////////////// create a property AST node for AVG function  ////////////////////
	final AstNode operand3Node = new AstNode(operandToken2);
	operand1Node.setTag(tag2);
	final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
	final AstNode avgNode = new AstNode(avgToken);
	avgNode.addChild(operand3Node);
	avgNode.setTag(visitor.determineTagForAggregationOperations(avgNode));


	///////////// create '-' operation specific AST node for operands 1 and 2 /////////////////
	final Token minusToken = new Token(EgTokenCategory.MINUS, "-");
	final AstNode minusNode = new AstNode(minusToken);
	minusNode.addChild(operand1Node);
	minusNode.addChild(operand2Node);
	minusNode.setTag(visitor.determineTagBasedOnOperands(minusNode));

	///////////// create '+' operation specific AST node for operands minusNode and avgNode /////////////////
	final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
	final AstNode plusNode = new AstNode(plusToken);
	plusNode.addChild(minusNode);
	plusNode.addChild(avgNode);

	/////////////////// now check whether collectional context is calculated correctly for the expression root node ///////////////////
	assertEquals("Incorrect level", "selfProperty.selfProperty.collectional.selfProperty.collectional", visitor.determineTagBasedOnOperands(plusNode));
    }

}
