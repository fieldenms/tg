package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyKeyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

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
    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractDomainTreeTest();
	rootTypes.add(MasterEntityForIncludedPropertiesLogic.class);
	rootTypes.add(MasterEntityWithUnionForIncludedPropertiesLogic.class);
	return rootTypes;
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
	final IDomainTreeManagerAndEnhancer dtm = new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractDomainTreeManagerTest());
	manageTestingDTM_for_CalculatedPropertyTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

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

    protected CalculatedProperty correctCalculatedPropertyCreation(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final CalculatedProperty calc = CalculatedProperty.createAndValidate(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	checkTrivialParams(calc, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	return calc;
    }

    @Test
    public void test_copying_of_empty_calculated_property() {
	final CalculatedProperty calc = CalculatedProperty.createEmpty(factory(), MasterEntity.class, "", dtm().getEnhancer());
	final Result valid = calc.isValid();
	if (valid.isSuccessful()) {
	    fail("Should be not successful.");
	}
	checkTrivialParams(calc, MasterEntity.class, "", null, null, null, NO_ATTR, "", dtm().getEnhancer());
	assertCalculatedProperty(calc, null, null, null, null, MasterEntity.class, null, null);

	final CalculatedProperty copy = calc.copy(getSerialiser());
	final Result copyValid = copy.isValid();
	if (copyValid.isSuccessful()) {
	    fail("Should be not successful.");
	}

	checkTrivialParams(copy, MasterEntity.class, "", null, null, null, NO_ATTR, "", dtm().getEnhancer());
	assertCalculatedProperty(copy, null, null, null, null, MasterEntity.class, null, null);

	assertMetaInfoIdentical(calc, copy);
    }

    @Test
    public void test_copying_of_correct_full_calculated_property() {
	final CalculatedProperty calc = CalculatedProperty.createAndValidate(factory(), MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	final Result valid = calc.isValid();
	if (!valid.isSuccessful()) {
	    fail("Should be successful.");
	}
	checkTrivialParams(calc, MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(calc, EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);

	dtm().getEnhancer().addCalculatedProperty(calc);
	dtm().getEnhancer().apply();
	final CalculatedProperty copy = (CalculatedProperty) dtm().getEnhancer().copyCalculatedProperty(MasterEntity.class, "calculatedProperty");
	final Result copyValid = copy.isValid();
	if (copyValid.isSuccessful()) {
	    fail("Should be not successful.");
	}
	assertNotNull("Should be not null.", copy.getProperty("title").getFirstFailure());

	checkTrivialParams(copy, MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	assertCalculatedProperty(copy, EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);

	assertMetaInfoIdentical(calc, copy, "title");
    }

    private void assertMetaInfoIdentical(final CalculatedProperty calc1, final CalculatedProperty calc2, final String ... except) {
	final List<String> excepts = Arrays.asList(except);
	for (final MetaProperty property1 : calc1.getProperties().values()) {
	    if (excepts.contains(property1.getName())) {
		continue;
	    }
	    final MetaProperty property2 = calc2.getProperty(property1.getName());

	    if (property1.getFirstFailure() != null) {
		assertNotNull("Should be not null.", property2.getFirstFailure());
		assertEquals("Validation results should be equal.", property1.getFirstFailure().getMessage(), property2.getFirstFailure().getMessage());
		assertEquals("Validation results should be equal.", property1.getFirstFailure().getEx().getMessage(), property2.getFirstFailure().getEx().getMessage());
	    } else {
		if (property2.getFirstFailure() != null) {
		    System.err.println("Property [" + property1.getName() + "] validation result should be null. But = [" + property2.getFirstFailure() + "].");
		}
		assertNull("Property [" + property1.getName() + "] validation result should be null.", property2.getFirstFailure());
	    }
	    if (property1.getFirstWarning() != null) {
		assertNotNull("Should be not null.", property2.getFirstWarning());
		assertEquals("Warning results should be equal.", property1.getFirstWarning().getMessage(), property2.getFirstWarning().getMessage());
	    } else {
		if (property2.getFirstWarning() != null) {
		    System.err.println("Property [" + property1.getName() + "] warning result should be null. But = [" + property2.getFirstWarning() + "].");
		}
		assertNull("Property [" + property1.getName() + "] warning result should be null.", property2.getFirstWarning());
	    }
	    assertEquals("Requiredness should be identical.", property1.isRequired(), property2.isRequired());
	    assertEquals("Editability should be identical.", property1.isEditable(), property2.isEditable());
	}
    }

    protected void incorrectCalculatedPropertyCreationWithRoot(final Class<?> root) {
	final CalculatedProperty cp = CalculatedProperty.createEmpty(factory(), root, "", dtm().getEnhancer());
	checkTrivialParams(cp, Class.class, CalculatedProperty.BULLSHIT, null, null, null, NO_ATTR, "", dtm().getEnhancer());

	final String message = "The creation of calc prop with root [" + root + "] should be failed.";
	assertNotNull(message, cp.isValid());
	assertNotNull(message, cp.getProperty("root").getFirstFailure());
	assertFalse(message, cp.getProperty("root").getFirstFailure().isSuccessful());
	assertTrue(message, cp.getProperty("root").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
    }

    protected void incorrectCalculatedPropertyCreationWithContextPath(final String contextPath) {
	final CalculatedProperty cp = CalculatedProperty.createEmpty(factory(), MasterEntity.class, contextPath, dtm().getEnhancer());
	checkTrivialParams(cp, MasterEntity.class, CalculatedProperty.BULLSHIT, null, null, null, NO_ATTR, "", dtm().getEnhancer());

	final String message = "The creation of calc prop with contextPath [" + contextPath + "] should be failed.";
	assertNotNull(message, cp.isValid());
	assertNotNull(message, cp.getProperty("contextPath").getFirstFailure());
	assertFalse(message, cp.getProperty("contextPath").getFirstFailure().isSuccessful());
	assertTrue(message, cp.getProperty("contextPath").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
    }

    @Test
    public void test_incorrect_creation_with_bad_Root_type() {
	correctCalculatedPropertyCreation(MasterEntity.class, "");

	incorrectCalculatedPropertyCreationWithRoot(null);
	incorrectCalculatedPropertyCreationWithRoot(getClass());
    }

    @Test
    public void test_incorrect_creation_with_bad_Context_Path() {
	incorrectCalculatedPropertyCreationWithContextPath(null);
	incorrectCalculatedPropertyCreationWithContextPath("entityProp.collection.nonExistentPath");

	correctCalculatedPropertyCreation(MasterEntity.class, "");
	correctCalculatedPropertyCreation(MasterEntity.class, "entityProp");
	correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection");
    }

    @Test
    public void test_Title_validation_for_empty_expression_and_revalidation_after_Expression_change() {
	final CalculatedProperty calc = correctCalculatedPropertyCreation(MasterEntity.class, "entityProp");

	calc.setTitle("String prop");

	assertNull("Should be valid.", calc.getProperty("title").getFirstFailure());

	calc.setContextualExpression("integerProp * 2");

	final String message2 = "The title [" + "String prop" + "] should become incorrect in context [" + "entityProp" + "] => parentType [" + calc.parentType().getSimpleName() + "].";
	assertNotNull(message2, calc.isValid());
	assertNotNull(message2, calc.getProperty("title").getFirstFailure());
	assertFalse(message2, calc.getProperty("title").getFirstFailure().isSuccessful());
	assertTrue(message2, calc.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
    }

    protected CalculatedProperty correctCalculatedPropertyCreation(final Class<?> root, final String contextPath) {
	final CalculatedProperty calc = CalculatedProperty.createEmpty(factory(), root, contextPath, dtm().getEnhancer());
	checkTrivialParams(calc, root, contextPath, null, null, null, CalculatedPropertyAttribute.NO_ATTR, "", dtm().getEnhancer());
	return calc;
    }

    @Test
    public void test_that_creation_of_calc_properties_with_undefined_level_will_add_them_into_provided_context() {
	// properties like "2 * 3 -17"
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * 3 -17", "Calc", "Desc", CalculatedPropertyAttribute.NO_ATTR, ""), EXPRESSION, "calc", "", "calc", MasterEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * 3 -17", "Calc", "Desc", CalculatedPropertyAttribute.NO_ATTR, ""), EXPRESSION, "calc", "entityProp", "entityProp.calc", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * 3 -17", "Calc", "Desc", CalculatedPropertyAttribute.NO_ATTR, ""), COLLECTIONAL_EXPRESSION, "calc", "entityProp.collection", "entityProp.collection.calc", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
    }

    @Test
    public void test_inferred_category_context_and_place() {
	// EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "entityProp.entityProp", "entityProp.entityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// AGGREGATED_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", EvenSlaverEntity.class, MasterEntity.class, Integer.class);
	// COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection.slaveEntityProp", "entityProp.collection.slaveEntityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	// TODO AGGREGATED_COLLECTIONAL_EXPRESSION
	// TODO 				assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO 				assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// TODO 				assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection.slaveEntityProp", "entityProp.collection.slaveEntityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
    }

    @Test @Ignore
    public void test_inferred_category_context_and_place_for_AGGREGATED_COLLECTIONAL_EXPRESSIONs() {
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
    }

    @Test
    public void test_originationProperty_requiredness_at_the_beginning() {
    	CalculatedProperty cp = correctCalculatedPropertyCreation(MasterEntity.class, "");
	assertFalse("Should be not required.", cp.getProperty("originationProperty").isRequired());

	cp = correctCalculatedPropertyCreation(MasterEntity.class, "collection");
	assertFalse("Should be not required.", cp.getProperty("originationProperty").isRequired());

	cp = correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection");
	assertFalse("Should be not required.", cp.getProperty("originationProperty").isRequired());

	cp = correctCalculatedPropertyCreation(MasterEntity.class, "collection.entityProp");
	assertFalse("Should be not required.", cp.getProperty("originationProperty").isRequired());

	// AGGREGATED_EXPRESSION
	cp = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertTrue("Should be required.", cp.getProperty("originationProperty").isRequired());
    }

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

    @Test
    public void test_Attribute_property_enablement_for_different_categories() {
	// Empty calc prop
	CalculatedProperty cp1 = correctCalculatedPropertyCreation(MasterEntity.class, "");
	assertFalse("Should be disabled (non-editable).", cp1.getProperty("attribute").isEditable());
	cp1 = correctCalculatedPropertyCreation(MasterEntity.class, "entityProp");
	assertFalse("Should be disabled (non-editable).", cp1.getProperty("attribute").isEditable());
	cp1 = correctCalculatedPropertyCreation(MasterEntity.class, "collection");
	assertFalse("Should be disabled (non-editable).", cp1.getProperty("attribute").isEditable());

	// EXPRESSION
	cp1 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertFalse("Should be disabled (non-editable).", cp1.getProperty("attribute").isEditable());
	// AGGREGATED_EXPRESSION
	final CalculatedProperty cp2 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertFalse("Should be disabled (non-editable).", cp2.getProperty("attribute").isEditable());
	// COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp3 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertTrue("Should be enabled (editable).", cp3.getProperty("attribute").isEditable());
	// TODO						// AGGREGATED_COLLECTIONAL_EXPRESSION
	// TODO						final CalculatedProperty cp4 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	// TODO						assertFalse("Should be disabled (non-editable).", cp4.getProperty("attribute").isEditable());
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp5 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertTrue("Should be enabled (editable).", cp5.getProperty("attribute").isEditable());
    }

    @Test @Ignore
    public void test_that_Title_is_revalidated_after_parentType_has_been_changed_for_collectional_property_mutated_to_Aggregated_collectional() {
	final CalculatedProperty cp = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Placeholder prop", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "placeholderProp", "collection", "collection.placeholderProp", SlaveEntity.class, SlaveEntity.class, Integer.class);

	final String message = "The title [" + "Placeholder prop" + "] should be correct in context [" + "collection" + "] => parentType [" + cp.parentType().getSimpleName() + "].";
	assertNotNull(message, cp.isValid());
	assertNull(message, cp.getProperty("title").getFirstFailure());
	// assertFalse(message, cp.getProperty("title").getFirstFailure().isSuccessful());
	// assertTrue(message, cp.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);

	cp.setContextualExpression("MAX(" + cp.getContextualExpression() + ")");

	final String message2 = "The title [" + "Placeholder prop" + "] should become incorrect in context [" + "" + "] => parentType [" + cp.parentType().getSimpleName() + "].";
	assertNotNull(message2, cp.isValid());
	assertNotNull(message2, cp.getProperty("title").getFirstFailure());
	assertFalse(message2, cp.getProperty("title").getFirstFailure().isSuccessful());
	assertTrue(message2, cp.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
    }

    @Test
    public void test_that_Title_is_revalidated_after_parentType_has_been_changed_2() {
	CalculatedProperty cp = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp", "Double integer prop", "desc", NO_ATTR, "integerProp"), EXPRESSION, "doubleIntegerProp", "entityProp", "entityProp.doubleIntegerProp", SlaveEntity.class, SlaveEntity.class, Integer.class);

	final String message = "Should be correct.";
	assertNotNull(message, cp.isValid());
	assertTrue(message, cp.isValid().isSuccessful());

	dtm().getEnhancer().addCalculatedProperty(cp);
	dtm().getEnhancer().apply();

	cp = (CalculatedProperty) dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "entityProp.doubleIntegerProp");
	cp.setContextualExpression("MAX(" + cp.getContextualExpression() + ")");

	assertNotNull(message, cp.isValid());
	assertTrue(message, cp.isValid().isSuccessful());
	assertNull(message, cp.getProperty("title").getFirstFailure());
    }

    @Test
    public void test_that_OriginationProperty_is_revalidated_after_ContextualExpression_has_been_changed() {
	final CalculatedProperty cp = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "SUM(bigDecimalProp)", "Aggregated", "desc", NO_ATTR, "bigDecimalProp"), AGGREGATED_EXPRESSION, "aggregated", "", "aggregated", MasterEntity.class, MasterEntity.class, BigDecimal.class);

	assertNotNull("Should be valid", cp.isValid());
	assertTrue("Should be valid", cp.isValid().isSuccessful());


	cp.setContextualExpression("SUM(integerProp)");

	final String message2 = "The originationProperty should remain correct but become warned due to contextualExpression changes [" + cp.getContextualExpression() + "].";
	assertNotNull(message2, cp.isValid());
	assertTrue(message2, cp.isValid().isSuccessful());
	assertNull(message2, cp.getProperty("originationProperty").getFirstFailure());
	assertNotNull(message2, cp.getProperty("originationProperty").getFirstWarning());
	assertTrue(message2, cp.getProperty("originationProperty").getFirstWarning().isWarning());
	assertTrue(message2, cp.getProperty("originationProperty").getFirstWarning() instanceof CalcPropertyKeyWarning);
    }

    protected void assertCalculatedPropertyName(final ICalculatedProperty calc, final String name) {
	assertEquals("The name is incorrect.", name, calc.name());
    }

    protected CalculatedProperty correctCalculatedPropertyCreationWithName(final String title) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp");
    }

    protected void incorrectCalculatedPropertyCreationWithName(final String title) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	checkTrivialParams(cp, MasterEntity.class, "", "2 * integerProp", null, "desc", NO_ATTR, "integerProp", dtm().getEnhancer());

	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.isValid());
	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure());
	assertFalse("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure().isSuccessful());
	if (!StringUtils.isEmpty(title)) {
	    assertTrue("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
	}
    }

    protected void incorrectCalculatedPropertyCreationWithAttribute(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	checkTrivialParams(cp, root, contextPath, contextualExpression, title, desc, CalculatedPropertyAttribute.NO_ATTR, originationProperty, dtm().getEnhancer());

	final String message = "The creation of calc prop with attribute [" + attribute + "] should be failed.";
	assertNotNull(message, cp.isValid());
	assertNotNull(message, cp.getProperty("attribute").getFirstFailure());
	assertFalse(message, cp.getProperty("attribute").getFirstFailure().isSuccessful());
	assertTrue(message, cp.getProperty("attribute").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
    }

    @Test
    public void test_inferred_name() {
	incorrectCalculatedPropertyCreationWithName(null);
	incorrectCalculatedPropertyCreationWithName("");
	incorrectCalculatedPropertyCreationWithName("-  % ^% &^ %&78434 213 321123-% ^% &^ %&% ");
	assertCalculatedPropertyName(correctCalculatedPropertyCreationWithName("Calculated property"), "calculatedProperty");
	assertCalculatedPropertyName(correctCalculatedPropertyCreationWithName("CalCulaTed pRopertY"), "calCulaTedPRopertY");
	assertCalculatedPropertyName(correctCalculatedPropertyCreationWithName("-  % ^% &^ %&Calculated property-% ^% &^ %&% "), "calculatedProperty");
	assertCalculatedPropertyName(correctCalculatedPropertyCreationWithName("-  % ^% &^ %&   87 87Calc$^&^$%&^ulated pro&^%^&perty 56 bum 71 -% ^% &^ %&% "), "calculatedProperty56Bum71");
    }

    @Test
    public void test_incorrect_attributes_application() {
	// the attributes ALL / ANY cannot be applied to the following types of calc props:
	// EXPRESSION
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.entityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp");
	// AGGREGATED_EXPRESSION
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp)", "Calculated prope/rty", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	// TODO 					// AGGREGATED_COLLECTIONAL_EXPRESSION
	// TODO 					incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
	// TODO 					incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	// TODO 					incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
    }

    @Test @Ignore
    public void test_attributes_resetting_after_property_became_NON_ATTRIBUTED() {
	// the attributes ALL / ANY can be applied only to the following type of calc props:
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp1 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	cp1.setContextualExpression("MAX(" + cp1.getContextualExpression() + ")");
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp1.getAttribute());
	final CalculatedProperty cp2 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection", "entityProp.collection.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	cp2.setAttribute(CalculatedPropertyAttribute.NO_ATTR);
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp2.getAttribute());
	final CalculatedProperty cp3 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection.slaveEntityProp", "entityProp.collection.slaveEntityProp.calculatedProperty", EvenSlaverEntity.class, EvenSlaverEntity.class, Integer.class);
	cp3.setContextualExpression("MAX(" + cp3.getContextualExpression() + ")");
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp3.getAttribute());
    }

    protected CalculatedProperty assertCalculatedPropertyOrigination(final CalculatedProperty calc, final String originationProperty) {
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.getOriginationProperty());
	return calc;
    }

    private static int i = 0;

    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", contextualExpression, "Calculated property" + (++i), "desc", NO_ATTR, originationProperty);
    }

    protected void incorrectCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, originationProperty, dtm().getEnhancer());
	checkTrivialParams(cp, MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, "", dtm().getEnhancer());

	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be failed.";
	assertNotNull(message, cp.isValid());
	assertNotNull(message, cp.getProperty("originationProperty").getFirstFailure());
	assertFalse(message, cp.getProperty("originationProperty").getFirstFailure().isSuccessful());
	if (!StringUtils.isEmpty(originationProperty)) {
	    assertTrue(message, cp.getProperty("originationProperty").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
	}
    }

    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationPropertyWithWarning(final String originationProperty, final String contextualExpression) {
	final CalculatedProperty cp = assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);

	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be warned.";
	assertNotNull(message, cp.isValid());
	assertTrue(message, cp.isValid().isSuccessful());
	assertNull(message, cp.getProperty("originationProperty").getFirstFailure());
	assertNotNull(message, cp.getProperty("originationProperty").getFirstWarning());
	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning().isSuccessful());
	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning() instanceof CalcPropertyKeyWarning);
	return cp;
    }

    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationPropertyWithoutWarning(final String originationProperty, final String contextualExpression) {
	final CalculatedProperty cp = assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);

	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be without warning.";
	assertNotNull(message, cp.isValid());
	assertTrue(message, cp.isValid().isSuccessful());
	assertNull(message, cp.getProperty("originationProperty").getFirstFailure());
	assertNull(message, cp.getProperty("originationProperty").getFirstWarning());
	return cp;
    }

    @Test
    public void test_origination_property_application() {
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "MAX(2 * integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("", "MAX(2 * integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "MAX(2 * integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("integerProp", "MAX(2 * integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("entityProp.integerProp", "MAX(2 * integerProp * entityProp.integerProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("integerProp", "MAX(2 * moneyProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("entityProp.integerProp", "MAX(2 * integerProp * entityProp.moneyProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp * entityProp.moneyProp");
	correctCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp * entityProp.moneyProp");
    }

    @Test
    public void test_calculated_origination_property_application() {
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "1 * integerProp", "Single", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "2 * integerProp", "Double", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();

	final CalculatedProperty cp1 = correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("single", "MAX(2 * integerProp)");
	dtm().getEnhancer().addCalculatedProperty(cp1);
	dtm().getEnhancer().apply();
	final CalculatedProperty cp2 = correctCalculatedPropertyCreationWithOriginationPropertyWithoutWarning("double", "MAX(2 * integerProp)");
	dtm().getEnhancer().addCalculatedProperty(cp2);
	dtm().getEnhancer().apply();

	try {
	    dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "entityProp.single");
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "entityProp.double");
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_origination_property_application_with_context_outsiders() {
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp - ←.integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp - ←.integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "MAX(2 * integerProp - ←.integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("", "MAX(2 * integerProp - ←.integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "2 * integerProp - ←.integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "MAX(2 * integerProp - ←.integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("←.integerProp", "MAX(2 * integerProp - ←.integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("entityProp.integerProp", "MAX(2 * integerProp * entityProp.integerProp - ←.integerProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("←.moneyProp", "MAX(2 * moneyProp - ←.integerProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("←.moneyProp", "MAX(2 * integerProp * entityProp.moneyProp - ←.integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp * entityProp.moneyProp - ←.integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp * entityProp.moneyProp - ←.integerProp");
    }
}