package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForGlobalDomainTree;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManagerTest extends GlobalDomainTreeRepresentationTest {
    private static final Class<?> ROOT = MasterEntityForGlobalDomainTree.class;
    private final String NON_BASE_USERS_SAVE_AS = "NON_BASE_USER'S_SAVE_AS";
    private final String  property = "simpleEntityProp";

    private IGlobalDomainTreeManager initGlobalManagerWithEntityCentre() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(ROOT, null);
	managerForNonBaseUser.saveAsEntityCentreManager(ROOT, null, NON_BASE_USERS_SAVE_AS);
	return managerForNonBaseUser;
    }

    private ILocatorManager prepareGlobalManagerEntityCentreForLocatorActions(final IGlobalDomainTreeManager managerForNonBaseUser) {
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(ROOT, NON_BASE_USERS_SAVE_AS);
	dtm.getFirstTick().check(ROOT, property, true);
	return dtm.getFirstTick();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-CENTRES/MASTERS LOCATORS MANAGEMENT /////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_CENTRE_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = initGlobalManagerWithEntityCentre();
	final ILocatorManager locatorManager = prepareGlobalManagerEntityCentreForLocatorActions(managerForNonBaseUser);
	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(managerForNonBaseUser, property, locatorManager);

	// should be accepted before saving
	try {
	    managerForNonBaseUser.saveEntityCentreManager(ROOT, NON_BASE_USERS_SAVE_AS);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(ROOT, property);
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(ROOT, NON_BASE_USERS_SAVE_AS);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(ROOT, NON_BASE_USERS_SAVE_AS);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(ROOT);
	final ILocatorManager locatorManager = managerForNonBaseUser.getEntityMasterManager(ROOT);;

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(managerForNonBaseUser, property, locatorManager);

	// should be accepted before saving
	try {
	    managerForNonBaseUser.saveEntityMasterManager(ROOT);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(ROOT, property);
	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(ROOT);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(ROOT);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(final String property, final ILocatorManager locatorManager) {
	assertNotNull("Should be not null.", locatorManager.getLocatorManager(ROOT, property));

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION AS RELOADING ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a brand new instance of locator
	locatorManager.resetLocatorManagerToDefault(ROOT, property);
	assertFalse("The instance should not be 'changed'.", locatorManager.isChangedLocatorManager(ROOT, property));
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManager locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION & MODIFICATION //////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	initDefaultLocatorForSomeTestType(managerForNonBaseUser);

	assertNull("Should be null before creation.", locatorManager.getLocatorManager(ROOT, property));

	// initialise a brand new instance of locator
	locatorManager.refreshLocatorManager(ROOT, property);
	// MODIFY
	locatorManager.getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	assertTrue("The instance should be 'changed' after initialisation & modification (the user has checked some property!).", locatorManager.isChangedLocatorManager(ROOT, property));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-CENTRES MANAGEMENT //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(ROOT, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(ROOT, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(ROOT, null));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(ROOT, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", nonBaseMgr1.getEntityCentreManager(ROOT, null));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", nonBaseMgr2.getEntityCentreManager(ROOT, null));
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	try {
	    baseMgr.initEntityCentreManager(ROOT, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_NON_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	try {
	    nonBaseMgr.initEntityCentreManager(ROOT, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create PRINCIPLE and BASE SAVE AS REPORT for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "BASE SAVE AS REPORT");

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(ROOT, null));
	newBaseMgr.initEntityCentreManager(ROOT, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(ROOT, "BASE SAVE AS REPORT"));
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and BASE/NON-BASE SAVE AS REPORTS for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add(null); add("BASE SAVE AS REPORT"); }}, baseMgr.entityCentreNames(ROOT));
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, "BASE SAVE AS REPORT");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, "BASE SAVE AS REPORT", "NON-BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, nonBaseMgr.entityCentreNames(ROOT));

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(ROOT, null);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(ROOT, null));
	newNonBaseMgr.initEntityCentreManager(ROOT, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(ROOT, "BASE SAVE AS REPORT"));
	newNonBaseMgr.initEntityCentreManager(ROOT, "NON-BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(ROOT, "NON-BASE SAVE AS REPORT"));
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, newNonBaseMgr.entityCentreNames(ROOT));
    }

    @Test
    public void test_that_CENTRE_WITH_SAME_TITLE_retrieval_and_initialisation_and_removal_works_fine_for_NON_BASE_and_BASE_user_and_hides_base_CENTRE_for_NON_BASE_user() {
	// create PRINCIPLE and SAME report for USER2 and SAME report for user USER1
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "SAME");
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.getEntityCentreManager(ROOT, null).getFirstTick().check(ROOT, "integerProp", false);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "SAME");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(ROOT, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(ROOT, "SAME"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(ROOT, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));

	final IGlobalDomainTreeManager newBaseMgr = createManagerForBaseUser();
	newBaseMgr.initEntityCentreManager(ROOT, "SAME");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(ROOT, "SAME"));
	assertFalse("The state is incorrect.", newBaseMgr.getEntityCentreManager(ROOT, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));

	// remove "SAME" for USER2
	newNonBaseMgr.removeEntityCentreManager(ROOT, "SAME");
	assertNull("Should be removed.", newNonBaseMgr.getEntityCentreManager(ROOT, "SAME"));
	newNonBaseMgr.initEntityCentreManager(ROOT, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(ROOT, "SAME"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(ROOT, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));
    }

    @Test
    public void test_that_not_initialised_CENTRE_discarding_saving_saveAsing_isChangeding_removing_fails() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	try {
	    newNonBaseMgr.discardEntityCentreManager(ROOT, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveEntityCentreManager(ROOT, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveAsEntityCentreManager(ROOT, "REPORT", "NEW-REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.removeEntityCentreManager(ROOT, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_isChanged_works_fine_after_modification_saving_discarding() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	assertFalse("Should not be changed after saveAs.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "integerProp", false);
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	nonBaseMgr.saveEntityCentreManager(ROOT, "REPORT");
	assertFalse("Should not be changed after save.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	assertTrue("Should be modified.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));

	nonBaseMgr.discardEntityCentreManager(ROOT, "REPORT");
	assertFalse("Should not be changed after discard.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
    }

    @Test
    public void test_that_CENTRE_freezing_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	assertFalse("Should not be changed.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "integerProp", false);

	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should be unchecked after modification.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should not be freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	ICentreDomainTreeManagerAndEnhancer currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(ROOT, "REPORT");

	// FREEEEEEEZEEEEEE all current changes
	nonBaseMgr.freezeEntityCentreManager(ROOT, "REPORT");

	assertFalse("The current mgr after freezing should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after freezing should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").equals(currentMgrBeforeFreeze));

	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	////////////////////// Not permitted tasks after report has been freezed //////////////////////
	try {
	    nonBaseMgr.freezeEntityCentreManager(ROOT, "REPORT");
	    fail("Double freezing is not permitted. Please do you job -- save/discard and freeze again if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.initEntityCentreManager(ROOT, "REPORT");
	    fail("Init action is not permitted while report is freezed. Please do you job -- save/discard and Init it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveAsEntityCentreManager(ROOT, "REPORT", "NEW_REPORT_TITLE");
	    fail("Saving As is not permitted while report is freezed. Please do you job -- save/discard and SaveAs if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.removeEntityCentreManager(ROOT, "REPORT");
	    fail("Removing is not permitted while report is freezed. Please do you job -- save/discard and remove it if you need!");
	} catch (final IllegalArgumentException e) {
	}

	// change smth.
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(ROOT, "REPORT");
	assertTrue("Should be changed after discard after freezing (due to existence of before-freezing changes).", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should be unchecked after discard after freezing (according to before-freezing changes).", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	// FREEEEEEEZEEEEEE all current changes (again)
	nonBaseMgr.freezeEntityCentreManager(ROOT, "REPORT");
	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	// change smth.
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "bigDecimalProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(ROOT, "REPORT");

	// save (precisely "apply") after-freezing changes
	nonBaseMgr.saveEntityCentreManager(ROOT, "REPORT");

	assertFalse("The current mgr after 'acceptance unfreezing' should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after 'acceptance unfreezing' should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").equals(currentMgrBeforeFreeze));

	assertTrue("Should be changed after applying.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));

	// return to the original version of the manager and check if it really is not changed
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getFirstTick().check(ROOT, "bigDecimalProp", false);

	assertFalse("Should not be changed after returning to original version.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(ROOT, "REPORT"));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_changed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after initialisation.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'changed' after initialisation.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(ROOT, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(ROOT, "REPORT");
	assertFalse("The CENTRE should be 'unchanged' after unfreezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should remain 'not null' after unfreezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after unfreezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_freezed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").acceptAnalysisManager(name);
	nonBaseMgr.saveEntityCentreManager(ROOT, "REPORT");
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name).setVisible(false);
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").freezeAnalysisManager(name);
	assertTrue("The CENTRE should be 'changed' due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null'.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'freezed' after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should be 'unchanged' after freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(ROOT, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(ROOT, "REPORT");
	assertTrue("The CENTRE should be 'changed' after unfreezing due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(ROOT, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_reloading_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.getEntityCentreManager(ROOT, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// modify and reload instantly
	nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").setRunAutomatically(false);
	nonBaseMgr.initEntityCentreManager(ROOT, "REPORT");
	assertTrue("The state is incorrect after reloading.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT").isRunAutomatically());
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// remove report
	nonBaseMgr.removeEntityCentreManager(ROOT, "REPORT");
	assertNull("Should be removed.", nonBaseMgr.getEntityCentreManager(ROOT, "REPORT"));

	try {
	    nonBaseMgr.removeEntityCentreManager(ROOT, null);
	    fail("Removing of not own reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_BASE_user() {
	// create PRINCIPLE and REPORT report for USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "REPORT");

	// remove report
	baseMgr.removeEntityCentreManager(ROOT, "REPORT");
	assertNull("Should be removed.", baseMgr.getEntityCentreManager(ROOT, "REPORT"));

	try {
	    baseMgr.removeEntityCentreManager(ROOT, null);
	    fail("Removing of principle reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_saving_works_fine_for_own_reports_and_fails_for_not_own_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "BASE");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.initEntityCentreManager(ROOT, "BASE");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, "BASE", "BASE-DERIVED");

	baseMgr.saveEntityCentreManager(ROOT, null);
	baseMgr.saveEntityCentreManager(ROOT, "BASE");

	try {
	    nonBaseMgr.saveEntityCentreManager(ROOT, null);
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveEntityCentreManager(ROOT, "BASE");
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	nonBaseMgr.saveEntityCentreManager(ROOT, "BASE-DERIVED");
    }

    @Test
    public void test_that_CENTRE_savingAs_works_fine_for_all_visible_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(ROOT, null);
	baseMgr.saveAsEntityCentreManager(ROOT, null, "BASE");
	baseMgr.saveAsEntityCentreManager(ROOT, "BASE", "BASE-DERIVED-1");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(ROOT, null);
	nonBaseMgr.initEntityCentreManager(ROOT, "BASE");
	nonBaseMgr.initEntityCentreManager(ROOT, "BASE-DERIVED-1");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, null, "PRINCIPLE-DERIVED");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, "BASE", "BASE-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, "BASE-DERIVED-1", "BASE-DERIVED-1-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(ROOT, "PRINCIPLE-DERIVED", "PRINCIPLE-DERIVED-DERIVED-1");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-MASTERS MANAGEMENT //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", baseMgr.getEntityMasterManager(ROOT));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", baseMgr.getEntityMasterManager(ROOT));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT));
	assertEquals("Should be equal.", nonBaseMgr.getEntityMasterManager(ROOT), baseMgr.getEntityMasterManager(ROOT));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr1.getEntityMasterManager(ROOT));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr2.getEntityMasterManager(ROOT));
	assertEquals("Should be equal.", nonBaseMgr1.getEntityMasterManager(ROOT), nonBaseMgr2.getEntityMasterManager(ROOT));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create master mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(ROOT);

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", newBaseMgr.getEntityMasterManager(ROOT));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_and_intelligent_discard_and_save_works_fine_for_NON_BASE_user() {
	// create mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(ROOT);
	baseMgr.getEntityMasterManager(ROOT).refreshLocatorManager(ROOT, property);
	baseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(ROOT).acceptLocatorManager(ROOT, property);
	baseMgr.saveEntityMasterManager(ROOT);

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should reset current master manager (because it was initialised from base configuration, not own)
	nonBaseMgr.discardEntityMasterManager(ROOT);
	assertNull("Should be resetted to empty! Intelligent discard operation.", nonBaseMgr.getEntityMasterManager(ROOT));
	// check init methods again for user USER2
	nonBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// modify & save a current instance of master manager
	nonBaseMgr.getEntityMasterManager(ROOT).refreshLocatorManager(ROOT, property);
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(ROOT).acceptLocatorManager(ROOT, property);
	nonBaseMgr.saveEntityMasterManager(ROOT);
	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(ROOT));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should do nothing with current instance of master manager, due to lack of connection with base configuration
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).isRunAutomatically());
	newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).setRunAutomatically(false);

	newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getEnhancer().addCalculatedProperty(EntityWithStringKeyType.class, "", "2 * integerProp", "New calc prop", "Double integer prop", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getEnhancer().apply();
	assertNotNull("Should be not null.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).isRunAutomatically());
	newNonBaseMgr.discardEntityMasterManager(ROOT);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(ROOT));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	try {
	    newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp");
	    fail("The calc prop should not be acceptable after discard operation.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).isRunAutomatically());
    }

    @Test
    public void test_that_not_initialised_MASTER_discarding_saving_isChangeding_fails() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();

	try {
	    nonBaseMgr.discardEntityMasterManager(ROOT);
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveEntityMasterManager(ROOT);
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
//	try {
//	    nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class);
//	    fail("Should be initialised before.");
//	} catch (final IllegalArgumentException e) {
//	}
    }

//    @Test
//    public void test_that_MASTER_isChanged_works_fine_after_modification_saving_discarding() {
//	// create master for USER2
//	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
//	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).initLocatorManagerByDefault(MasterEntity.class, property);
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, property);
//	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
//
//	assertFalse("Should not be changed after 'save'.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
//	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
//	assertFalse("Should not be changed after save.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
//	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.discardEntityMasterManager(MasterEntity.class);
//	assertFalse("Should not be changed after discard.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//    }

    @Test
    public void test_that_MASTER_reloading_works_fine() {
	// create mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(ROOT);
	baseMgr.getEntityMasterManager(ROOT).refreshLocatorManager(ROOT, property);
	baseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(ROOT).acceptLocatorManager(ROOT, property);
	baseMgr.saveEntityMasterManager(ROOT);
	// prepare a test by saving own configuration for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(ROOT);
	nonBaseMgr.getEntityMasterManager(ROOT).refreshLocatorManager(ROOT, property);
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(ROOT).acceptLocatorManager(ROOT, property);
	nonBaseMgr.saveEntityMasterManager(ROOT);

	// modify and reload instantly (OWN configuration)
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManager(ROOT);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));

	// modify and reload instantly (BASE configuration)
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManagerByDefault(ROOT);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("The state is incorrect.", nonBaseMgr.getEntityMasterManager(ROOT).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
    }
}