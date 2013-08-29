package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation.IPropertyDisablementListener;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * A test for base TG domain tree representation.
 *
 * @author TG Team
 *
 */
public class AbstractDomainTreeRepresentationAndEnhancerTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IDomainTreeManagerAndEnhancer dtm() {
	return (IDomainTreeManagerAndEnhancer) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(AbstractDomainTreeRepresentationAndEnhancerTest.class);
    }

    protected static Object createDtm_for_AbstractDomainTreeRepresentationAndEnhancerTest() {
	return new DomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractDomainTreeRepresentationAndEnhancerTest());
    }

    protected static Object createIrrelevantDtm_for_AbstractDomainTreeRepresentationAndEnhancerTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeRepresentationAndEnhancerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeTest());
	return rootTypes;
    }

    protected static void manageTestingDTM_for_AbstractDomainTreeRepresentationAndEnhancerTest(final Object obj) {
	final IDomainTreeManagerAndEnhancer dtmae = (IDomainTreeManagerAndEnhancer) obj;

	manageTestingDTM_for_AbstractDomainTreeTest(dtmae.getRepresentation());

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtmae.getEnhancer().addCalculatedProperty(MasterEntity.class, name, "1 * integerProp", "Calc integer prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtmae.getEnhancer().apply();
	    }
	}, "");
	dtmae.getFirstTick().checkedProperties(MasterEntity.class);
	dtmae.getSecondTick().checkedProperties(MasterEntity.class);
    }

    protected static void performAfterDeserialisationProcess_for_AbstractDomainTreeRepresentationAndEnhancerTest(final Object obj) {
    }

    protected static void assertInnerCrossReferences_for_AbstractDomainTreeRepresentationAndEnhancerTest(final Object obj) {
	final AbstractDomainTreeManagerAndEnhancer dtmae = (AbstractDomainTreeManagerAndEnhancer) obj;
	final AbstractDomainTreeManager dtm = (AbstractDomainTreeManager) dtmae.base();
	AbstractDomainTreeManagerTest.assertInnerCrossReferences_for_AbstractDomainTreeManagerTest(dtm);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Excluding logic //////////////////////
    ////////////////////////////////////////////////////////////////
    @Test
    public void test_that_domain_changes_for_First_Level_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
    }

    @Test
    public void test_that_domain_changes_for_First_Level_COLLECTIONS_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "entityPropCollection.prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	PropertyTypeDeterminator.determinePropertyType(dtm().getEnhancer().getManagedType(MasterEntityForIncludedPropertiesLogic.class), "entityPropCollection.prop1");

	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection.prop1").setAttribute(CalculatedPropertyAttribute.ALL);
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "entityPropCollection.prop1").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
    }

    @Test
    public void test_that_domain_changes_for_Second_Level_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop1", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "2 * integerProp", "Prop2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop1", "entityProp.prop2", "entityPropCollection", "entityPropCollection.dummy-property").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityProp.prop2", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop2");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_domain_changes_for_Second_Level_circular_properties_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();

	assertTrue("Should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType.entityProp.keyPartProp"));

	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.dummy-property", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.dummy-property", "entityPropOfSelfType.prop1", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property").toString(), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class).toString());
    }

    @Test
    public void test_that_domain_changes_for_property_removal_are_correctly_reflected_in_Included_properties_with_resetting_of_the_meta_state() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getFirstTick().check(MasterEntityForIncludedPropertiesLogic.class, "prop1", true);

	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    public void test_that_domain_changes_for_calc_property_modifications_are_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "", "2 * integerProp", "Prop1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getFirstTick().check(MasterEntityForIncludedPropertiesLogic.class, "prop1", true);
	dtm().getSecondTick().check(MasterEntityForIncludedPropertiesLogic.class, "prop1", true);

	// minor change which should do nothing with meta-state
	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1").setDesc("Changed desc");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	assertTrue("Should be unchanged.", dtm().getFirstTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));
	assertTrue("Should be unchanged.", dtm().getSecondTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));

	// minor change which should do nothing with meta-state
	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1").setOriginationProperty("moneyProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	assertTrue("Should be unchanged.", dtm().getFirstTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));
	assertTrue("Should be unchanged.", dtm().getSecondTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));

	// minor change which should do nothing with meta-state
	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1").setContextualExpression("3 * moneyProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	assertTrue("Should be unchanged.", dtm().getFirstTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));
	assertTrue("Should be unchanged.", dtm().getSecondTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));

	// at this stage -- title change is treated as "significant"
	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1").setTitle("Prop 2");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop2"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	assertFalse("Should be unchanged.", dtm().getFirstTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop2"));
	assertFalse("Should be unchanged.", dtm().getSecondTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop2"));

	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop2").setTitle("Prop 1");
	dtm().getEnhancer().apply();
	dtm().getFirstTick().check(MasterEntityForIncludedPropertiesLogic.class, "prop1", true);
	dtm().getSecondTick().check(MasterEntityForIncludedPropertiesLogic.class, "prop1", true);

	// significant change which should reset+update meta-state
	dtm().getEnhancer().getCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "prop1").setContextualExpression("MAX(2 * integerProp)");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "prop1"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	assertFalse("Should be resetted.", dtm().getFirstTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));
	assertFalse("Should be resetted.", dtm().getSecondTick().isChecked(MasterEntityForIncludedPropertiesLogic.class, "prop1"));
    }

    @Test
    public void test_that_Included_Properties_are_correct_after_serialisation_deserialisation() throws Exception {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), dtm().getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = serialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = serialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property"), copy.getRepresentation().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////// 6. Available functions.//////////////////////
    ////////////////////////////////////////////////////////////////////
    protected List<Function> enhanceFunctionsWithCollectionalAttributes(final List<Function> functions) {
	final List<Function> newFunctions = new ArrayList<Function>(functions);
	newFunctions.add(Function.ALL);
	newFunctions.add(Function.ANY);
	return newFunctions;
    }

    @Test
    public void test_that_integer_calculated_property_has_functions_with_Count_Distinct() {
	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.EXPRESSION based on date property.
	final String m = "Available functions are incorrect.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "YEAR(dateProp)", "Integer Prop Originated From Date", "desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
		dtm().getEnhancer().apply();
		assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "integerPropOriginatedFromDate"))));
	    }
	}, ""); // integerPropOriginatedFromDate

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION based on date property.
	allLevelsWithCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "YEAR(dateProp)", "Integer Prop Originated From Date", "desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
		dtm().getEnhancer().apply();
		assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "integerPropOriginatedFromDate"))));
	    }
	}, ""); // integerPropOriginatedFromDate

	// TODO // Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on entity property.
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(entityProp)", "Calc From Entity Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(entityProp)", "Calc From Entity Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(entityProp)", "Calc From Entity Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	// TODO dtm().getEnhancer().apply();
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromEntityProp1")));
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp2")));
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp3")));
	//
	// TODO // Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on boolean property.
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(booleanProp)", "Calc From Boolean Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(booleanProp)", "Calc From Boolean Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	// TODO dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(booleanProp)", "Calc From Boolean Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	// TODO dtm().getEnhancer().apply();
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromBooleanProp1")));
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp2")));
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp3")));

	/////////////////////////////////////////////////// There are no count distinct //////////////////////////////////////////////
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "2 * integerProp", "Calculated Integer Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtm().getEnhancer().apply();
		assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "calculatedIntegerProp"))));
	    }
	}, ""); // calculatedIntegerProp

	allLevelsWithCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "2 * integerProp", "Calculated Integer Prop", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
		dtm().getEnhancer().apply();
		assertEquals(m, enhanceFunctionsWithCollectionalAttributes(Arrays.asList(Function.SELF,  Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, AbstractDomainTreeTest.name(name, "calculatedIntegerProp"))));
	    }
	}, ""); // calculatedIntegerProp

	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "COUNT(integerProp)", "Calc From Integer Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "COUNT(integerProp)", "Calc From Integer Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.entityProp", "COUNT(integerProp)", "Calc From Integer Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp2")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromIntegerProp3")));
    }

    @Test @Ignore
    public void test_that_integer_calculated_of_AGGREGATED_COLLECTIONAL_EXPRESSION_type_property_has_functions_with_Count_Distinct() {
	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.EXPRESSION based on date property.
	final String m = "Available functions are incorrect.";

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on entity property.
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(entityProp)", "Calc From Entity Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(entityProp)", "Calc From Entity Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(entityProp)", "Calc From Entity Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "entityProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromEntityProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp2")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromEntityProp3")));

	// Check whether count distinct function can be applied to calculated property of CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION type based on boolean property.
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "COUNT(booleanProp)", "Calc From Boolean Prop 1", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "COUNT(booleanProp)", "Calc From Boolean Prop 2", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.slaveEntityProp", "COUNT(booleanProp)", "Calc From Boolean Prop 3", "desc", CalculatedPropertyAttribute.NO_ATTR, "booleanProp");
	dtm().getEnhancer().apply();
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "calcFromBooleanProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp2")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.COUNT_DISTINCT, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.calcFromBooleanProp3")));
    }

    @Test
    public void test_which_calculated_property_categories_have_which_functions() {
	enhanceDomainWithCalculatedPropertiesOfDifferentTypes(dtm());
	final String m = "Available functions are incorrect.";

	// EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "exprProp")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.exprProp")));
	// AGGREGATED_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "aggrExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "aggrExprProp2")));
	// COLLECTIONAL_EXPRESSION
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.collExprProp")));
	assertEquals(m, enhanceFunctionsWithCollectionalAttributes( Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX)), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.simpleEntityProp.collExprProp")));
	// TODO AGGREGATED_COLLECTIONAL_EXPRESSION
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp1")));
	// TODO assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp2")));
	// ATTRIBUTED_COLLECTIONAL_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.attrCollExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.collection.simpleEntityProp.attrCollExprProp2")));
    }

    @Test @Ignore
    public void test_AGGREGATED_COLLECTIONAL_EXPRESSION_calculated_property_category_have_which_functions() {
	enhanceDomainWithCalculatedPropertiesOfDifferentTypes(dtm());
	final String m = "Available functions are incorrect.";

	// AGGREGATED_COLLECTIONAL_EXPRESSION
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp1")));
	assertEquals(m, Arrays.asList(Function.SELF, Function.SUM, Function.AVG, Function.MIN, Function.MAX), new ArrayList<Function>(dtm().getRepresentation().availableFunctions(MasterEntity.class, "entityProp.aggrCollExprProp2")));
    }

    private static int i, j;

    @Test
    public void test_that_PropertyListeners_work() {
	i = 0; j = 0;
	final IPropertyListener listener = new IPropertyListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean wasAddedOrRemoved, final Boolean oldState) {
		if (wasAddedOrRemoved == null) {
		    throw new IllegalArgumentException("'wasAddedOrRemoved' cannot be null.");
		}
		if (wasAddedOrRemoved) {
		    i++;
		} else {
		    j++;
		}
	    }

	    @Override
	    public boolean isInternal() {
	        return false;
	    }
	};
	dtm().getRepresentation().addPropertyListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getRepresentation().excludeImmutably(MasterEntity.class, "integerProp");
	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "1 * 2 * integerProp", "Calc prop1", "Desc", CalculatedPropertyAttribute.NO_ATTR, "bigDecimalProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp1");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 2, j);

	dtm().getRepresentation().warmUp(MasterEntity.class, "entityProp.entityProp.slaveEntityProp");
	assertEquals("Incorrect value 'i'.", 132, i);
	assertEquals("Incorrect value 'j'.", 5, j);
    }

    @Test
    public void test_that_WeakPropertyListeners_work() {
	i = 0; j = 0;
	IPropertyListener listener = new IPropertyListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean wasAddedOrRemoved, final Boolean oldState) {
		if (wasAddedOrRemoved == null) {
		    throw new IllegalArgumentException("'wasAddedOrRemoved' cannot be null.");
		}
		if (wasAddedOrRemoved) {
		    i++;
		} else {
		    j++;
		}
	    }

	    @Override
	    public boolean isInternal() {
	        return false;
	    }
	};
	dtm().getRepresentation().addWeakPropertyListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getRepresentation().excludeImmutably(MasterEntity.class, "integerProp");
	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	listener = null;
	System.gc();

	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "1 * 2 * integerProp", "Calc prop1", "Desc", CalculatedPropertyAttribute.NO_ATTR, "bigDecimalProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 1, j);
    }

    @Test
    public void test_that_PropertyDisablementListeners_work() {
	i = 0; j = 0;
	final IPropertyDisablementListener listener = new IPropertyDisablementListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenDisabled, final Boolean oldState) {
		if (hasBeenDisabled == null) {
		    throw new IllegalArgumentException("'hasBeenDisabled' cannot be null.");
		}
		if (hasBeenDisabled) {
		    i++;
		} else {
		    j++;
		}
	    }
	};
	dtm().getRepresentation().getFirstTick().addPropertyDisablementListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "integerProp");
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
	dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "bigDecimalProp");
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getRepresentation().warmUp(MasterEntity.class, "entityProp.entityProp.slaveEntityProp");
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 0, j);
    }

    @Test
    public void test_that_WeakPropertyDisablementListeners_work() {
	i = 0; j = 0;
	IPropertyDisablementListener listener = new IPropertyDisablementListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenDisabled, final Boolean oldState) {
		if (hasBeenDisabled == null) {
		    throw new IllegalArgumentException("'hasBeenDisabled' cannot be null.");
		}
		if (hasBeenDisabled) {
		    i++;
		} else {
		    j++;
		}
	    }
	};
	dtm().getRepresentation().getFirstTick().addWeakPropertyDisablementListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "integerProp");
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	listener = null;
	System.gc();

	dtm().getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
	dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "bigDecimalProp");
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);
    }
}