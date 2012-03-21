package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;


/**
 * A test for {@link AbstractAnalysisDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManagerTest extends AbstractAnalysisDomainTreeManagerTest {
    @Override
    protected IPivotDomainTreeManagerAndEnhancer dtm() {
	return (IPivotDomainTreeManagerAndEnhancer) super.dtm();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_PivotDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractAnalysisDomainTreeManagerTest();
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_PivotDomainTreeManagerTest(final IPivotDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeManagerTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IPivotDomainTreeManagerAndEnhancer dtm = new PivotDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_PivotDomainTreeManagerTest());
	manageTestingDTM_for_PivotDomainTreeManagerTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
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
    public void test_that_Widths_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	final String property = "booleanProp";

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getFirstTick().check(MasterEntity.class, name, true);
		dtm().getFirstTick().use(MasterEntity.class, name, true);
	    }
	}, property);

	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be 80
	checkOrSetMethodValuesForNonCollectional(80, property, dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValuesForNonCollectional(85, property, dtm().getRepresentation().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForNonCollectional(85, property, dtm().getFirstTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValuesForNonCollectional(95, property, dtm().getFirstTick(), "setWidth", int.class);

	checkOrSetMethodValuesForNonCollectional(95, property, dtm().getFirstTick(), "getWidth");
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
    public void test_that_PropertyUsageListeners_work() {
    }

    @Override
    public void test_that_PropertyOrderingListeners_work() {
    }
}
