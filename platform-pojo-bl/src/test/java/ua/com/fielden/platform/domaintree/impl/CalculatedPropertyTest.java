package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ALL;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ANY;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.domaintree.impl.CalculatedProperty.createEmpty;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;

/**
 * A test for {@link CalculatedProperty}.
 *
 * @author TG Team
 *
 */
public class CalculatedPropertyTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_CalculatedPropertyTest() {
	return createRootTypes_for_AbstractDomainTreeTest();
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_CalculatedPropertyTest(final IDomainTreeManager dtm) {
	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	// dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IDomainTreeManagerAndEnhancer dtm = new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_CalculatedPropertyTest());
	manageTestingDTM_for_CalculatedPropertyTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// Utilities ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected CalculatedProperty correctCalculatedPropertyCreation(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final CalculatedProperty calc = CalculatedProperty.createCorrect(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	checkTrivialParams(calc, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	return calc;
    }

    protected ICalculatedProperty checkTrivialParams(final CalculatedProperty calc, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer enhancer) {
	assertEquals("The root is incorrect.", root, calc.getRoot());
	assertEquals("The contextPath is incorrect.", contextPath, calc.getContextPath());
	assertEquals("The contextualExpression is incorrect.", contextualExpression, calc.getContextualExpression());
	assertEquals("The title is incorrect.", title, calc.getTitle());
	assertEquals("The desc is incorrect.", desc, calc.getDesc());
	assertEquals("The attribute is incorrect.", attribute, calc.getAttribute());
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.getOriginationProperty());
	assertEquals("The enhancer is incorrect.", enhancer, calc.getEnhancer());
	return calc;
    }

    protected CalculatedProperty assertCalculatedProperty(final CalculatedProperty calc, final CalculatedPropertyCategory category, final String name, final String path, final String pathAndName, final Class<?> contextType, final Class<?> parentType, final Class<?> resultType) {
	assertEquals("The category is incorrect.", category, calc.category());
	assertEquals("The name is incorrect.", name, calc.name());
	assertEquals("The path is incorrect.", path, calc.path());
	assertEquals("The pathAndName is incorrect.", pathAndName, calc.pathAndName());
	assertEquals("The contextType is incorrect.", contextType, calc.contextType());
	assertEquals("The parentType is incorrect.", parentType, calc.parentType());
	assertEquals("The resultType is incorrect.", resultType, calc.resultType());
	return calc;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// 1. Creation //////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// 1. 1. Empty //////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_empty_calculated_property_creation_with_null_Factory_fails() {
	try {
	    createEmpty(null, MasterEntity.class, "", dtm().getEnhancer());
	    fail("Cannot create calc property without Factory.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_null_Enhancer_fails() {
	try {
	    createEmpty(factory(), MasterEntity.class, "", null);
	    fail("Cannot create calc property without Enhancer.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_Root_type_not_present_in_enhancer_fails() {
	// null
	try {
	    createEmpty(factory(), null, "", dtm().getEnhancer());
	    fail("Cannot create calc property without Root type.");
	} catch (final IncorrectCalcPropertyException e) {
	}

	// Class type -- even non-entity-typed
	try {
	    createEmpty(factory(), CalculatedPropertyTest.class, "", dtm().getEnhancer());
	    fail("Cannot create calc property with non-Root type of enhancer.");
	} catch (final IncorrectCalcPropertyException e) {
	}

	// Non-root-type from enhancer
	try {
	    createEmpty(factory(), SlaveEntity.class, "", dtm().getEnhancer());
	    fail("Cannot create calc property with non-Root type of enhancer.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_non_existent_ContextPath_fails() {
	// null
	try {
	    createEmpty(factory(), MasterEntity.class, null, dtm().getEnhancer());
	    fail("Cannot create calc property without ContextPath.");
	} catch (final IncorrectCalcPropertyException e) {
	}

	// non-existent
	try {
	    createEmpty(factory(), MasterEntity.class, "non_existent_prop.non_existent_sub_prop", dtm().getEnhancer());
	    fail("Cannot create calc property with non-existent ContextPath.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_non_entity_typed_ContextPath_fails() {
	// Non-entity-typed contextPath
	try {
	    createEmpty(factory(), MasterEntity.class, "integerProp", dtm().getEnhancer());
	    fail("Cannot create calc property with Non-entity-typed contextPath.");
	} catch (final IncorrectCalcPropertyException e) {
	}
	try {
	    createEmpty(factory(), MasterEntity.class, "entityProp.integerProp", dtm().getEnhancer());
	    fail("Cannot create calc property with Non-entity-typed contextPath.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_appropriate_parameters_does_not_fail() {
	createEmpty(factory(), MasterEntity.class, "", dtm().getEnhancer());
	createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());
    }

    private static void assertMetaState(final CalculatedProperty cp, final String property, final boolean required, final boolean editable, final boolean valid) {
	assertEquals("Incorrect requiredness for property [" + property + "].", required, cp.getProperty(property).isRequired());
	assertEquals("Incorrect editablity for property [" + property + "].", editable, cp.getProperty(property).isEditable());
	assertEquals("Incorrect validity for property [" + property + "].", valid, cp.getProperty(property).isValid());
    }

    @Test
    public void test_that_empty_calculated_property_creation_with_correct_parameters_initialises_whole_appropriate_state() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, SlaveEntity.class, null, null);

	assertMetaState1(cp);
    }

    @Test
    public void test_that_validation_upon_empty_calculated_property_is_appropriate() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	assertFalse("Should be not successful.", cp.isValid().isSuccessful());
	assertFalse("Expression should be incorrect.", cp.getProperty("contextualExpression").isValidWithRequiredCheck());
	assertEquals("Should be equal.", cp.isValid(), cp.getProperty("contextualExpression").getFirstFailure());

	cp.setContextualExpression("2 * integerProp");
	assertFalse("Should still be not successful.", cp.isValid().isSuccessful());
	assertFalse("Title should be incorrect.", cp.getProperty("title").isValidWithRequiredCheck());
	assertEquals("Should be equal.", cp.isValid(), cp.getProperty("title").getFirstFailure());

	cp.setTitle("New calculated property");
	assertTrue("Should become successful.", cp.isValid().isSuccessful());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// 1. 2. Copying ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_Not_Assigned_calculated_property_copying_is_not_permitted() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp").setTitle("Calculated property");
	assertTrue("Should be valid.", cp.isValid().isSuccessful());
	try {
	    cp.copy();
	    fail("Calc property cannot be copied if it is not added to enhancer.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_Invalid_calculated_property_copying_is_not_permitted() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	assertFalse("Should be invalid.", cp.isValid().isSuccessful());
	try {
	    cp.copy();
	    fail("Calc property cannot be copied if it is not added to enhancer.");
	} catch (final IncorrectCalcPropertyException e) {
	}
    }

    @Test
    public void test_that_calculated_property_copying_creates_a_copy_with_the_same_invalid_title() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp").setTitle("Calculated property");
	assertTrue("Should be valid.", cp.isValid().isSuccessful());
	dtm().getEnhancer().addCalculatedProperty(cp);

	final CalculatedProperty copy = (CalculatedProperty) dtm().getEnhancer().copyCalculatedProperty(MasterEntity.class, "entityProp.calculatedProperty");
	assertFalse("Should be not successful.", copy.isValid().isSuccessful());
	assertFalse("Title should be incorrect (the same as original calc prop).", copy.getProperty("title").isValidWithRequiredCheck());
	assertEquals("Should be equal.", copy.isValid(), copy.getProperty("title").getFirstFailure());

	checkTrivialParams(copy, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(copy, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(copy, "root", true, false, true);
	assertMetaState(copy, "contextPath", false, false, true);
	assertMetaState(copy, "contextualExpression", true, true, true);
	assertMetaState(copy, "title", true, true, false);
	assertMetaState(copy, "attribute", false, false, true);
	assertMetaState(copy, "originationProperty", false, true, true);

	copy.setTitle("Calculated property Copy");
	assertTrue("Should become successful.", copy.isValid().isSuccessful());

	checkTrivialParams(copy, MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property Copy", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(copy, EXPRESSION, "calculatedPropertyCopy", "entityProp", "entityProp.calculatedPropertyCopy", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState1(copy);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// 2. Mutation //////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// 2. 1. contextualExpression ////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void assertMetaState0(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, false);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_ContextualExpression_mutation_makes_it_invalid_for_null_or_empty_expression() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");

	cp.setContextualExpression("");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState0(cp);

	cp.setContextualExpression("3 * integerProp");

	cp.setContextualExpression(null);

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "3 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState0(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_makes_it_invalid_for_incorrect_expression() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerPropUnknown");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, SlaveEntity.class, null, null);
	assertMetaState0(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_makes_it_invalid_for_TwiceOrMore_Aggregated_simple_expression() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	cp.setContextualExpression("MAX(SUM(integerProp))");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, SlaveEntity.class, null, null);
	assertMetaState0(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_makes_it_invalid_for_Aggregated_collectional_expression_temporarily_at_this_stage() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());

	cp.setContextualExpression("SUM(integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "collection", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, SlaveEntity.class, null, null);
	assertMetaState0(cp);
    }

    private void assertMetaState1(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_EXPRESSION_at_1_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "", null, MasterEntity.class, MasterEntity.class, Integer.class);
	assertMetaState1(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState1(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_EXPRESSION_at_3_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp.entityProp", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState1(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_EXPRESSION_for_expression_with_undefined_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * 3 - 17");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * 3 - 17", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState1(cp);
    }

    private void assertMetaState2(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", true, true, true);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_EXPRESSION_at_1_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, MasterEntity.class, MasterEntity.class, Integer.class);
	assertMetaState2(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState2(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_EXPRESSION_at_3_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.entityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	assertMetaState2(cp);
    }

    private void assertMetaState3(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, true, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_COLLECTIONAL_EXPRESSION_at_1_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "collection", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, COLLECTIONAL_EXPRESSION, null, "collection", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_COLLECTIONAL_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, COLLECTIONAL_EXPRESSION, null, "entityProp.collection", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_COLLECTIONAL_EXPRESSION_at_3_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection.slaveEntityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, COLLECTIONAL_EXPRESSION, null, "entityProp.collection.slaveEntityProp", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_COLLECTIONAL_EXPRESSION_for_collectional_expression_with_undefined_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * 3 - 17");

	checkTrivialParams(cp, MasterEntity.class, "collection", "2 * 3 - 17", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, COLLECTIONAL_EXPRESSION, null, "collection", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    private void assertMetaState4(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test @Ignore
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_COLLECTIONAL_EXPRESSION_at_1_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_COLLECTIONAL_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState4(cp);
    }

    @Test @Ignore
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_COLLECTIONAL_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_COLLECTIONAL_EXPRESSION, null, "entityProp", null, EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState4(cp);
    }

    @Test @Ignore
    public void test_that_ContextualExpression_mutation_forms_simple_AGGREGATED_COLLECTIONAL_EXPRESSION_at_3_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection.slaveEntityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_COLLECTIONAL_EXPRESSION, null, "entityProp", null, EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState4(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_ATTRIBUTED_COLLECTIONAL_EXPRESSION_at_1_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");
	cp.setAttribute(ALL);

	checkTrivialParams(cp, MasterEntity.class, "collection", "2 * integerProp", null, null, ALL, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, ATTRIBUTED_COLLECTIONAL_EXPRESSION, null, "collection", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_ATTRIBUTED_COLLECTIONAL_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");
	cp.setAttribute(ANY);

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", "2 * integerProp", null, null, ANY, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, ATTRIBUTED_COLLECTIONAL_EXPRESSION, null, "entityProp.collection", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_forms_simple_ATTRIBUTED_COLLECTIONAL_EXPRESSION_at_3_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection.slaveEntityProp", dtm().getEnhancer());

	cp.setContextualExpression("2 * integerProp");
	cp.setAttribute(ALL);

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", null, null, ALL, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, ATTRIBUTED_COLLECTIONAL_EXPRESSION, null, "entityProp.collection.slaveEntityProp", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    @Test @Ignore
    public void test_that_ContextualExpression_mutation_resets_Attribute_when_property_becomes_AGGREGATED_COLLECTIONAL_EXPRESSION() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection.slaveEntityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");
	cp.setAttribute(ALL);

	cp.setContextualExpression("MAX(2 * integerProp)");

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection.slaveEntityProp", "MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_COLLECTIONAL_EXPRESSION, null, "entityProp", null, EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState4(cp);
    }

    @Test
    public void test_that_ContextualExpression_mutation_causes_Title_revalidation() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setTitle("String prop");

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", "String prop", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, "stringProp", "entityProp", "entityProp.stringProp", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, false);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_ContextualExpression_mutation_causes_OriginationProperty_revalidation() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setOriginationProperty("bigDecimalProp");

	cp.setContextualExpression("2 * integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, "bigDecimalProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);

	assertTrue("Incorrect warning for property [originationProperty].", cp.getProperty("originationProperty").getFirstWarning() instanceof CalcPropertyWarning);
	assertTrue("Incorrect warning for property [originationProperty].", cp.getProperty("originationProperty").hasWarnings());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// 2. 2. title ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void assertMetaState5(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, false);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_Title_mutation_makes_it_invalid_for_null_or_empty_title() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setTitle("Calc");

	cp.setTitle("");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "Calc", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calc", null, null, SlaveEntity.class, null, null);
	assertMetaState5(cp);

	cp.setTitle("Calculated");

	cp.setTitle(null);

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "Calculated", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calculated", null, null, SlaveEntity.class, null, null);
	assertMetaState5(cp);
    }

    @Test
    public void test_that_Title_mutation_makes_it_invalid_for_title_without_letters() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setTitle("Calc");

	cp.setTitle("-  % ^% &^ %&78434 213 321123-% ^% &^ %&% ");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "Calc", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calc", null, null, SlaveEntity.class, null, null);
	assertMetaState5(cp);
    }

    @Test
    public void test_that_Title_mutation_makes_it_invalid_for_title_that_already_exists_in_enhancer() {
	final CalculatedProperty old = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	old.setContextualExpression("2 * integerProp").setTitle("Old");
	dtm().getEnhancer().addCalculatedProperty(old);

	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("3 * integerProp");

	cp.setTitle("Old");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "3 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState5(cp);
    }

    @Test
    public void test_that_Title_mutation_makes_it_invalid_for_title_that_already_exists_in_original_domain() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");

	cp.setTitle("Integer prop");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState5(cp);
    }

    @Test
    public void test_that_Title_mutation_populates_appropriate_cropped_name() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setTitle("Calculated property");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "Calculated property", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calculatedProperty", null, null, SlaveEntity.class, null, null);
	assertMetaState1(cp);

	cp.setTitle("CalCulaTed pRopertY");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "CalCulaTed pRopertY", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calCulaTedPRopertY", null, null, SlaveEntity.class, null, null);
	assertMetaState1(cp);

	cp.setTitle("-  % ^% &^ %&Calculated property-% ^% &^ %&% ");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "-  % ^% &^ %&Calculated property-% ^% &^ %&% ", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calculatedProperty", null, null, SlaveEntity.class, null, null);
	assertMetaState1(cp);

	cp.setTitle("-  % ^% &^ %&   87 87Calc$^&^$%&^ulated pro&^%^&perty 56 bum 71 -% ^% &^ %&% ");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", null, "-  % ^% &^ %&   87 87Calc$^&^$%&^ulated pro&^%^&perty 56 bum 71 -% ^% &^ %&% ", null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, "calculatedProperty56Bum71", null, null, SlaveEntity.class, null, null);
	assertMetaState1(cp);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// 2. 3. attribute ///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void assertMetaState6(final CalculatedProperty cp) {
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, false);
	assertMetaState(cp, "originationProperty", false, true, true);
    }

    @Test
    public void test_that_Attribute_mutation_makes_it_invalid_for_null_attribute() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());

	cp.setAttribute(null);

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, EvenSlaverEntity.class, null, null);
	assertMetaState6(cp);
    }

    @Test
    public void test_that_Attribute_mutation_makes_it_invalid_for_undefined_category() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());

	cp.setAttribute(ANY);

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", null, null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, null, null, null, null, EvenSlaverEntity.class, null, null);
	assertMetaState6(cp);
    }

    @Test
    public void test_that_Attribute_mutation_makes_it_invalid_for_non_EXPRESSION_category_and_ALL_or_ANY_attribute() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");

	cp.setAttribute(ANY);

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState6(cp);
    }

    @Test
    public void test_that_Attribute_mutation_makes_it_invalid_for_non_AGGREGATED_EXPRESSION_category_and_ALL_or_ANY_attribute() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("MAX(2 * integerProp)");

	cp.setAttribute(ALL);

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, false);
	assertMetaState(cp, "originationProperty", true, true, true);
    }

    @Test @Ignore
    public void test_that_Attribute_mutation_makes_it_invalid_for_non_AGGREGATED_COLLECTIONAL_EXPRESSION_category_and_ALL_or_ANY_attribute() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "collection", dtm().getEnhancer());
	cp.setContextualExpression("MAX(2 * integerProp)");

	cp.setAttribute(ANY);

	checkTrivialParams(cp, MasterEntity.class, "collection", "MAX(2 * integerProp)", null, null, NO_ATTR, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_COLLECTIONAL_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, false);
	assertMetaState(cp, "originationProperty", true, true, true);
    }

    @Test
    public void test_that_Attribute_mutation_forms_simple_ATTRIBUTED_COLLECTIONAL_EXPRESSION_at_2_level() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp.collection", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");

	cp.setAttribute(ANY);

	checkTrivialParams(cp, MasterEntity.class, "entityProp.collection", "2 * integerProp", null, null, ANY, null, dtm().getEnhancer());
	assertCalculatedProperty(cp, ATTRIBUTED_COLLECTIONAL_EXPRESSION, null, "entityProp.collection", null, EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertMetaState3(cp);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// 2. 4. originationProperty /////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_OriginationProperty_mutation_makes_it_invalid_for_null_origProp_and_AGGREGATED_EXPRESSION() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("MAX(2 * integerProp)");
	cp.setOriginationProperty("integerProp");

	cp.setOriginationProperty(null);

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "MAX(2 * integerProp)", null, null, NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", true, true, false);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_makes_it_invalid_for_empty_origProp_and_AGGREGATED_EXPRESSION() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("MAX(2 * integerProp)");
	cp.setOriginationProperty("integerProp");

	cp.setOriginationProperty("");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "MAX(2 * integerProp)", null, null, NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, AGGREGATED_EXPRESSION, null, "", null, SlaveEntity.class, MasterEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", true, true, false);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_makes_it_invalid_for_non_existent_origProp() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");
	cp.setOriginationProperty("integerProp");

	cp.setOriginationProperty("non_existent_prop");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, false);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_forms_valid_state_for_Origination_present_in_contextualExpression() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");
	cp.setOriginationProperty("integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_forms_valid_state_for_Origination_present_in_contextualExpression_with_context_ousiders() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp * ←.integerProp");
	cp.setOriginationProperty("←.integerProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp * ←.integerProp", null, null, NO_ATTR, "←.integerProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_forms_valid_state_with_warning_for_Origination_NOT_present_in_contextualExpression() {
	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("2 * integerProp");
	cp.setOriginationProperty("bigDecimalProp");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "2 * integerProp", null, null, NO_ATTR, "bigDecimalProp", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", true, cp.getProperty("originationProperty").hasWarnings());
    }

    @Test
    public void test_that_OriginationProperty_mutation_forms_valid_state_for_calculated_Origination_expression_present_in_contextualExpression() {
	final CalculatedProperty orEx = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	orEx.setTitle("single").setContextualExpression("2 * integerProp");
	dtm().getEnhancer().addCalculatedProperty(orEx);
	dtm().getEnhancer().apply();

	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("(2 * integerProp + 13) * 73");
	cp.setOriginationProperty("single");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "(2 * integerProp + 13) * 73", null, null, NO_ATTR, "single", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", false, cp.getProperty("originationProperty").hasWarnings());

	cp.setTitle("Expr");
	dtm().getEnhancer().addCalculatedProperty(cp);
	try {
	    dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "entityProp.single");
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_OriginationProperty_mutation_forms_valid_state_with_warning_for_calculated_Origination_expression_NOT_present_in_contextualExpression() {
	final CalculatedProperty orEx = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	orEx.setTitle("single").setContextualExpression("2 * integerProp");
	dtm().getEnhancer().addCalculatedProperty(orEx);
	dtm().getEnhancer().apply();

	final CalculatedProperty cp = createEmpty(factory(), MasterEntity.class, "entityProp", dtm().getEnhancer());
	cp.setContextualExpression("(3 * integerProp + 13) * 73");
	cp.setOriginationProperty("single");

	checkTrivialParams(cp, MasterEntity.class, "entityProp", "(3 * integerProp + 13) * 73", null, null, NO_ATTR, "single", dtm().getEnhancer());
	assertCalculatedProperty(cp, EXPRESSION, null, "entityProp", null, SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertMetaState(cp, "root", true, false, true);
	assertMetaState(cp, "contextPath", false, false, true);
	assertMetaState(cp, "contextualExpression", true, true, true);
	assertMetaState(cp, "title", true, true, true);
	assertMetaState(cp, "attribute", false, false, true);
	assertMetaState(cp, "originationProperty", false, true, true);
	assertEquals("Incorrect hasWarnings for property [originationProperty].", true, cp.getProperty("originationProperty").hasWarnings());

	cp.setTitle("Expr");
	dtm().getEnhancer().addCalculatedProperty(cp);
	try {
	    dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "entityProp.single");
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}
    }




















//    protected void incorrectCalculatedPropertyCreationWithRoot(final Class<?> root) {
//	final CalculatedProperty cp = CalculatedProperty.createEmpty(factory(), root, "", dtm().getEnhancer());
//	checkTrivialParams(cp, Class.class, CalculatedProperty.BULLSHIT, null, null, null, NO_ATTR, "", dtm().getEnhancer());
//
//	final String message = "The creation of calc prop with root [" + root + "] should be failed.";
//	assertNotNull(message, cp.isValid());
//	assertNotNull(message, cp.getProperty("root").getFirstFailure());
//	assertFalse(message, cp.getProperty("root").getFirstFailure().isSuccessful());
//	assertTrue(message, cp.getProperty("root").getFirstFailure() instanceof IncorrectCalcPropertyException);
//    }
//
//    protected void incorrectCalculatedPropertyCreationWithContextPath(final String contextPath) {
//	final CalculatedProperty cp = CalculatedProperty.createEmpty(factory(), MasterEntity.class, contextPath, dtm().getEnhancer());
//	checkTrivialParams(cp, MasterEntity.class, CalculatedProperty.BULLSHIT, null, null, null, NO_ATTR, "", dtm().getEnhancer());
//
//	final String message = "The creation of calc prop with contextPath [" + contextPath + "] should be failed.";
//	assertNotNull(message, cp.isValid());
//	assertNotNull(message, cp.getProperty("contextPath").getFirstFailure());
//	assertFalse(message, cp.getProperty("contextPath").getFirstFailure().isSuccessful());
//	assertTrue(message, cp.getProperty("contextPath").getFirstFailure() instanceof IncorrectCalcPropertyException);
//    }
//
//    protected CalculatedProperty correctCalculatedPropertyCreation(final Class<?> root, final String contextPath) {
//	final CalculatedProperty calc = CalculatedProperty.createEmpty(factory(), root, contextPath, dtm().getEnhancer());
//	checkTrivialParams(calc, root, contextPath, null, null, null, CalculatedPropertyAttribute.NO_ATTR, "", dtm().getEnhancer());
//	return calc;
//    }

    @Test
    public void test_inferred_category_context_and_place_for_Outsider_Context_expressions() {
	// EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp + MAX(integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "MAX(integerProp) + 2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp + 3 * ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp + 3 * ←.integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "MAX(←.integerProp) + 2 * integerProp + 3 * ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + ←.←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + MAX(←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + ←.integerProp + ←.←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + ←.integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + ←.←.integerProp + MAX(←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp + MAX(←.←.integerProp) + ←.←.integerProp + MAX(←.integerProp) + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "MAX(←.←.integerProp) + 2 * integerProp + ←.←.integerProp + MAX(←.integerProp) + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);

	// AGGREGATED_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp + MAX(integerProp))", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(MAX(integerProp) + 2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp + MAX(←.integerProp) + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(MAX(←.integerProp) + 2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + MAX(←.integerProp) + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + ←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + MAX(←.←.integerProp) + ←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + ←.integerProp + ←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp + MAX(←.←.integerProp) + ←.←.integerProp + MAX(←.integerProp) + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(MAX(←.integerProp) + 2 * integerProp + MAX(←.←.integerProp) + ←.←.integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);

	// COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + ←.integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// reversed
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "←.integerProp + 2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + MAX(←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.integerProp + MAX(←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.←.integerProp + MAX(←.←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.integerProp + ←.←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// reversed
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "←.←.integerProp + 2 * integerProp + ←.integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);

	// TODO 					// AGGREGATED_COLLECTIONAL_EXPRESSION
	// TODO 					assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO 					assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO 					// TODO mixing of totals is not yet supported
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp + MAX(←.integerProp))", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp + ←.integerProp + MAX(←.integerProp))", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO 					// reversed
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(←.integerProp + 2 * integerProp + MAX(←.integerProp))", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	//
	// TODO 					assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// TODO 					assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// TODO 					// TODO mixing of totals is not yet supported
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp + MAX(←.integerProp))", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp + MAX(←.integerProp) + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// TODO 					// reversed
	// TODO 					//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(MAX(←.integerProp) + 2 * integerProp + ←.integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);

	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + ←.integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp + ←.integerProp + MAX(←.integerProp)", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// reversed
	// assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "←.integerProp + 2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);

	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	// assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + MAX(←.integerProp)", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + ←.integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO mixing of totals is not yet supported
	//assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp + MAX(←.integerProp) + ←.integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// reversed
	// assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "MAX(←.integerProp) + 2 * integerProp + ←.integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
    }







