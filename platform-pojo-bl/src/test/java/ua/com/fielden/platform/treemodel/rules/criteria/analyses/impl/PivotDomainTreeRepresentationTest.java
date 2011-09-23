package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeManager.IPivotDomainTreeManagerAndEnhancer;

/**
 * A test for "analyses" tree representation.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
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
    protected static Set<Class<?>> createRootTypes_for_PivotDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest();
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_PivotDomainTreeRepresentationTest(final IPivotDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IPivotDomainTreeManagerAndEnhancer dtm = new PivotDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_PivotDomainTreeRepresentationTest());
	manageTestingDTM_for_PivotDomainTreeRepresentationTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
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
		    dtm().getRepresentation().getSecondTick().getWidthByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().setWidthByDefault(MasterEntity.class, name, 85);
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
	checkOrSetMethodValues(80, "dateProp", dtm().getRepresentation().getFirstTick(), "getWidthByDefault");
	checkOrSetMethodValues(80, "integerProp", dtm().getRepresentation().getFirstTick(), "getWidthByDefault");

	// Alter DEFAULT and check //
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getFirstTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getFirstTick(), "getWidthByDefault");
    }

    @Test
    public void test_that_widths_by_default_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// default width should be 80
	checkOrSetMethodValues(80, "dateProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");
	checkOrSetMethodValues(80, "integerProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");

	// Alter DEFAULT and check //
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValues(85, "dateProp", dtm().getRepresentation().getSecondTick(), "getWidthByDefault");
    }
}
