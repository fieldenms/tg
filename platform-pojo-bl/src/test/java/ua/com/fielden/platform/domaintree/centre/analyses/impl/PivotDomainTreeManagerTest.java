package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;


/**
 * A test for {@link AbstractAnalysisDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManagerTest extends AbstractAnalysisDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IPivotDomainTreeManager dtm() {
	return (IPivotDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(PivotDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_PivotDomainTreeManagerTest() {
	return new PivotDomainTreeManager(serialiser(), createRootTypes_for_PivotDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_PivotDomainTreeManagerTest() {
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_PivotDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(dtm);
	return dtm;
    }

    protected static Set<Class<?>> createRootTypes_for_PivotDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_PivotDomainTreeManagerTest(final Object obj) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_PivotDomainTreeManagerTest(final Object obj) {
	performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void assertInnerCrossReferences_for_PivotDomainTreeManagerTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractAnalysisDomainTreeManagerTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_PivotDomainTreeManagerTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeManagerTest();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_unused_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Unused property should cause IllegalArgument exception.";

	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set width
		try {
		    dtm().getFirstTick().getWidth(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setWidth(MasterEntity.class, name, 85);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp", "dateProp", "integerProp", "booleanProp", "intAggExprProp");

	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set width
		try {
		    dtm().getSecondTick().getWidth(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().setWidth(MasterEntity.class, name, 85);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp", "dateProp", "integerProp", "booleanProp", "intAggExprProp");
    }

    @Test
    public void test_that_Widths_for_first_tick_are_default_for_the_first_time_and_can_be_altered_and_are_treated_as_single_column() {
	final String property = "booleanProp";
	final String property2 = "entityProp.booleanProp";
	final String property3 = "entityProp.entityProp.booleanProp";

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getFirstTick().check(MasterEntity.class, name, true);
		dtm().getFirstTick().use(MasterEntity.class, name, true);
	    }
	}, property);

	// There are three "used" properties "booleanProp", "entityProp.booleanProp", "entityProp.entityProp.booleanProp".
	// They should be used as a couple for setting / determining a width.

	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be 80
	checkOrSetMethodValuesForOneLevel(80, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(80, property2, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(80, property3, dtm().getFirstTick(), "getWidth");

	checkOrSetMethodValuesForOneLevel(85, property, dtm().getRepresentation().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForOneLevel(85, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(85, property2, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(85, property3, dtm().getFirstTick(), "getWidth");

	checkOrSetMethodValuesForOneLevel(87, property2, dtm().getRepresentation().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForOneLevel(87, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(87, property2, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(87, property3, dtm().getFirstTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValuesForOneLevel(95, property, dtm().getFirstTick(), "setWidth", int.class);

	checkOrSetMethodValuesForOneLevel(95, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(95, property2, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(95, property3, dtm().getFirstTick(), "getWidth");

	checkOrSetMethodValuesForOneLevel(97, property3, dtm().getFirstTick(), "setWidth", int.class);

	checkOrSetMethodValuesForOneLevel(97, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(97, property2, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(97, property3, dtm().getFirstTick(), "getWidth");
    }

    @Test
    public void test_that_Widths_for_second_tick_are_default_for_the_first_time_and_can_be_altered() {
	final String property = "intAggExprProp";

	oneLevel(new IAction() {
	    public void action(final String name) {
		dtm().getSecondTick().check(MasterEntity.class, name, true);
		dtm().getSecondTick().use(MasterEntity.class, name, true);
	    }
	}, property);

	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be 80
	checkOrSetMethodValuesForOneLevel(80, property, dtm().getSecondTick(), "getWidth");
	checkOrSetMethodValuesForOneLevel(85, property, dtm().getRepresentation().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForOneLevel(85, property, dtm().getSecondTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValuesForOneLevel(95, property, dtm().getSecondTick(), "setWidth", int.class);

	checkOrSetMethodValuesForOneLevel(95, property, dtm().getSecondTick(), "getWidth");
    }

    @Override
    @Test
    public void test_that_usage_management_works_correctly_for_first_tick() {
	dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", true);
	assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
	assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", true);
        assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", true);
        assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", false);
        assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", false);
        assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
    }

    @Override
    @Test
    public void test_that_usage_management_works_correctly_for_second_tick() {
        super.test_that_usage_management_works_correctly_for_second_tick();
        dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", true);
        dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);
	assertEquals("The list of used properties for second tick is incorrect.", Arrays.asList("moneyAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	assertEquals("The list of used properties for second tick is incorrect.", Arrays.asList("intAggExprProp"), dtm().getSecondTick().usedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "intAggExprProp");
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", false);
	assertEquals("The ordering list of used properties for the second tick is incorrect", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
    }

    @Test
    public void test_that_second_usage_manager_works_correctly_for_first_tick(){
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", true);
	assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList("booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
	dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "booleanProp", true);
        assertEquals("List of used properties for the first tick is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList("booleanProp"), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", true);
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", false);
        assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
        dtm().getSecondTick().use(MasterEntity.class, "moneyAggExprProp", true);
        dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", true);
	try {
	    dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", true);
	    fail("Can not use property of the second usage manager");
	} catch (final IllegalStateException e) {
	    assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
	}
	dtm().getSecondTick().use(MasterEntity.class, "intAggExprProp", false);
	dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", true);
	assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
	dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", false);
	dtm().getFirstTick().getSecondUsageManager().use(MasterEntity.class, "dateExprProp", false);
	assertEquals("List of used properties for the first tick and second usage manager is incorrect.", Arrays.asList(), dtm().getFirstTick().getSecondUsageManager().usedProperties(MasterEntity.class));
    }

    @Override
    public void test_that_PropertyUsageListeners_work() {
    }

    @Override
    public void test_that_WeakPropertyUsageListeners_work() {
    }

    @Override
    public void test_that_PropertyOrderingListeners_work() {
    }

    @Override
    public void test_that_WeakPropertyOrderingListeners_work() {
    }
}
