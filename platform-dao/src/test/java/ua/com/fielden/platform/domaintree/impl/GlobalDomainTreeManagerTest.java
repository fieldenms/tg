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
import ua.com.fielden.platform.domaintree.testing.MasterEntity;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManagerTest extends GlobalDomainTreeRepresentationTest {
    private final String NON_BASE_USERS_SAVE_AS = "NON_BASE_USER'S_SAVE_AS";
    private final String property = "entityProp.simpleEntityProp";

    private IGlobalDomainTreeManager initGlobalManagerWithEntityCentre() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	return managerForNonBaseUser;
    }

    private ILocatorManager prepareGlobalManagerEntityCentreForLocatorActions(final IGlobalDomainTreeManager managerForNonBaseUser) {
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	dtm.getFirstTick().check(MasterEntity.class, property, true);
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
	    managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(MasterEntity.class, property);
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(MasterEntity.class, property));
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	assertNull("Should be null.", locatorManager.getLocatorManager(MasterEntity.class, property));
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final ILocatorManager locatorManager = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);;

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(managerForNonBaseUser, property, locatorManager);

	// should be accepted before saving
	try {
	    managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(MasterEntity.class, property);
	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(MasterEntity.class, property));
	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);
	assertNull("Should be null.", locatorManager.getLocatorManager(MasterEntity.class, property));
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(final String property, final ILocatorManager locatorManager) {
	assertNotNull("Should be not null.", locatorManager.getLocatorManager(MasterEntity.class, property));

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION AS RELOADING ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a brand new instance of locator
	locatorManager.resetLocatorManagerToDefault(MasterEntity.class, property);
	assertFalse("The instance should not be 'changed'.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManager locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION & MODIFICATION //////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	initDefaultLocatorForSomeTestType(managerForNonBaseUser);

	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));

	// initialise a brand new instance of locator
	locatorManager.refreshLocatorManager(MasterEntity.class, property);
	// MODIFY
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	assertTrue("The instance should be 'changed' after initialisation & modification (the user has checked some property!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-CENTRES MANAGEMENT //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(MasterEntity.class, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(MasterEntity.class, null));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", nonBaseMgr1.getEntityCentreManager(MasterEntity.class, null));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", nonBaseMgr2.getEntityCentreManager(MasterEntity.class, null));
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	try {
	    baseMgr.initEntityCentreManager(MasterEntity.class, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_NON_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	try {
	    nonBaseMgr.initEntityCentreManager(MasterEntity.class, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create PRINCIPLE and BASE SAVE AS REPORT for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "BASE SAVE AS REPORT");

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MasterEntity.class, null));
	newBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT"));
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and BASE/NON-BASE SAVE AS REPORTS for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add(null); add("BASE SAVE AS REPORT"); }}, baseMgr.entityCentreNames(MasterEntity.class));
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT", "NON-BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, nonBaseMgr.entityCentreNames(MasterEntity.class));

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, null));
	newNonBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "BASE SAVE AS REPORT"));
	newNonBaseMgr.initEntityCentreManager(MasterEntity.class, "NON-BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "NON-BASE SAVE AS REPORT"));
	assertEquals("Incorrect names of entity-centres.", new HashSet<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, newNonBaseMgr.entityCentreNames(MasterEntity.class));
    }

    @Test
    public void test_that_CENTRE_WITH_SAME_TITLE_retrieval_and_initialisation_and_removal_works_fine_for_NON_BASE_and_BASE_user_and_hides_base_CENTRE_for_NON_BASE_user() {
	// create PRINCIPLE and SAME report for USER2 and SAME report for user USER1
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "SAME");
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", false);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "SAME");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(MasterEntity.class, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME").getFirstTick().isChecked(MasterEntity.class, "integerProp"));

	final IGlobalDomainTreeManager newBaseMgr = createManagerForBaseUser();
	newBaseMgr.initEntityCentreManager(MasterEntity.class, "SAME");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME"));
	assertFalse("The state is incorrect.", newBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME").getFirstTick().isChecked(MasterEntity.class, "integerProp"));

	// remove "SAME" for USER2
	newNonBaseMgr.removeEntityCentreManager(MasterEntity.class, "SAME");
	assertNull("Should be removed.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME"));
	newNonBaseMgr.initEntityCentreManager(MasterEntity.class, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(MasterEntity.class, "SAME").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
    }

    @Test
    public void test_that_not_initialised_CENTRE_discarding_saving_saveAsing_isChangeding_removing_fails() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	try {
	    newNonBaseMgr.discardEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "REPORT", "NEW-REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.removeEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_isChanged_works_fine_after_modification_saving_discarding() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	assertFalse("Should not be changed after saveAs.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", false);
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("Should not be changed after save.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", true);
	assertTrue("Should be modified.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));

	nonBaseMgr.discardEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("Should not be changed after discard.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
    }

    @Test
    public void test_that_CENTRE_freezing_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	assertFalse("Should not be changed.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", false);

	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should be unchecked after modification.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertFalse("Should not be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	ICentreDomainTreeManagerAndEnhancer currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT");

	// FREEEEEEEZEEEEEE all current changes
	nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");

	assertFalse("The current mgr after freezing should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after freezing should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").equals(currentMgrBeforeFreeze));

	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	////////////////////// Not permitted tasks after report has been freezed //////////////////////
	try {
	    nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Double freezing is not permitted. Please do you job -- save/discard and freeze again if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.initEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Init action is not permitted while report is freezed. Please do you job -- save/discard and Init it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "REPORT", "NEW_REPORT_TITLE");
	    fail("Saving As is not permitted while report is freezed. Please do you job -- save/discard and SaveAs if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.removeEntityCentreManager(MasterEntity.class, "REPORT");
	    fail("Removing is not permitted while report is freezed. Please do you job -- save/discard and remove it if you need!");
	} catch (final IllegalArgumentException e) {
	}

	// change smth.
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MasterEntity.class, "REPORT");
	assertTrue("Should be changed after discard after freezing (due to existence of before-freezing changes).", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should be unchecked after discard after freezing (according to before-freezing changes).", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	// FREEEEEEEZEEEEEE all current changes (again)
	nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "bigDecimalProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	// change smth.
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "bigDecimalProp"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT");

	// save (precisely "apply") after-freezing changes
	nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "REPORT");

	assertFalse("The current mgr after 'acceptance unfreezing' should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after 'acceptance unfreezing' should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").equals(currentMgrBeforeFreeze));

	assertTrue("Should be changed after applying.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "bigDecimalProp"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	// return to the original version of the manager and check if it really is not changed
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "bigDecimalProp", false);

	assertFalse("Should not be changed after returning to original version.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_changed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after initialisation.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'changed' after initialisation.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("The CENTRE should be 'unchanged' after unfreezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should remain 'not null' after unfreezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after unfreezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_freezed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").acceptAnalysisManager(name);
	nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name).setVisible(false);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").freezeAnalysisManager(name);
	assertTrue("The CENTRE should be 'changed' due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null'.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'freezed' after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should be 'unchanged' after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MasterEntity.class, "REPORT");
	assertTrue("The CENTRE should be 'changed' after unfreezing due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_reloading_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, null).getFirstTick().check(MasterEntity.class, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// modify and reload instantly
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", false);
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, "REPORT");
	assertTrue("The state is incorrect after reloading.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// remove report
	nonBaseMgr.removeEntityCentreManager(MasterEntity.class, "REPORT");
	assertNull("Should be removed.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT"));

	try {
	    nonBaseMgr.removeEntityCentreManager(MasterEntity.class, null);
	    fail("Removing of not own reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_BASE_user() {
	// create PRINCIPLE and REPORT report for USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "REPORT");

	// remove report
	baseMgr.removeEntityCentreManager(MasterEntity.class, "REPORT");
	assertNull("Should be removed.", baseMgr.getEntityCentreManager(MasterEntity.class, "REPORT"));

	try {
	    baseMgr.removeEntityCentreManager(MasterEntity.class, null);
	    fail("Removing of principle reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_saving_works_fine_for_own_reports_and_fails_for_not_own_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "BASE");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "BASE", "BASE-DERIVED");

	baseMgr.saveEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveEntityCentreManager(MasterEntity.class, "BASE");

	try {
	    nonBaseMgr.saveEntityCentreManager(MasterEntity.class, null);
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "BASE");
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "BASE-DERIVED");
    }

    @Test
    public void test_that_CENTRE_savingAs_works_fine_for_all_visible_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MasterEntity.class, null);
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "BASE");
	baseMgr.saveAsEntityCentreManager(MasterEntity.class, "BASE", "BASE-DERIVED-1");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, null);
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE");
	nonBaseMgr.initEntityCentreManager(MasterEntity.class, "BASE-DERIVED-1");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, null, "PRINCIPLE-DERIVED");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "BASE", "BASE-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "BASE-DERIVED-1", "BASE-DERIVED-1-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(MasterEntity.class, "PRINCIPLE-DERIVED", "PRINCIPLE-DERIVED-DERIVED-1");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-MASTERS MANAGEMENT //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", baseMgr.getEntityMasterManager(MasterEntity.class));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", baseMgr.getEntityMasterManager(MasterEntity.class));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertEquals("Should be equal.", nonBaseMgr.getEntityMasterManager(MasterEntity.class), baseMgr.getEntityMasterManager(MasterEntity.class));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr1.getEntityMasterManager(MasterEntity.class));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr2.getEntityMasterManager(MasterEntity.class));
	assertEquals("Should be equal.", nonBaseMgr1.getEntityMasterManager(MasterEntity.class), nonBaseMgr2.getEntityMasterManager(MasterEntity.class));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create master mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(MasterEntity.class);

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", newBaseMgr.getEntityMasterManager(MasterEntity.class));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_and_intelligent_discard_and_save_works_fine_for_NON_BASE_user() {
	// create mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(MasterEntity.class);
	baseMgr.getEntityMasterManager(MasterEntity.class).refreshLocatorManager(MasterEntity.class, property);
	baseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, property);
	baseMgr.saveEntityMasterManager(MasterEntity.class);

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should reset current master manager (because it was initialised from base configuration, not own)
	nonBaseMgr.discardEntityMasterManager(MasterEntity.class);
	assertNull("Should be resetted to empty! Intelligent discard operation.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	// check init methods again for user USER2
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// modify & save a current instance of master manager
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).refreshLocatorManager(MasterEntity.class, property);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, property);
	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should do nothing with current instance of master manager, due to lack of connection with base configuration
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).isRunAutomatically());
	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).setRunAutomatically(false);

	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getEnhancer().addCalculatedProperty(EntityWithStringKeyType.class, "", "2 * integerProp", "New calc prop", "Double integer prop", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getEnhancer().apply();
	assertNotNull("Should be not null.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).isRunAutomatically());
	newNonBaseMgr.discardEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	try {
	    newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp");
	    fail("The calc prop should not be acceptable after discard operation.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).isRunAutomatically());
    }

    @Test
    public void test_that_not_initialised_MASTER_discarding_saving_isChangeding_fails() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();

	try {
	    nonBaseMgr.discardEntityMasterManager(MasterEntity.class);
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
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
	baseMgr.initEntityMasterManager(MasterEntity.class);
	baseMgr.getEntityMasterManager(MasterEntity.class).refreshLocatorManager(MasterEntity.class, property);
	baseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, property);
	baseMgr.saveEntityMasterManager(MasterEntity.class);
	// prepare a test by saving own configuration for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).refreshLocatorManager(MasterEntity.class, property);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, property);
	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);

	// modify and reload instantly (OWN configuration)
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));

	// modify and reload instantly (BASE configuration)
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManagerByDefault(MasterEntity.class);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
    }
}