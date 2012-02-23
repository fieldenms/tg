package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ALL;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ANY;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
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
import ua.com.fielden.platform.utils.EntityUtils;

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
    protected static void manageTestingDTM_for_AbstractDomainTreeManagerTest(final IDomainTreeManager dtm) {
	manageTestingDTM_for_AbstractDomainTreeTest(dtm);

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IDomainTreeManagerAndEnhancer dtm = new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractDomainTreeManagerTest());
	manageTestingDTM_for_AbstractDomainTreeManagerTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    protected ICalculatedProperty checkTrivialParams(final ICalculatedProperty calc, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	assertEquals("The root is incorrect.", root, calc.getRoot());
	assertEquals("The contextPath is incorrect.", contextPath, calc.getContextPath());
	assertEquals("The contextualExpression is incorrect.", contextualExpression, calc.getContextualExpression());
	assertEquals("The title is incorrect.", title, calc.getTitle());
	assertEquals("The desc is incorrect.", desc, calc.getDesc());
	assertEquals("The attribute is incorrect.", attribute, calc.getAttribute());
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.getOriginationProperty());
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
	final CalculatedProperty calc = CalculatedProperty.create(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	checkTrivialParams(calc, root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	return calc;
    }

    @Test
    public void test_serialisation() {
	final CalculatedProperty calc = CalculatedProperty.create(factory(), MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	checkTrivialParams(calc, MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp");
	assertCalculatedProperty(calc, EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	final CalculatedProperty copy = EntityUtils.deepCopy(calc, getSerialiser());
	checkTrivialParams(copy, MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp");
	assertCalculatedProperty(copy, EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
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
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp.collection.slaveEntityProp", "entityProp.collection.slaveEntityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
    }

    @Test
    public void test_Attribute_property_enablement_for_different_categories() {
	// EXPRESSION
	final CalculatedProperty cp1 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertFalse("Should be disabled (non-editable).", cp1.getProperty("attribute").isEditable());
	// AGGREGATED_EXPRESSION
	final CalculatedProperty cp2 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_EXPRESSION, "calculatedProperty", "", "calculatedProperty", MasterEntity.class, MasterEntity.class, Integer.class);
	assertFalse("Should be disabled (non-editable).", cp2.getProperty("attribute").isEditable());
	// COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp3 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", NO_ATTR, "integerProp"), COLLECTIONAL_EXPRESSION, "calculatedProperty", "collection", "collection.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	assertTrue("Should be enabled (editable).", cp3.getProperty("attribute").isEditable());
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp4 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", NO_ATTR, "integerProp"), AGGREGATED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertFalse("Should be disabled (non-editable).", cp4.getProperty("attribute").isEditable());
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp5 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	assertTrue("Should be enabled (editable).", cp5.getProperty("attribute").isEditable());
    }

    @Test
    public void test_that_Title_is_revalidated_after_parentType_has_been_changed() {
	// COLLECTIONAL_EXPRESSION
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

    protected void assertCalculatedPropertyName(final ICalculatedProperty calc, final String name) {
	assertEquals("The name is incorrect.", name, calc.name());
    }

    protected CalculatedProperty correctCalculatedPropertyCreationWithName(final String title) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp");
    }

    protected void incorrectCalculatedPropertyCreationWithName(final String title) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp", dtm().getEnhancer());
	checkTrivialParams(cp, MasterEntity.class, "", "2 * integerProp", null, "desc", NO_ATTR, "integerProp");

	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.isValid());
	assertNotNull("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure());
	assertFalse("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure().isSuccessful());
	if (!StringUtils.isEmpty(title)) {
	    assertTrue("The creation of calc prop with title [" + title + "] should be failed.", cp.getProperty("title").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
	}
    }

    protected void incorrectCalculatedPropertyCreationWithAttribute(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
	checkTrivialParams(cp, root, contextPath, contextualExpression, title, desc, CalculatedPropertyAttribute.NO_ATTR, originationProperty);

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
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreationWithAttribute(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
    }

    @Test
    public void test_attributes_resetting_after_property_became_NON_ATTRIBUTED() {
	// the attributes ALL / ANY can be applied only to the following type of calc props:
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	final CalculatedProperty cp1 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "", "calculatedProperty", SlaveEntity.class, MasterEntity.class, Integer.class);
	cp1.setContextualExpression("MAX(" + cp1.getContextualExpression() + ")");
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp1.getAttribute());
	final CalculatedProperty cp2 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", EvenSlaverEntity.class, SlaveEntity.class, Integer.class);
	cp2.setAttribute(CalculatedPropertyAttribute.NO_ATTR);
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp2.getAttribute());
	final CalculatedProperty cp3 = assertCalculatedProperty(correctCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp"), ATTRIBUTED_COLLECTIONAL_EXPRESSION, "calculatedProperty", "entityProp", "entityProp.calculatedProperty", SlaveEntity.class, SlaveEntity.class, Integer.class);
	cp3.setContextualExpression("MAX(" + cp3.getContextualExpression() + ")");
	assertEquals("The attribute resetting has not been executed.", CalculatedPropertyAttribute.NO_ATTR, cp3.getAttribute());
    }

    protected CalculatedProperty assertCalculatedPropertyOrigination(final CalculatedProperty calc, final String originationProperty) {
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.getOriginationProperty());
	return calc;
    }

    protected CalculatedProperty correctCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, originationProperty);
    }

    protected void incorrectCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	final CalculatedProperty cp = CalculatedProperty.create(factory(), MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, originationProperty, dtm().getEnhancer());
	checkTrivialParams(cp, MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, null);

	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be failed.";
	assertNotNull(message, cp.isValid());
	assertNotNull(message, cp.getProperty("originationProperty").getFirstFailure());
	assertFalse(message, cp.getProperty("originationProperty").getFirstFailure().isSuccessful());
	if (!StringUtils.isEmpty(originationProperty)) {
	    assertTrue(message, cp.getProperty("originationProperty").getFirstFailure() instanceof IncorrectCalcPropertyKeyException);
	}
    }

    protected void correctCalculatedPropertyCreationWithOriginationPropertyWithWarning(final String originationProperty, final String contextualExpression) {
	final CalculatedProperty cp = assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);

	final String message = "The creation of calc prop with originationProperty [" + originationProperty + "] should be warned.";
	assertNotNull(message, cp.isValid());
	assertTrue(message, cp.isValid().isSuccessful());
	assertNull(message, cp.getProperty("originationProperty").getFirstFailure());
	assertNotNull(message, cp.getProperty("originationProperty").getFirstWarning());
	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning().isSuccessful());
	assertTrue(message, cp.getProperty("originationProperty").getFirstWarning() instanceof CalcPropertyKeyWarning);
    }

    @Test
    public void test_origination_property_application() {
	correctCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "MAX(2 * integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("", "MAX(2 * integerProp)");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "MAX(2 * integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("integerProp", "MAX(2 * integerProp)");
	correctCalculatedPropertyCreationWithOriginationProperty("entityProp.integerProp", "MAX(2 * integerProp * entityProp.integerProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("integerProp", "MAX(2 * moneyProp)");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("entityProp.integerProp", "MAX(2 * integerProp * entityProp.moneyProp)");
	correctCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp * entityProp.moneyProp");
    }
}
