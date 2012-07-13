package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic;

/**
 * A test for {@link AbstractDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class AbstractDomainTreeManagerAndEnhancerTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IDomainTreeManagerAndEnhancer dtm() {
	return (IDomainTreeManagerAndEnhancer) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(AbstractDomainTreeManagerAndEnhancerTest.class);
    }

    protected static Object createDtm_for_AbstractDomainTreeManagerAndEnhancerTest() {
	return new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractDomainTreeManagerAndEnhancerTest());
    }

    protected static Object createIrrelevantDtm_for_AbstractDomainTreeManagerAndEnhancerTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeManagerAndEnhancerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeTest());
	rootTypes.add(MasterEntityForIncludedPropertiesLogic.class);
	rootTypes.add(MasterEntityWithUnionForIncludedPropertiesLogic.class);
	return rootTypes;
    }

    protected static void manageTestingDTM_for_AbstractDomainTreeManagerAndEnhancerTest(final Object obj) {
	final IDomainTreeManagerAndEnhancer dtmae = (IDomainTreeManagerAndEnhancer) obj;

	manageTestingDTM_for_AbstractDomainTreeTest(dtmae.getRepresentation());

	dtmae.getFirstTick().checkedProperties(MasterEntity.class);
	dtmae.getSecondTick().checkedProperties(MasterEntity.class);
    }

    protected static void performAfterDeserialisationProcess_for_AbstractDomainTreeManagerAndEnhancerTest(final Object obj) {
    }

    protected static void assertInnerCrossReferences_for_AbstractDomainTreeManagerAndEnhancerTest(final Object obj) {
	final AbstractDomainTreeManagerAndEnhancer dtmae = (AbstractDomainTreeManagerAndEnhancer) obj;
	final AbstractDomainTreeManager dtm = (AbstractDomainTreeManager) dtmae.base();
	AbstractDomainTreeManagerTest.assertInnerCrossReferences_for_AbstractDomainTreeManagerTest(dtm);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_domain_changes_are_correctly_reflected_in_CHECKed_properties() {
	assertEquals("Incorrect checked properties.", Collections.emptyList(), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "1 * 2 * integerProp", "Prop1_mutably checked prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("entityProp.prop1_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "1 * 2 * integerProp", "Prop2_mutably checked prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("entityProp.prop1_mutablyCheckedProp", "entityProp.prop2_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getFirstTick().swap(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1_mutablyCheckedProp", "entityProp.prop2_mutablyCheckedProp");
	assertEquals("Incorrect checked properties.", Arrays.asList("entityProp.prop2_mutablyCheckedProp", "entityProp.prop1_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1_mutablyCheckedProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("entityProp.prop2_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop2_mutablyCheckedProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Collections.emptyList(), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    ///////////////////////////////////////////////////////////////////////
    ////////////////////// 3. Calculated properties ///////////////////////
    ///////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_calculated_properties_work() throws Exception {
	/////////////// ADDING & MANAGING ///////////////
	// enhance domain with new calculated property
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "1 * 2 * integerProp", "Calc prop1", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertFalse("The brand new calculated property should be included.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	dtm().getRepresentation().excludeImmutably(MasterEntity.class, "calcProp1");
	assertTrue("The brand new calculated property should become excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));

	// enhance domain with new calculated property
	final String calcProp2 = "calcProp2"; // "entityProp.calcProp2";
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "MAX(1 * 2.5 * moneyProp)", "Calc prop2", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm().getEnhancer().apply();
	assertFalse("The calculated property should 'be' enabled at first.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	dtm().getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, calcProp2);
	assertTrue("The brand new calculated property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));

	// enhance domain with new calculated property
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "1 * 2.5 * moneyProp", "Calc prop3", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm().getEnhancer().apply();
	assertFalse("The brand new calculated property should be immutable unchecked.", dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, "calcProp3"));
	dtm().getSecondTick().check(MasterEntity.class, "calcProp3", true);
	dtm().getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, "calcProp3");
	assertTrue("The brand new calculated property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	assertTrue("The brand new calculated property should be checked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp3"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "calcProp3"));

	// enhance domain with new calculated property
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "1 * 2.5 * moneyProp", "Calc prop4", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm().getEnhancer().apply();

	// enhance domain with new calculated property
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(1 * 2.5 * bigDecimalProp)", "Calc prop5", "Desc", CalculatedPropertyAttribute.NO_ATTR, "bigDecimalProp");
	dtm().getEnhancer().apply();
	assertFalse("The brand new calculated property should be unchecked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp5"));
	dtm().getSecondTick().check(MasterEntity.class, "calcProp5", true);
	assertTrue("The brand new calculated property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	assertTrue("The brand new calculated property should be checked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp3"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "calcProp3"));
	assertTrue("The brand new calculated property should be checked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp5"));

	/////////////// MODIFYING & MANAGING ///////////////
	dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp1").setDesc("new desc");
	dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp1").setContextualExpression("56 * 78 / integerProp");
	dtm().getEnhancer().apply();
	assertTrue("The brand new calculated property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	assertTrue("The brand new calculated property should be checked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp3"));
	assertTrue("The brand new calculated property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "calcProp3"));
	assertTrue("The brand new calculated property should be checked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp5"));

	/////////////// REMOVING & MANAGING ///////////////
	final ICalculatedProperty calc1 = dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp1");
	final ICalculatedProperty calc2 = dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, calcProp2);
	final ICalculatedProperty calc3 = dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp3");
	final ICalculatedProperty calc4 = dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp4");
	final ICalculatedProperty calc5 = dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp5");
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp1");
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, calcProp2);
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp3");
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp4");
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp5");
	dtm().getEnhancer().apply();

	try {
	    dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1");
	    fail("At this moment property 'calcProp1' should not exist and should cause exception.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2);
	    fail("At this moment property 'calcProp2' should not exist and should cause exception.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getRepresentation().getSecondTick().isCheckedImmutably(MasterEntity.class, "calcProp3");
	    fail("At this moment property 'calcProp3' should not exist and should cause exception.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp5");
	    fail("At this moment property 'calcProp5' should not exist and should cause exception.");
	} catch (final IllegalArgumentException e) {
	}

	dtm().getEnhancer().addCalculatedProperty(calc1);
	dtm().getEnhancer().addCalculatedProperty(calc2);
	dtm().getEnhancer().addCalculatedProperty(calc3);
	dtm().getEnhancer().addCalculatedProperty(calc4);
	dtm().getEnhancer().addCalculatedProperty(calc5);
	dtm().getEnhancer().apply();
	assertFalse("The calculated property with the same name should 'become' included.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertFalse("The calculated property with the same name should 'become' enabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	assertFalse("The calculated property with the same name should 'become' unchecked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp3"));
	assertFalse("The calculated property with the same name should 'become' enabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "calcProp3"));
	assertFalse("The calculated property with the same name should 'become' unchecked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp5"));

	///////////////////////////////////////////////////////////////////////////////////////////
	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = serialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = serialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
	assertNotNull("", copy.getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp1"));
	assertNotNull("", copy.getEnhancer().getCalculatedProperty(MasterEntity.class, calcProp2));
	assertNotNull("", copy.getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp3"));
	assertNotNull("", copy.getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp5"));
	assertFalse("The calculated property with the same name should 'become' excluded.", copy.getRepresentation().isExcludedImmutably(MasterEntity.class, "calcProp1"));
	assertFalse("The calculated property with the same name should 'become' disabled.", copy.getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, calcProp2));
	assertFalse("The calculated property with the same name should 'become' unchecked.", dtm().getSecondTick().isChecked(MasterEntity.class, "calcProp3"));
	assertFalse("The calculated property with the same name should 'become' enabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "calcProp3"));
	assertFalse("The calculated property with the same name should 'become' checked.", copy.getSecondTick().isChecked(MasterEntity.class, "calcProp5"));
    }
}
