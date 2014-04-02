package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class LevelAllocatingVisitorWithContextIndividualMethodsTest {

    @Test
    public void test_calc_of_name_node_level_with_collectional_property() throws SemanticException {
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");
        assertEquals("Incorrect level", new Integer(2), visitor.determineLevelForProperty("collectional.intProperty"));
        assertEquals("Incorrect level", new Integer(2), visitor.determineLevelForProperty("selfProperty.collectional.intProperty"));
    }

    @Test
    public void one2one_asociation_should_not_increment_level() throws SemanticException {
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation");
        assertEquals("Incorrect level", new Integer(1), visitor.determineLevelForProperty("←.intProp"));
    }

    @Test
    public void one2one_asociation_should_not_increment_level_even_if_its_one2many_is_a_context() throws SemanticException {
        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation");
        assertEquals("Incorrect level", new Integer(2), visitor.determineLevelForProperty("←.←.intProp"));
    }

    @Test
    public void test_calc_of_level_based_on_operands_with_agnostic_and_assigned_levels_out_of_context() throws SemanticException, RecognitionException {
        final Token intToken = new Token(EgTokenCategory.INT, "3");
        final AstNode intNode = new AstNode(intToken);

        final Token propToken = new Token(EgTokenCategory.NAME, "←.entityProperty.intProperty");
        final AstNode propNode = new AstNode(propToken);
        propNode.setLevel(1);

        final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
        final AstNode plusNode = new AstNode(plusToken);
        plusNode.addChild(intNode).addChild(propNode);

        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");
        assertEquals("Incorrect level", new Integer(1), visitor.determineLevelBasedOnOperands(plusNode));
    }

    @Test
    public void test_calc_of_level_based_on_operands_with_different_assigned_levels_out_of_context() throws SemanticException, RecognitionException {
        final Token colPropToken = new Token(EgTokenCategory.NAME, "←.collectional.intProperty");
        final AstNode colPropNode = new AstNode(colPropToken);
        colPropNode.setLevel(2);

        final Token propToken = new Token(EgTokenCategory.NAME, "←.entityProperty.intProperty");
        final AstNode propNode = new AstNode(propToken);
        propNode.setLevel(1);

        final Token plusToken = new Token(EgTokenCategory.PLUS, "+");
        final AstNode plusNode = new AstNode(plusToken);
        plusNode.addChild(colPropNode).addChild(propNode);

        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");

        try {
            visitor.determineLevelBasedOnOperands(plusNode);
            fail("Exception was expected.");
        } catch (final IncompatibleOperandException ex) {
        }
    }

    @Test
    public void test_calc_of_level_for_aggregation_functions_nodes_with_level_agnostic_operand() throws SemanticException, RecognitionException {
        final Token operandToken = new Token(EgTokenCategory.INT, "2");
        final AstNode operandNode = new AstNode(operandToken);

        final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
        final AstNode avgNode = new AstNode(avgToken);
        avgNode.addChild(operandNode);

        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");
        assertNull("Incorrect level", visitor.determineLevelBasedOnOperands(avgNode));
    }

    @Test
    public void test_calc_of_level_for_aggregation_functions_nodes_with_collectional_operand_out_of_context() throws SemanticException, RecognitionException {
        final Token operandToken = new Token(EgTokenCategory.NAME, "←.collectional.intProperty");
        final AstNode operandNode = new AstNode(operandToken);
        operandNode.setLevel(2);

        final Token avgToken = new Token(EgTokenCategory.AVG, "AVG");
        final AstNode avgNode = new AstNode(avgToken);
        avgNode.addChild(operandNode);

        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");
        assertEquals("Incorrect level", new Integer(1), visitor.determineLevelForAggregationOperations(avgNode));
    }

    @Test
    public void test_calc_of_level_for_aggregation_functions_nodes_without_collectional_operand_out_of_context() throws SemanticException, RecognitionException {
        final Token operandToken = new Token(EgTokenCategory.NAME, "←.entityProperty.intProperty");
        final AstNode operandNode = new AstNode(operandToken);
        operandNode.setLevel(1);

        final Token avgToken = new Token(EgTokenCategory.SUM, "SUM");
        final AstNode avgNode = new AstNode(avgToken);
        avgNode.addChild(operandNode);

        final LevelAllocatingVisitor visitor = new LevelAllocatingVisitor(EntityLevel1.class, "selfProperty");
        assertEquals("Incorrect level", new Integer(0), visitor.determineLevelForAggregationOperations(avgNode));
    }

}
