package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ALL;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ANY;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyKeyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
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

    protected ICalculatedProperty checkTrivialParams(final ICalculatedProperty calc, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	assertEquals("The root is incorrect.", root, calc.root());
	assertEquals("The contextPath is incorrect.", contextPath, calc.contextPath());
	assertEquals("The contextualExpression is incorrect.", contextualExpression, calc.contextualExpression());
	assertEquals("The title is incorrect.", title, calc.title());
	assertEquals("The desc is incorrect.", desc, calc.desc());
	assertEquals("The attribute is incorrect.", attribute, calc.attribute());
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.originationProperty());
	return calc;
    }

    protected void assertCalculatedProperty(final ICalculatedProperty calc, final CalculatedPropertyCategory category, final String name, final String path, final String pathAndName, final Class<?> contextType, final Class<?> parentType, final Class<?> resultType) {
	assertEquals("The category is incorrect.", category, calc.category());
	assertEquals("The name is incorrect.", name, calc.name());
	assertEquals("The path is incorrect.", path, calc.path());
	assertEquals("The pathAndName is incorrect.", pathAndName, calc.pathAndName());
	assertEquals("The contextType is incorrect.", contextType, calc.contextType());
	assertEquals("The parentType is incorrect.", parentType, calc.parentType());
	assertEquals("The resultType is incorrect.", resultType, calc.resultType());
    }

    protected ICalculatedProperty correctCalculatedPropertyCreation(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	final ICalculatedProperty calc = new CalculatedProperty(root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	checkTrivialParams(calc, root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	return calc;
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

    protected void assertCalculatedPropertyName(final ICalculatedProperty calc, final String name) {
	assertEquals("The name is incorrect.", name, calc.name());
    }

    protected ICalculatedProperty correctCalculatedPropertyCreationWithName(final String title) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", title, "desc", NO_ATTR, "integerProp");
    }

    protected void incorrectCalculatedPropertyCreationWithName(final String title) {
	try {
	    correctCalculatedPropertyCreationWithName(title);
	    fail("The creation of calc prop with title [" + title + "] should be failed.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
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

    protected void incorrectCalculatedPropertyCreation(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	try {
	    correctCalculatedPropertyCreation(root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	    fail("The creation of calc prop with expr [" + contextualExpression + "] and attribute [" + attribute + "] should be failed.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
    }

    @Test
    public void test_incorrect_attributes_application() {
	// the attributes ALL / ANY cannot be applied to the following types of calc props:
	// EXPRESSION
	incorrectCalculatedPropertyCreation(MasterEntity.class, "", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * integerProp", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * integerProp", "Calculated property", "desc", ALL, "integerProp");
	// AGGREGATED_EXPRESSION
	incorrectCalculatedPropertyCreation(MasterEntity.class, "", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp", "2 * MAX(2 * integerProp)", "Calculated prope/rty", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp.entityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	// AGGREGATED_COLLECTIONAL_EXPRESSION
	incorrectCalculatedPropertyCreation(MasterEntity.class, "collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ANY, "integerProp");
	incorrectCalculatedPropertyCreation(MasterEntity.class, "entityProp.collection.slaveEntityProp", "2 * MAX(2 * integerProp)", "Calculated property", "desc", ALL, "integerProp");
    }

    protected void assertCalculatedPropertyOrigination(final ICalculatedProperty calc, final String originationProperty) {
	assertEquals("The originationProperty is incorrect.", originationProperty, calc.originationProperty());
    }

    protected ICalculatedProperty correctCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	return correctCalculatedPropertyCreation(MasterEntity.class, "entityProp", contextualExpression, "Calculated property", "desc", NO_ATTR, originationProperty);
    }

    protected void incorrectCalculatedPropertyCreationWithOriginationProperty(final String originationProperty, final String contextualExpression) {
	try {
	    correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression);
	    fail("The creation of calc prop with originationProperty [" + originationProperty + "] should be failed.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
    }

    protected void correctCalculatedPropertyCreationWithOriginationPropertyWithWarning(final String originationProperty, final String contextualExpression) {
	try {
	    assertCalculatedPropertyOrigination(correctCalculatedPropertyCreationWithOriginationProperty(originationProperty, contextualExpression), originationProperty);
	    fail("The creation of calc prop with originationProperty [" + originationProperty + "] should be warned.");
	} catch (final CalcPropertyKeyWarning e) {
	}
    }

    @Test
    public void test_origination_property_application() {
	incorrectCalculatedPropertyCreationWithOriginationProperty(null, "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty("", "2 * integerProp");
	incorrectCalculatedPropertyCreationWithOriginationProperty("nonExistentProp", "2 * integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("integerProp", "2 * integerProp");
	correctCalculatedPropertyCreationWithOriginationProperty("entityProp.integerProp", "2 * integerProp * entityProp.integerProp");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("integerProp", "2 * moneyProp");
	correctCalculatedPropertyCreationWithOriginationPropertyWithWarning("entityProp.integerProp", "2 * integerProp * entityProp.moneyProp");
    }
}
