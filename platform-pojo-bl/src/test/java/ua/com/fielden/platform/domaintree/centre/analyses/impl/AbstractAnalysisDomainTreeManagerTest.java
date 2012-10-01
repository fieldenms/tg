package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager.IPropertyOrderingListener;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager.IPropertyUsageListener;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerTest;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManager1;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.utils.Pair;


/**
 * A test for {@link AbstractAnalysisDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class AbstractAnalysisDomainTreeManagerTest extends AbstractDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IAbstractAnalysisDomainTreeManager dtm() {
	return (IAbstractAnalysisDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(AbstractAnalysisDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_AbstractAnalysisDomainTreeManagerTest() {
	return new AbstractAnalysisDomainTreeManager1(serialiser(), createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_AbstractAnalysisDomainTreeManagerTest() {
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(dtm);
	return dtm;
    }

    protected static void enhanceManagerWithBasicCalculatedProperties(final ICentreDomainTreeManagerAndEnhancer dtm) {
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(integerProp)", "Int agg expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(bigDecimalProp)", "Big decimal agg expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "bigDecimalProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(moneyProp)", "Money agg expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "YEAR(dateProp)", "Date expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");

	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(moneyProp)", "Unchecked agg expr prop 1", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "MAX(moneyProp)", "Unchecked agg expr prop 2", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.entityProp", "MAX(moneyProp)", "Unchecked agg expr prop 3", "Desc", CalculatedPropertyAttribute.NO_ATTR, "moneyProp");

	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "YEAR(dateProp)", "Unchecked date expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "YEAR(dateProp)", "Unchecked date expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	dtm.getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.entityProp", "YEAR(dateProp)", "Unchecked date expr prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	dtm.getEnhancer().apply();
    }

    protected static Set<Class<?>> createRootTypes_for_AbstractAnalysisDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeManagerTest());
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	return rootTypes;
    }

    public static void manageTestingDTM_for_AbstractAnalysisDomainTreeManagerTest(final Object obj) {
	final IAbstractAnalysisDomainTreeManager dtm = (IAbstractAnalysisDomainTreeManager) obj;

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);

	manageTestingDTM_for_AbstractDomainTreeTest(dtm.getRepresentation());

	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "intAggExprProp")) {
	    dtm.getSecondTick().check(MasterEntity.class, "intAggExprProp", true);
	}
	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "bigDecimalAggExprProp")) {
	    dtm.getSecondTick().check(MasterEntity.class, "bigDecimalAggExprProp", true);
	}
	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "moneyAggExprProp")) {
	    dtm.getSecondTick().check(MasterEntity.class, "moneyAggExprProp", true);
	}
	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "dateExprProp")) {
	    dtm.getFirstTick().check(MasterEntity.class, "dateExprProp", true);
	}
	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "simpleEntityProp")) {
	    dtm.getFirstTick().check(MasterEntity.class, "simpleEntityProp", true);
	}
	if (!dtm.getRepresentation().isExcludedImmutably(MasterEntity.class, "booleanProp")) {
	    dtm.getFirstTick().check(MasterEntity.class, "booleanProp", true);
	}
    }

    public static void performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeManagerTest(final Object obj) {
	CentreDomainTreeManagerAndEnhancer.initAnalysisManagerReferencesOn((IAbstractAnalysisDomainTreeManager) obj, (ICentreDomainTreeManagerAndEnhancer) irrelevantDtm());
    }

    public static void assertInnerCrossReferences_for_AbstractAnalysisDomainTreeManagerTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractDomainTreeManagerTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeManagerTest() {
	final String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual = new String [1];
	fieldWhichReferenceShouldNotBeDistictButShouldBeEqual[0] = "parentCentreDomainTreeManager";
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
	// this test is redundant due to lack of special isChecked logic in AnalysisDomainTreeManager
    }

    @Override
    @Test
    public void test_that_CHECK_state_for_mutated_by_Check_method_properties_is_actually_mutated() {
	// checked properties, mutated_by_Check_method
	final String message = "Checked property, defined in isChecked() contract, should return 'true' CHECK state, and after manual mutation its state should be desired.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		if (!dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name)) {
		    dtm().getFirstTick().check(MasterEntity.class, name, false);
		    isCheck_equals_to_state(name, message, false);
		    dtm().getFirstTick().check(MasterEntity.class, name, true);
		    isCheck_equals_to_state(name, message, true);
		}
	    }
	}, "mutablyCheckedProp");
    }


    ////////////////////// 1. Specific analysis-manager logic //////////////////////
    @Test
    public void test_that_manager_visibility_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be TRUE
	assertTrue("Analysis manager by default should be visible", dtm().isVisible());

	// Alter and check //
	assertTrue("The manager reference should be the same",dtm() == dtm().setVisible(false));
	assertFalse("Analysis manager should be invisible", dtm().isVisible());
    }

    @Test
    public void test_that_unchecked_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Unchecked property should cause IllegalArgument exception.";
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		// FIRST TICK
		// usage manager
		try {
		    dtm().getFirstTick().isUsed(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().use(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "uncheckedDateExprProp");
	oneLevel(new IAction() {
	    public void action(final String name) {
		// SECOND TICK
		//usage manager
		try {
		    dtm().getSecondTick().isUsed(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().use(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// ordering
		try {
		    dtm().getSecondTick().toggleOrdering(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "uncheckedAggExprProp1", "uncheckedAggExprProp2", "uncheckedAggExprProp3");
    }

    @Test
    public void test_that_unused_properties_ToggleOrdering_action_for_second_tick_cause_exception() {
	final String message = "Unused property should cause IllegalArgument exception.";
	oneLevel(new IAction() {
	    public void action(final String name) {
		// SECOND TICK
		// ordering
		try {
		    dtm().getSecondTick().toggleOrdering(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "intAggExprProp", "bigDecimalAggExprProp", "moneyAggExprProp");
    }

    @Test
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered() {
	// it is necessary to make properties "used" to be able to toggle ordering
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", true);
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);

	// THE FIRST TIME -- returns DEFAULT VALUES //
	// Default ordering for the analysis's second tick should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithCompositeKey.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter and check //
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.DESCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
    }

    @Test
    public void test_that_Orderings_for_second_tick_can_be_changed_based_on_Usage_changes() {
	// it is necessary to make properties "used" to be able to toggle ordering
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", true);
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "bigDecimalAggExprProp");
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "moneyAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("bigDecimalAggExprProp", Ordering.DESCENDING), new Pair<String, Ordering>("moneyAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	// un-use middle property
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", false);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	// un-use right property
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", false);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	// un-use last property
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", false);
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
    }

    @Test
    public void test_that_usage_management_works_correctly_for_first_tick() {
	// At the beginning the list of used properties should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));

	// Add "use properties" and see whether list of "used properties" is correctly ordered.
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("dateExprProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityProp", true);
	assertTrue("The property should be used", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityProp"));
	assertEquals("value is incorrect.", Arrays.asList("dateExprProp", "simpleEntityProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp", "booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityProp"));
	assertEquals("value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
    }

    @Test
    public void test_that_usage_management_works_correctly_for_second_tick() {
	// At the beginning the list of used properties should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().usedProperties(MasterEntity.class));

	// Add "use properties" and see whether list of "used properties" is correctly ordered.
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "moneyAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "intAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("intAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", true);
	assertTrue("The property should be used", dtm().getSecondTick().isUsed(MasterEntity.class, "bigDecimalAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("intAggExprProp", "bigDecimalAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "intAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("bigDecimalAggExprProp", "moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "moneyAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("bigDecimalAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "bigDecimalAggExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getSecondTick().isUsed(MasterEntity.class, "bigDecimalAggExprProp"));
	assertEquals("value is incorrect.", Arrays.asList(), dtm().getSecondTick().usedProperties(MasterEntity.class));
    }

    @Override
    public void test_that_CHECKed_properties_order_is_correct() throws Exception {
    }

    @Override
    public void test_that_CHECKed_properties_Move_Swap_operations_work() throws Exception {
    }

    @Override
    public void test_that_CHECKed_properties_order_is_correct_and_can_be_altered() throws Exception {
    }

    @Override
    public void test_that_PropertyCheckingListeners_work() {
    }

    private static int i, j;

    @Test
    public void test_that_PropertyUsageListeners_work() {
	i = 0; j = 0;
	final IPropertyUsageListener listener = new IPropertyUsageListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenUsed, final Boolean oldState) {
		if (hasBeenUsed == null) {
		    throw new IllegalArgumentException("'hasBeenUsed' cannot be null.");
		}
		if (hasBeenUsed) {
		    i++;
		} else {
		    j++;
		}
	    }
	};
	dtm().getFirstTick().addPropertyUsageListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	final String property = "booleanProp";
	dtm().getFirstTick().check(MasterEntity.class, property, true);
	dtm().getFirstTick().use(MasterEntity.class, property, true);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getFirstTick().use(MasterEntity.class, property, false);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	dtm().getRepresentation().warmUp(MasterEntity.class, "entityProp.entityProp.slaveEntityProp");
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 1, j);
    }

    @Test
    public void test_that_PropertyOrderingListeners_work() {
	// it is necessary to make properties "used" to be able to toggle ordering
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);

	i = 0;
	final IPropertyOrderingListener listener = new IPropertyOrderingListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final List<Pair<String, Ordering>> newOrderedProperties, final List<Pair<String, Ordering>> oldState) {
		i++;
	    }
	};
	dtm().getSecondTick().addPropertyOrderingListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);

	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	assertEquals("Incorrect value 'i'.", 1, i);

	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	assertEquals("Incorrect value 'i'.", 2, i);

	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	assertEquals("Incorrect value 'i'.", 3, i);

	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("intAggExprProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));

	assertEquals("Incorrect value 'i'.", 4, i);
    }
}
