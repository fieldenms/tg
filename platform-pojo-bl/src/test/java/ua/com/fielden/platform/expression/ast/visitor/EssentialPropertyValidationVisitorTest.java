package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.InvalidPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.MissingPropertyException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class EssentialPropertyValidationVisitorTest {

    @Test
    public void test_missing_property_in_expression_with_no_properties() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 + 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final Exception ex) {
	    fail("There should have been no exceptions during AST walking.");
	}
    }

    @Test
    public void test_missing_property_in_expression_with_first_level_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 * (2 + strProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	final NodeByTokenExtractionVisitor extractor = new NodeByTokenExtractionVisitor(EgTokenCategory.NAME);
	try {
	    new AstWalker(ast, visitor, extractor).walk();
	} catch (final Exception ex) {
	    fail("There should have been no exceptions during AST walking.");
	}
	assertEquals("Incorrect number of property nodes", 1, extractor.getNodes().size());
    }

    @Test
    public void test_missing_property_in_expression_with_mistyped_first_level_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 * (2 + strPropert)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("There should have been no exceptions during AST walking.");
	} catch (final MissingPropertyException ex) {
	    assertEquals("Incorrect error message.", "Could not find property strPropert", ex.getMessage());
	}
    }

    @Test
    public void test_missing_property_in_expression_with_mistyped_first_level_property_out_of_context() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 * (2 + ←.strPropert)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation");
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("There should have been no exceptions during AST walking.");
	} catch (final MissingPropertyException ex) {
	    assertEquals("Incorrect error message.", "Could not find property key.strPropert", ex.getMessage());
	}
    }

    @Test
    public void test_missing_property_in_expression_with_first_and_second_level_properties() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("entityProperty.intProperty * (2 + strProperty) / selfProperty.selfProperty.entityProperty.intProperty").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	final NodeByTokenExtractionVisitor extractor = new NodeByTokenExtractionVisitor(EgTokenCategory.NAME);
	try {
	    new AstWalker(ast, visitor, extractor).walk();
	} catch (final Exception ex) {
	    fail("There should have been no exceptions during AST walking.");
	}
	assertEquals("Incorrect number of property nodes", 3, extractor.getNodes().size());
	assertEquals("Incorrectly identified property name", "entityProperty.intProperty", extractor.getNodes().get(0).getToken().text);
	assertEquals("Incorrectly identified property name", "strProperty", extractor.getNodes().get(1).getToken().text);
	assertEquals("Incorrectly identified property name", "selfProperty.selfProperty.entityProperty.intProperty", extractor.getNodes().get(2).getToken().text);
    }

    @Test
    public void test_missing_property_in_expression_with_some_missing_first_and_second_level_properties() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("entityProperty.intProperty * (2 + strProperty) / selfProperty.selfProperty.entityProperty.itProperty").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("There should have been no exceptions during AST walking.");
	} catch (final MissingPropertyException ex) {
	    assertEquals("Incorrect error message.", "Could not find property selfProperty.selfProperty.entityProperty.itProperty", ex.getMessage());
	}
    }

    @Test
    public void calculated_properties_should_not_be_permitted_as_part_of_expressions_at_this_stage() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("selfProperty.entityProperty.intProperty * selfProperty.calcuatedProperty").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("There should have been an exception during AST walking.");
	} catch (final InvalidPropertyException ex) {
	    assertEquals("Incorrect error message.", "Calculated properties cannot be used as part of expressions at this stage. Property selfProperty.calcuatedProperty is calculated.", ex.getMessage());
	}
    }

    @Test
    public void should_have_successfully_validated_expression_with_relative_properties_for_one_to_one_association() throws Exception {
	final Token[] tokens = new ExpressionLexer("←.intProp * (2 + intProp) / ←.intProp").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation");
	final NodeByTokenExtractionVisitor extractor = new NodeByTokenExtractionVisitor(EgTokenCategory.NAME);
	new AstWalker(ast, visitor, extractor).walk();

	assertEquals("Incorrect number of property nodes", 3, extractor.getNodes().size());
	assertEquals("Incorrectly identified property name", "←.intProp", extractor.getNodes().get(0).getToken().text);
	assertEquals("Incorrectly identified property name", "intProp", extractor.getNodes().get(1).getToken().text);
	assertEquals("Incorrectly identified property name", "←.intProp", extractor.getNodes().get(2).getToken().text);
    }

    @Test
    public void should_have_successfully_validated_expression_with_relative_propertie_for_one_to_one_association_with_one_to_many_special_case_association() throws Exception {
	final Token[] tokens = new ExpressionLexer("←.intProp * (2 + decimalProp) / ←.←.intProp").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final EssentialPropertyValidationVisitor visitor = new EssentialPropertyValidationVisitor(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation");
	final NodeByTokenExtractionVisitor extractor = new NodeByTokenExtractionVisitor(EgTokenCategory.NAME);
	new AstWalker(ast, visitor, extractor).walk();

	assertEquals("Incorrect number of property nodes", 3, extractor.getNodes().size());
	assertEquals("Incorrectly identified property name", "←.intProp", extractor.getNodes().get(0).getToken().text);
	assertEquals("Incorrectly identified property name", "decimalProp", extractor.getNodes().get(1).getToken().text);
	assertEquals("Incorrectly identified property name", "←.←.intProp", extractor.getNodes().get(2).getToken().text);
    }


}