//    protected void assertCalculatedPropertyName(final ICalculatedProperty calc, final String name) {
//	assertEquals("The name is incorrect.", name, calc.name());
//    }
//
//    protected CalculatedProperty correctCalculatedPropertyCreationWithName(final String title) {
//	return correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp");
//    }
//
//    protected void incorrectCalculatedPropertyCreationWithName(final String title) {
//	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
//	checkTrivialParams(cp, MasterEntity.class, "", "2 * integerProp", null, "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
//
//	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.isValid());
//	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure());
//	assertFalse("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure().isSuccessful());
//	if (!StringUtils.isEmpty(title)) {
//	    assertTrue("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyException);
//	}
//    }
//
//    protected void incorrectCalculatedPropertyCreationWithAttribute(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
//	final CalculatedProperty cp = CalculatedProperty.create(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
//	checkTrivialParams(cp, root, contextPath, contextualExpression, title, desc, CalculatedPropertyAttribute.NO_ATTR, originationProperty, dtm().getEnhancer());
//
//	final String message = "The creation of calc prop with attribute [" + attribute + "] should be failed.";
//	assertNotNull(message, cp.isValid());
//	assertNotNull(message, cp.getProperty("attribute").getFirstFailure());
//	assertFalse(message, cp.getProperty("attribute").getFirstFailure().isSuccessful());
//	assertTrue(message, cp.getProperty("attribute").getFirstFailure() instanceof IncorrectCalcPropertyException);
//    }
//
//    protected CalculatedProperty assertCalculatedPropertyOrigination(final CalculatedProperty calc, final String originationProperty) {
//	assertEquals("The originationProperty is incorrect.", originationProperty, calc.getOriginationProperty());
//	return calc;
//    }
//
//    private static int i = 0;
//
//    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
//	return correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", contextualExpression, "Calculated property" + (++i), "desc", NO_ATTR, originationProperty);
//    }
//
//    protected void incorrectCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
//	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, originationProperty, dtm().getEnhancer());
//	checkTrivialParams(cp, MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, "", dtm().getEnhancer());
//
//	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be failed.";
//	assertNotNull(message, cp.isValid());
//	assertNotNull(message, cp.getProperty("originationProperty").getFirstFailure());
//	assertFalse(message, cp.getProperty("originationProperty").getFirstFailure().isSuccessful());
//	if (!StringUtils.isEmpty(originationProperty)) {
//	    assertTrue(message, cp.getProperty("originationProperty").getFirstFailure() instanceof IncorrectCalcPropertyException);
//	}
//    }
//
//    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationPropertyWithWarning(final String originationProperty, final String contextualExpression) {
//	final CalculatedProperty cp = assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);
//
//	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be warned.";
//	assertNotNull(message, cp.isValid());
//	assertTrue(message, cp.isValid().isSuccessful());
//	assertNull(message, cp.getProperty("originationProperty").getFirstFailure());
//	assertNotNull(message, cp.getProperty("originationProperty").getFirstWarning());
//	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning().isSuccessful());
//	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning() instanceof CalcPropertyWarning);
//	return cp;
//    }
//
//    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationPropertyWithoutWarning(final String originationProperty, final String contextualExpression) {
//	final CalculatedProperty cp = assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);
//
//	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be without warning.";
//	assertNotNull(message, cp.isValid());
//	assertTrue(message, cp.isValid().isSuccessful());
//	assertNull(message, cp.getProperty("originationProperty").getFirstFailure());
//	assertNull(message, cp.getProperty("originationProperty").getFirstWarning());
//	return cp;
//    }


}