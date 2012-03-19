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
    public void test_that_excluded_properties_actions_for_second_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Excluded property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set width by default
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
	}, "excludedManuallyProp");
    }

    @Test
    public void test_that_Widths_for_second_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be 80
	checkOrSetMethodValues(80, "dateProp", dtm().getSecondTick(), "getWidth");
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getSecondTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValues(95, "dateProp", dtm().getSecondTick(), "setWidth", int.class);

	checkOrSetMethodValues(95, "dateProp", dtm().getSecondTick(), "getWidth");
    }

    @Test
    public void test_that_Widths_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be 80
	checkOrSetMethodValues(80, "dateProp", dtm().getFirstTick(), "getWidth");
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getFirstTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValues(95, "dateProp", dtm().getFirstTick(), "setWidth", int.class);

	checkOrSetMethodValues(95, "dateProp", dtm().getFirstTick(), "getWidth");
    }

    @Override
    public void test_that_PropertyUsageListeners_work() {
    }

    @Override
    public void test_that_PropertyOrderingListeners_work() {
    }
}
