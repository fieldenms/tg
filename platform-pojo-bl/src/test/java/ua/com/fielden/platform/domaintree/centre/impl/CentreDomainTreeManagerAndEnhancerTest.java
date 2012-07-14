package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.IAnalysisListener;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancerTest;
import ua.com.fielden.platform.domaintree.testing.EntityForCentreCheckedProperties;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForCentreDomainTree;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;


/**
 * A test for {@link CentreDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManagerAndEnhancerTest extends AbstractDomainTreeManagerAndEnhancerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ICentreDomainTreeManagerAndEnhancer dtm() {
	return (ICentreDomainTreeManagerAndEnhancer) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(CentreDomainTreeManagerAndEnhancerTest.class);
    }

    public static Object createDtm_for_CentreDomainTreeManagerAndEnhancerTest() {
	return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_CentreDomainTreeManagerAndEnhancerTest());
    }

    public static Object createIrrelevantDtm_for_CentreDomainTreeManagerAndEnhancerTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeManagerAndEnhancerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeManagerAndEnhancerTest());
	rootTypes.add(MasterEntityForCentreDomainTree.class);
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	rootTypes.add(EntityForCentreCheckedProperties.class);
	return rootTypes;
    }

    public static void manageTestingDTM_for_CentreDomainTreeManagerAndEnhancerTest(final Object obj) {
	final ICentreDomainTreeManagerAndEnhancer dtmae = (ICentreDomainTreeManagerAndEnhancer) obj;

	CentreDomainTreeManagerTest.manageTestingDTM_for_CentreDomainTreeManagerTest(obj);

	// initialise analysis to ensure that equals / serialisation / copying works
	dtmae.initAnalysisManagerByDefault("New Pivot analysis.", AnalysisType.PIVOT);
	dtmae.getAnalysisManager("New Pivot analysis.").getFirstTick().check(MasterEntity.class, "booleanProp", true);
	dtmae.acceptAnalysisManager("New Pivot analysis.");
    }

    public static void performAfterDeserialisationProcess_for_CentreDomainTreeManagerAndEnhancerTest(final Object obj) {
    }

    public static void assertInnerCrossReferences_for_CentreDomainTreeManagerAndEnhancerTest(final Object obj) {
	assertInnerCrossReferences_for_AbstractDomainTreeManagerAndEnhancerTest(obj);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void test_that_domain_changes_are_not_permitted_for_used_in_analyses_properties() throws Exception {
	// enhance centre domain with new calculated property
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "MAX(1 * 2.5 * bigDecimalProp)", "Calc prop5", "Desc", CalculatedPropertyAttribute.NO_ATTR, "bigDecimalProp");
	dtm().getEnhancer().apply();

	dtm().initAnalysisManagerByDefault("Report", AnalysisType.SIMPLE);
	dtm().acceptAnalysisManager("Report");
	
	// use property in one of the analyses
	dtm().getAnalysisManager("Report").getSecondTick().check(MasterEntity.class, "calcProp5", true);
	
	// removal
	dtm().getEnhancer().removeCalculatedProperty(MasterEntity.class, "calcProp5");
	try {
	    dtm().getEnhancer().apply();
	    fail("Should be failed.");
	} catch (final IncorrectCalcPropertyException e) {
	}
	dtm().getEnhancer().discard();
	
	// 'significant' changes
	dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp5").setContextualExpression("1 * 2.5 * bigDecimalProp");
	try {
	    dtm().getEnhancer().apply();
	    fail("Should be failed.");
	} catch (final IncorrectCalcPropertyException e) {
	}
	dtm().getEnhancer().discard();
	
	dtm().getEnhancer().getCalculatedProperty(MasterEntity.class, "calcProp5").setTitle("Completely another title");
	try {
	    dtm().getEnhancer().apply();
	    fail("Should be failed.");
	} catch (final IncorrectCalcPropertyException e) {
	}
	dtm().getEnhancer().discard();
    }

    @Override
    @Test
    public void test_that_domain_changes_are_correctly_reflected_in_CHECKed_properties() {
	dtm().getFirstTick().setColumnsNumber(3);

	assertEquals("Incorrect checked properties.", Collections.emptyList(), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getFirstTick().check(MasterEntityForIncludedPropertiesLogic.class, "integerProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "1 * 2 * integerProp", "Prop1_mutably checked prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("integerProp", "entityProp.prop1_mutablyCheckedProp", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().addCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp", "1 * 2 * integerProp", "Prop2_mutably checked prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("integerProp", "entityProp.prop1_mutablyCheckedProp", "entityProp.prop2_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getFirstTick().swap(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1_mutablyCheckedProp", "entityProp.prop2_mutablyCheckedProp");
	assertEquals("Incorrect checked properties.", Arrays.asList("integerProp", "entityProp.prop2_mutablyCheckedProp", "entityProp.prop1_mutablyCheckedProp"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop1_mutablyCheckedProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("integerProp", "entityProp.prop2_mutablyCheckedProp", "0-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().getEnhancer().removeCalculatedProperty(MasterEntityForIncludedPropertiesLogic.class, "entityProp.prop2_mutablyCheckedProp");
	dtm().getEnhancer().apply();
	assertEquals("Incorrect checked properties.", Arrays.asList("integerProp", "1-placeholder-origin-0-1", "0-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    protected void test_initialisation_discarding_and_saving_of_Analyses(final AnalysisType analysisType, final String name) {
	assertNull("Should be null before creation.", dtm().getAnalysisManager(name));

	try {
	    dtm().removeAnalysisManager(name);
	    fail("The removal of non-existent analysis should fail.");
	} catch (final IllegalArgumentException e) {
	}

	// initialise a brand new instance of analysis (e.g. pivot)
	dtm().initAnalysisManagerByDefault(name, analysisType);
	try {
	    dtm().initAnalysisManagerByDefault(name, AnalysisType.SIMPLE);
	    fail("The creation of analysis with same name should fail.");
	} catch (final IllegalArgumentException e) {
	}
	assertTrue("The instance should be 'changed' after initialisation.", dtm().isChangedAnalysisManager(name));

	// obtain a current instance of analysis and alter it
	IAbstractAnalysisDomainTreeManager analysisMgr = dtm().getAnalysisManager(name);
	final Class<?> root = MasterEntityForCentreDomainTree.class;
	analysisMgr.getFirstTick().check(root, "simpleEntityProp", true);
	assertTrue("The instance should remain 'changed' after some operations.", dtm().isChangedAnalysisManager(name));

	// discard just created analysis
	dtm().discardAnalysisManager(name);
	assertNull("Should be null after discarding.", dtm().getAnalysisManager(name));
	assertFalse("The instance should be 'unchanged' after discard operation.", dtm().isChangedAnalysisManager(name));

	// initialise a brand new instance of analysis again (e.g. pivot)
	dtm().initAnalysisManagerByDefault(name, analysisType);
	assertTrue("The instance should be 'changed' after initialisation.", dtm().isChangedAnalysisManager(name));
	// obtain a current instance of analysis and alter it
	analysisMgr = dtm().getAnalysisManager(name);
	analysisMgr.getFirstTick().check(root, "simpleEntityProp", true);
	assertTrue("The instance should remain 'changed' after some operations.", dtm().isChangedAnalysisManager(name));

	// accept just created analysis
	dtm().acceptAnalysisManager(name);
	assertFalse("The instance should become 'unchanged' after accept operation.", dtm().isChangedAnalysisManager(name));
	assertNotNull("Should be not null after accepting.", dtm().getAnalysisManager(name));
	assertTrue("Should be checked.", dtm().getAnalysisManager(name).getFirstTick().isChecked(root, "simpleEntityProp"));

	// alter and discard
	dtm().getAnalysisManager(name).getFirstTick().check(root, "booleanProp", true);
	assertTrue("The instance should become 'changed' after some operations.", dtm().isChangedAnalysisManager(name));
	dtm().discardAnalysisManager(name);
	assertFalse("The instance should be 'unchanged' after discard operation.", dtm().isChangedAnalysisManager(name));
	assertNotNull("Should be not null after accept operation.", dtm().getAnalysisManager(name));
	assertTrue("Should be checked.", dtm().getAnalysisManager(name).getFirstTick().isChecked(root, "simpleEntityProp"));
	assertFalse("Should be unchecked after discarding.", dtm().getAnalysisManager(name).getFirstTick().isChecked(root, "booleanProp"));

	// remove just accepted analysis
	dtm().removeAnalysisManager(name);
	assertNull("Should be null after removal.", dtm().getAnalysisManager(name));
	assertFalse("The instance should be 'unchanged' after removal.", dtm().isChangedAnalysisManager(name));

	// initialise a brand new instance of analysis in order to test removal of the changed instance
	dtm().initAnalysisManagerByDefault(name, analysisType);
	assertTrue("The instance should be 'changed' after initialisation.", dtm().isChangedAnalysisManager(name));
	analysisMgr = dtm().getAnalysisManager(name);
	analysisMgr.getFirstTick().check(root, "simpleEntityProp", true);
	assertTrue("The instance should remain 'changed' after some operations.", dtm().isChangedAnalysisManager(name));
	dtm().removeAnalysisManager(name);
	assertNull("Should be null after removal.", dtm().getAnalysisManager(name));
	assertFalse("The instance should be 'unchanged' after removal.", dtm().isChangedAnalysisManager(name));
    }

    @Test
    public void test_initialisation_discarding_and_saving_Analyses() {
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.PIVOT, "A brand new PIVOT analysis");
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.SIMPLE, "A brand new SIMPLE analysis");
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.LIFECYCLE, "A brand new LIFECYCLE analysis");
    }

    private static int i, j;

    @Test
    public void test_that_AnalysisListeners_work() {
	i = 0; j = 0;
	final IAnalysisListener listener = new IAnalysisListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenInitialised, final Boolean oldState) {
		if (hasBeenInitialised == null) {
		    throw new IllegalArgumentException("'hasBeenInitialised' cannot be null.");
		}
		if (hasBeenInitialised) {
		    i++;
		} else {
		    j++;
		}
	    }
	};
	dtm().addAnalysisListener(listener);

	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	final String name = "Analysis";
	final AnalysisType analysisType = AnalysisType.SIMPLE;
	// initialisation
	dtm().initAnalysisManagerByDefault(name, analysisType);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	final Class<?> root = MasterEntityForCentreDomainTree.class;
	dtm().getAnalysisManager(name).getFirstTick().check(root, "simpleEntityProp", true);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	// discarding
	dtm().discardAnalysisManager(name);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	// initialisation again
	dtm().initAnalysisManagerByDefault(name, analysisType);
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	// removal (not accepted)
	dtm().removeAnalysisManager(name);
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 2, j);

	// initialisation again
	dtm().initAnalysisManagerByDefault(name, analysisType);
	assertEquals("Incorrect value 'i'.", 3, i);
	assertEquals("Incorrect value 'j'.", 2, j);

	dtm().acceptAnalysisManager(name);
	assertEquals("Incorrect value 'i'.", 3, i);
	assertEquals("Incorrect value 'j'.", 2, j);

	// removal (accepted)
	dtm().removeAnalysisManager(name);
	assertEquals("Incorrect value 'i'.", 3, i);
	assertEquals("Incorrect value 'j'.", 3, j);
    }

    @Test
    public void test_that_Analyses_freezing_works_fine() {
	final AnalysisType analysisType = AnalysisType.SIMPLE;
	final String name2 = "A brand new SIMPLE analysis";

	final String property1 = "booleanProp", property2 = "booleanProp2";

	// initialise a brand new instance of analysis again (e.g. pivot)
	dtm().initAnalysisManagerByDefault(name2, analysisType);
	final Class<?> root = MasterEntityForCentreDomainTree.class;
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property1, true);
	dtm().acceptAnalysisManager(name2);

	assertFalse("Should not be changed.", dtm().isChangedAnalysisManager(name2));
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property1, false);

	assertTrue("Should be changed after modification.", dtm().isChangedAnalysisManager(name2));
	assertFalse("Should be unchecked after modification.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertFalse("Should be NOT freezed.", dtm().isFreezedAnalysisManager(name2));

	IAbstractAnalysisDomainTreeManager currentMgrBeforeFreeze = dtm().getAnalysisManager(name2);

	// FREEEEEEEZEEEEEE all current changes
	dtm().freezeAnalysisManager(name2);

	assertFalse("The current mgr after freezing should not be identical to currentMgrBeforeFreeze.", dtm().getAnalysisManager(name2) == currentMgrBeforeFreeze);
	assertTrue("The current mgr after freezing should be equal to currentMgrBeforeFreeze.", dtm().getAnalysisManager(name2).equals(currentMgrBeforeFreeze));

	assertFalse("Should not be changed after freezing.", dtm().isChangedAnalysisManager(name2));
	assertFalse("Should be unchecked after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertTrue("Should be freezed.", dtm().isFreezedAnalysisManager(name2));

	////////////////////// Not permitted tasks after report has been freezed //////////////////////
	try {
	    dtm().freezeAnalysisManager(name2);
	    fail("Double freezing is not permitted. Please do you job -- save/discard and freeze again if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().initAnalysisManagerByDefault(name2, analysisType);
	    fail("Init action is not permitted while report is freezed. Please do you job -- save/discard and Init it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().removeAnalysisManager(name2);
	    fail("Removing is not permitted while report is freezed. Please do you job -- save/discard and remove it if you need!");
	} catch (final IllegalArgumentException e) {
	}

	// change smth.
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property1, true);
	assertTrue("Should be changed after modification after freezing.", dtm().isChangedAnalysisManager(name2));
	assertTrue("Should be checked after modification after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertTrue("Should be freezed.", dtm().isFreezedAnalysisManager(name2));

	// discard after-freezing changes
	dtm().discardAnalysisManager(name2);
	assertTrue("Should be changed after discard after freezing (due to existence of before-freezing changes).", dtm().isChangedAnalysisManager(name2));
	assertFalse("Should be unchecked after discard after freezing (according to before-freezing changes).", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertFalse("Should be NOT freezed.", dtm().isFreezedAnalysisManager(name2));

	// FREEEEEEEZEEEEEE all current changes (again)
	dtm().freezeAnalysisManager(name2);
	assertFalse("Should not be changed after freezing.", dtm().isChangedAnalysisManager(name2));
	assertFalse("Should be unchecked after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertFalse("Should be unchecked after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property2));
	assertTrue("Should be freezed.", dtm().isFreezedAnalysisManager(name2));

	// change smth.
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property2, true);
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property1, true);
	assertTrue("Should be changed after modification after freezing.", dtm().isChangedAnalysisManager(name2));
	assertTrue("Should be checked after modification after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property2));
	assertTrue("Should be checked after modification after freezing.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertTrue("Should be freezed.", dtm().isFreezedAnalysisManager(name2));

	currentMgrBeforeFreeze = dtm().getAnalysisManager(name2);

	// apply after-freezing changes
	dtm().acceptAnalysisManager(name2);

	assertFalse("The current mgr after freezing should not be identical to currentMgrBeforeFreeze.", dtm().getAnalysisManager(name2) == currentMgrBeforeFreeze);
	assertTrue("The current mgr after freezing should be equal to currentMgrBeforeFreeze.", dtm().getAnalysisManager(name2).equals(currentMgrBeforeFreeze));

	assertTrue("Should be changed after applying.", dtm().isChangedAnalysisManager(name2));
	assertTrue("Should be checked after applying.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property2));
	assertTrue("Should be checked after applying.", dtm().getAnalysisManager(name2).getFirstTick().isChecked(root, property1));
	assertFalse("Should be NOT freezed.", dtm().isFreezedAnalysisManager(name2));

	// return to the original version of the manager and check if it really is not changed
	dtm().getAnalysisManager(name2).getFirstTick().check(root, property2, false);

	assertFalse("Should not be changed after returning to original version.", dtm().isChangedAnalysisManager(name2));
    }

    @Test
    public void test_the_order_of_Analyses() {
	for (int i = 1; i <= 5; i++) {
	    dtm().initAnalysisManagerByDefault("Pivot " + i, AnalysisType.PIVOT);
	}
	assertEquals("The order of analyses is incorrect.", Arrays.asList("New Pivot analysis.", "Pivot 1", "Pivot 2", "Pivot 3", "Pivot 4", "Pivot 5"), dtm().analysisKeys());

	dtm().initAnalysisManagerByDefault("Pivot " + 6, AnalysisType.PIVOT);
	assertEquals("The order of analyses is incorrect.", Arrays.asList("New Pivot analysis.", "Pivot 1", "Pivot 2", "Pivot 3", "Pivot 4", "Pivot 5", "Pivot 6"), dtm().analysisKeys());

	dtm().removeAnalysisManager("Pivot " + 2);
	assertEquals("The order of analyses is incorrect.", Arrays.asList("New Pivot analysis.", "Pivot 1", "Pivot 3", "Pivot 4", "Pivot 5", "Pivot 6"), dtm().analysisKeys());

	dtm().discardAnalysisManager("Pivot " + 4);
	assertEquals("The order of analyses is incorrect.", Arrays.asList("New Pivot analysis.", "Pivot 1", "Pivot 3", "Pivot 5", "Pivot 6"), dtm().analysisKeys());

	dtm().acceptAnalysisManager("Pivot " + 5);
	assertEquals("The order of analyses is incorrect.", Arrays.asList("New Pivot analysis.", "Pivot 1", "Pivot 3", "Pivot 5", "Pivot 6"), dtm().analysisKeys());
    }

}
