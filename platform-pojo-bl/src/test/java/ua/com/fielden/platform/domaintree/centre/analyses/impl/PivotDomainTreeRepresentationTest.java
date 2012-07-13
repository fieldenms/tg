package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;

/**
 * A test for "pivot analyses" tree representation.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
//    @Override
//    protected IPivotDomainTreeManager dtm() {
//	return (IPivotDomainTreeManager) ((ICentreDomainTreeManagerAndEnhancer) just_a_dtm()).getAnalysisManager("Report");
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    /**
//     * Creates root types.
//     *
//     * @return
//     */
//    protected static Set<Class<?>> createRootTypes_for_PivotDomainTreeRepresentationTest() {
//	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest();
//	return rootTypes;
//    }
//
//    /**
//     * Provides a testing configuration for the manager.
//     *
//     * @param dtm
//     */
//    protected static void manageTestingDTM_for_PivotDomainTreeRepresentationTest(final IPivotDomainTreeManager dtm) {
//	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
//    }
//
//    @BeforeClass
//    public static void initDomainTreeTest() {
//	final ICentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_PivotDomainTreeRepresentationTest());
//	centre.initAnalysisManagerByDefault("Report", AnalysisType.PIVOT);
//	final IPivotDomainTreeManager dtm = (IPivotDomainTreeManager) centre.getAnalysisManager("Report");
//	manageTestingDTM_for_PivotDomainTreeRepresentationTest(dtm);
//	centre.acceptAnalysisManager("Report");
//	setDtmArray(serialiser().serialise(centre));
//    }
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IPivotDomainTreeRepresentation dtm() {
	return (IPivotDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(PivotDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_PivotDomainTreeRepresentationTest() {
	return new PivotDomainTreeRepresentation(serialiser(), createRootTypes_for_PivotDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_PivotDomainTreeRepresentationTest() {
	return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_PivotDomainTreeRepresentationTest());
    }

    protected static Set<Class<?>> createRootTypes_for_PivotDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_PivotDomainTreeRepresentationTest(final Object obj) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_PivotDomainTreeRepresentationTest(final Object obj) {
	performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void assertInnerCrossReferences_for_PivotDomainTreeRepresentationTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_PivotDomainTreeRepresentationTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeRepresentationTest();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Test
    public void test_that_excluded_properties_actions_for_second_ticks_cause_exceptions_for_all_specific_logic() {
	super.test_that_excluded_properties_actions_for_second_ticks_cause_exceptions_for_all_specific_logic();
	final String message = "Excluded property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set width by default
		try {
		    dtm().getSecondTick().getWidthByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().setWidthByDefault(MasterEntity.class, name, 85);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// get/set width by default
		try {
		    dtm().getFirstTick().getWidthByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setWidthByDefault(MasterEntity.class, name, 85);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");
    }

    @Test
    public void test_that_widths_by_default_for_first_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// default width should be 80
	checkOrSetMethodValuesForOneLevel(80, "dateProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(80, "integerProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(80, "booleanProp", dtm().getFirstTick(), "getWidthByDefault");

	// Alter DEFAULT and check //
	checkOrSetMethodValuesForOneLevel(85, "dateProp", dtm().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForOneLevel(85, "dateProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(85, "integerProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(85, "booleanProp", dtm().getFirstTick(), "getWidthByDefault");

	checkOrSetMethodValuesForOneLevel(95, "booleanProp", dtm().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForOneLevel(95, "dateProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(95, "integerProp", dtm().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValuesForOneLevel(95, "booleanProp", dtm().getFirstTick(), "getWidthByDefault");
    }

    @Test
    public void test_that_widths_by_default_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// default width should be 80
	checkOrSetMethodValues(80, "dateProp", dtm().getSecondTick(), "getWidthByDefault");
	checkOrSetMethodValues(80, "integerProp", dtm().getSecondTick(), "getWidthByDefault");

	// Alter DEFAULT and check //
	checkOrSetMethodValues(85, "dateProp", dtm().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getSecondTick(), "getWidthByDefault");
    }
}
