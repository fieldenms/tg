package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.ArrayList;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForGlobalDomainTree;
import ua.com.fielden.platform.domaintree.testing.MiMasterEntityForGlobalDomainTree;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManagerTest extends GlobalDomainTreeRepresentationTest {
    private static final Class<?> ROOT = MasterEntityForGlobalDomainTree.class;
    private static final Class<?> MENU_ITEM_TYPE = MiMasterEntityForGlobalDomainTree.class;
    private final String NON_BASE_USERS_SAVE_AS = "NON_BASE_USER'S_SAVE_AS";
    private final String property = "simpleEntityProp";

    private IGlobalDomainTreeManager initGlobalManagerWithEntityCentre() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MENU_ITEM_TYPE, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, NON_BASE_USERS_SAVE_AS);
	return managerForNonBaseUser;
    }

    private ILocatorManager prepareGlobalManagerEntityCentreForLocatorActions(final IGlobalDomainTreeManager managerForNonBaseUser) {
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MENU_ITEM_TYPE, NON_BASE_USERS_SAVE_AS);
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
	    managerForNonBaseUser.saveEntityCentreManager(MENU_ITEM_TYPE, NON_BASE_USERS_SAVE_AS);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(ROOT, property);
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MENU_ITEM_TYPE, NON_BASE_USERS_SAVE_AS);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MENU_ITEM_TYPE, NON_BASE_USERS_SAVE_AS);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	final ILocatorManager locatorManager = managerForNonBaseUser.getMasterDomainTreeManager(MENU_ITEM_TYPE);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(managerForNonBaseUser, property, locatorManager);

	// should be accepted before saving
	try {
	    managerForNonBaseUser.saveMasterDomainTreeManager(MENU_ITEM_TYPE);
	    fail("The save operation should be performed for fully accepted instance of entity-centre manager.");
	} catch (final IllegalArgumentException e) {
	}
	locatorManager.acceptLocatorManager(ROOT, property);
	// save a box with locators
	managerForNonBaseUser.saveMasterDomainTreeManager(MENU_ITEM_TYPE);

	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);
	assertNull("Should be null.", locatorManager.getLocatorManager(ROOT, property));
	// save a box with locators
	managerForNonBaseUser.saveMasterDomainTreeManager(MENU_ITEM_TYPE);
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
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", baseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
    }

    @Test
    public void test_that_PRINCIPLE_CENTRE_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", nonBaseMgr1.getEntityCentreManager(MENU_ITEM_TYPE, null));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", nonBaseMgr2.getEntityCentreManager(MENU_ITEM_TYPE, null));
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	try {
	    baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_initialisation_fails_for_NON_BASE_user_if_does_not_exist_in_db() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	try {
	    nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "NON_PRINCIPLE_CENTRE_THAT_DOES_NOT_EXIST");
	    fail("Should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create PRINCIPLE and BASE SAVE AS REPORT for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "BASE SAVE AS REPORT");

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
	newBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT"));
    }

    @Test
    public void test_that_CENTRE_retrieval_and_initialisation_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and BASE/NON-BASE SAVE AS REPORTS for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); }}, baseMgr.entityCentreNames(MENU_ITEM_TYPE));
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT", "NON-BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, nonBaseMgr.entityCentreNames(MENU_ITEM_TYPE));

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
	newNonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT"));
	newNonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "NON-BASE SAVE AS REPORT");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "NON-BASE SAVE AS REPORT"));
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, newNonBaseMgr.entityCentreNames(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_principal_CENTRE_automatic_saving_works_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);

	final QueryExecutionModel<EntityCentreConfig, EntityResultQueryModel<EntityCentreConfig>> model = from(GlobalDomainTreeManager.modelForCurrentAndBaseUsers(MENU_ITEM_TYPE.getName(), GlobalDomainTreeManager.title(MENU_ITEM_TYPE, null), baseMgr.getUserProvider().getUser())).with(fetchOnly(EntityCentreConfig.class).with("principal")).model();
	final EntityCentreConfig centre = getInstance(IEntityCentreConfigController.class).getEntity(model);
	assertTrue("Initialised automatically entity centre should be principle (even for non-base user that invoked).", centre.isPrincipal());
    }

    @Test
    public void test_that_principal_CENTRE_automatic_saving_works_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);

	final QueryExecutionModel<EntityCentreConfig, EntityResultQueryModel<EntityCentreConfig>> model = from(GlobalDomainTreeManager.modelForCurrentAndBaseUsers(MENU_ITEM_TYPE.getName(), GlobalDomainTreeManager.title(MENU_ITEM_TYPE, null), nonBaseMgr.getUserProvider().getUser())).with(fetchOnly(EntityCentreConfig.class).with("principal")).model();
	final EntityCentreConfig centre = getInstance(IEntityCentreConfigController.class).getEntity(model);
	assertTrue("Initialised automatically entity centre should be principle (even for non-base user that invoked).", centre.isPrincipal());
    }

    @Test
    public void test_that_entityCentreNames_works_for_not_initialised_centres() {
	// create PRINCIPLE and BASE/NON-BASE SAVE AS REPORTS for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); }}, baseMgr.entityCentreNames(MENU_ITEM_TYPE));
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT", "NON-BASE SAVE AS REPORT");
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, nonBaseMgr.entityCentreNames(MENU_ITEM_TYPE));

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	assertNull("Should be not initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null));
	assertNull("Should be not initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "BASE SAVE AS REPORT"));
	assertNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "NON-BASE SAVE AS REPORT"));
	assertEquals("Incorrect names of entity-centres.", new ArrayList<String>() {{ add(null); add("BASE SAVE AS REPORT"); add("NON-BASE SAVE AS REPORT"); }}, newNonBaseMgr.entityCentreNames(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_CENTRE_WITH_SAME_TITLE_retrieval_and_initialisation_and_removal_works_fine_for_NON_BASE_and_BASE_user_and_hides_base_CENTRE_for_NON_BASE_user() {
	// create PRINCIPLE and SAME report for USER2 and SAME report for user USER1
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "SAME");
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).getFirstTick().check(ROOT, "integerProp", false);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "SAME");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));

	final IGlobalDomainTreeManager newBaseMgr = createManagerForBaseUser();
	newBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "SAME");
	assertNotNull("Should be initialised.", newBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME"));
	assertFalse("The state is incorrect.", newBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));

	// remove "SAME" for USER2
	newNonBaseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, "SAME");
	assertNull("Should be removed.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME"));
	newNonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "SAME");
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "SAME").getFirstTick().isChecked(ROOT, "integerProp"));
    }

    @Test
    public void test_that_not_initialised_CENTRE_discarding_saving_saveAsing_isChangeding_removing_fails() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	try {
	    newNonBaseMgr.discardEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "REPORT", "NEW-REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    newNonBaseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_isChanged_works_fine_after_modification_saving_discarding() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	assertFalse("Should not be changed after saveAs.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "integerProp", false);
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("Should not be changed after save.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	assertTrue("Should be modified.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	nonBaseMgr.discardEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("Should not be changed after discard.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should NOT be modified.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
    }

    @Test
    public void test_that_CENTRE_freezing_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	assertFalse("Should not be changed.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "integerProp", false);

	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should be unchecked after modification.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should not be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	ICentreDomainTreeManagerAndEnhancer currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT");

	// FREEEEEEEZEEEEEE all current changes
	nonBaseMgr.freezeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");

	assertFalse("The current mgr after freezing should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after freezing should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").equals(currentMgrBeforeFreeze));

	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	////////////////////// Not permitted tasks after report has been freezed //////////////////////
	try {
	    nonBaseMgr.freezeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Double freezing is not permitted. Please do you job -- save/discard and freeze again if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Init action is not permitted while report is freezed. Please do you job -- save/discard and Init it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "REPORT", "NEW_REPORT_TITLE");
	    fail("Saving As is not permitted while report is freezed. Please do you job -- save/discard and SaveAs if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	    fail("Removing is not permitted while report is freezed. Please do you job -- save/discard and remove it if you need!");
	} catch (final IllegalArgumentException e) {
	}

	// change smth.
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertTrue("Should be changed after discard after freezing (due to existence of before-freezing changes).", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should be unchecked after discard after freezing (according to before-freezing changes).", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	// FREEEEEEEZEEEEEE all current changes (again)
	nonBaseMgr.freezeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("Should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be unchecked after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	// change smth.
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "integerProp", true);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "bigDecimalProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	currentMgrBeforeFreeze = nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT");

	// save (precisely "apply") after-freezing changes
	nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "REPORT");

	assertFalse("The current mgr after 'acceptance unfreezing' should not be identical to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT") == currentMgrBeforeFreeze);
	assertTrue("The current mgr after 'acceptance unfreezing' should be equal to currentMgrBeforeFreeze.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").equals(currentMgrBeforeFreeze));

	assertTrue("Should be changed after applying.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "bigDecimalProp"));
	assertTrue("Should be checked after applying.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().isChecked(ROOT, "integerProp"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	// return to the original version of the manager and check if it really is not changed
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getFirstTick().check(ROOT, "bigDecimalProp", false);

	assertFalse("Should not be changed after returning to original version.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertFalse("Should be NOT freezed.", nonBaseMgr.isFreezedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_changed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after initialisation.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'changed' after initialisation.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("The CENTRE should be 'unchanged' after unfreezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should remain 'not null' after unfreezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'changed' after unfreezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_freezing_unfreezing_works_fine_with_freezed_analysis_inside() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// init analysis => it should become 'changed' within CENTRE
	final String name = "A brand new PIVOT analysis";
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").acceptAnalysisManager(name);
	nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("The CENTRE should be 'unchanged'.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name).setVisible(false);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").freezeAnalysisManager(name);
	assertTrue("The CENTRE should be 'changed' due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null'.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should be 'freezed' after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should be 'unchanged' after freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));

	// FREEEEEEEZEEEEEE CENTRE
	nonBaseMgr.freezeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertFalse("The CENTRE should not be changed after freezing.", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE freezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));

	// discard after-freezing changes
	nonBaseMgr.discardEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertTrue("The CENTRE should be 'changed' after unfreezing due to freezed analysis (it copies current analysis to persistent and now the CENTRE ischanged).", nonBaseMgr.isChangedEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));
	assertNotNull("The EMBEDDED ANALYSIS should be 'not null' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").getAnalysisManager(name));
	assertTrue("The EMBEDDED ANALYSIS should remain 'freezed' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isFreezedAnalysisManager(name));
	assertFalse("The EMBEDDED ANALYSIS should remain 'unchanged' after CENTRE unfreezing.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isChangedAnalysisManager(name));
    }

    @Test
    public void test_that_CENTRE_reloading_works_fine() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, null).setRunAutomatically(true);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// modify and reload instantly
	nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").setRunAutomatically(false);
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertTrue("The state is incorrect after reloading.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT").isRunAutomatically());
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_NON_BASE_user() {
	// create PRINCIPLE and REPORT report for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// remove report
	nonBaseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertNull("Should be removed.", nonBaseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	try {
	    nonBaseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, null);
	    fail("Removing of not own reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_removing_works_fine_for_BASE_user() {
	// create PRINCIPLE and REPORT report for USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "REPORT");

	// remove report
	baseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, "REPORT");
	assertNull("Should be removed.", baseMgr.getEntityCentreManager(MENU_ITEM_TYPE, "REPORT"));

	try {
	    baseMgr.removeEntityCentreManager(MENU_ITEM_TYPE, null);
	    fail("Removing of principle reports should fail.");
	} catch (final IllegalArgumentException e) {
	}
    }

    @Test
    public void test_that_CENTRE_saving_works_fine_for_own_reports_and_fails_for_not_own_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "BASE");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE", "BASE-DERIVED");

	baseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "BASE");

	try {
	    nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, null);
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "BASE");
	    fail("It is not own report. Should not be able to save.");
	} catch (final IllegalArgumentException e) {
	}
	nonBaseMgr.saveEntityCentreManager(MENU_ITEM_TYPE, "BASE-DERIVED");
    }

    @Test
    public void test_that_CENTRE_savingAs_works_fine_for_all_visible_reports() {
	// create PRINCIPLE and BASE/NON-BASE reports for user USER2
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "BASE");
	baseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE", "BASE-DERIVED-1");
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, null);
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE");
	nonBaseMgr.initEntityCentreManager(MENU_ITEM_TYPE, "BASE-DERIVED-1");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, null, "PRINCIPLE-DERIVED");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE", "BASE-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "BASE-DERIVED-1", "BASE-DERIVED-1-DERIVED-2");
	nonBaseMgr.saveAsEntityCentreManager(MENU_ITEM_TYPE, "PRINCIPLE-DERIVED", "PRINCIPLE-DERIVED-DERIVED-1");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-MASTERS MANAGEMENT //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_its_NON_BASE_user() {
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));

	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertEquals("Should be equal.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE), baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_MASTER_initialisation_and_uploading_works_fine_for_NON_BASE_user_and_then_retrieval_and_initialisation_works_fine_for_other_NON_BASE_user_in_the_same_hierarchy() {
	final IGlobalDomainTreeManager nonBaseMgr1 = createManagerForNonBaseUser();
	nonBaseMgr1.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr1.getMasterDomainTreeManager(MENU_ITEM_TYPE));

	final IGlobalDomainTreeManager nonBaseMgr2 = createManagerForNonBaseUser2();
	nonBaseMgr2.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr2.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertEquals("Should be equal.", nonBaseMgr1.getMasterDomainTreeManager(MENU_ITEM_TYPE), nonBaseMgr2.getMasterDomainTreeManager(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_works_fine_for_BASE_user() {
	// create master mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);

	// check init methods for another instance of application for user USER1
	final IGlobalDomainTreeManager newBaseMgr = createManagerForNonBaseUser();
	newBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", newBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
    }

    @Test
    public void test_that_MASTER_retrieval_and_initialisation_and_intelligent_discard_and_save_works_fine_for_NON_BASE_user() {
	// create mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).refreshLocatorManager(ROOT, property);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).acceptLocatorManager(ROOT, property);
	baseMgr.saveMasterDomainTreeManager(MENU_ITEM_TYPE);

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should reset current master manager (because it was initialised from base configuration, not own)
	nonBaseMgr.discardMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNull("Should be resetted to empty! Intelligent discard operation.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	// check init methods again for user USER2
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertNotNull("Should be initialised.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// modify & save a current instance of master manager
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).refreshLocatorManager(ROOT, property);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).acceptLocatorManager(ROOT, property);
	nonBaseMgr.saveMasterDomainTreeManager(MENU_ITEM_TYPE);
	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertNotNull("Should be initialised.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should do nothing with current instance of master manager, due to lack of connection with base configuration
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).isRunAutomatically());
	newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).setRunAutomatically(false);

	newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getEnhancer().addCalculatedProperty(EntityWithStringKeyType.class, "", "2 * integerProp", "New calc prop", "Double integer prop", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getEnhancer().apply();
	assertNotNull("Should be not null.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).isRunAutomatically());
	newNonBaseMgr.discardMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertNotNull("Should be initialised.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE));
	assertNotNull("Should be initialised.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property));
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	try {
	    newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp");
	    fail("The calc prop should not be acceptable after discard operation.");
	} catch (final IncorrectCalcPropertyException e) {
	}
	assertTrue("The state is incorrect.", newNonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).isRunAutomatically());
    }

    @Test
    public void test_that_not_initialised_MASTER_discarding_saving_isChangeding_fails() {
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();

	try {
	    nonBaseMgr.discardMasterDomainTreeManager(MENU_ITEM_TYPE);
	    fail("Should be initialised before.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    nonBaseMgr.saveMasterDomainTreeManager(MENU_ITEM_TYPE);
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
	baseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).refreshLocatorManager(ROOT, property);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).acceptLocatorManager(ROOT, property);
	baseMgr.saveMasterDomainTreeManager(MENU_ITEM_TYPE);
	// prepare a test by saving own configuration for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).refreshLocatorManager(ROOT, property);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).acceptLocatorManager(ROOT, property);
	nonBaseMgr.saveMasterDomainTreeManager(MENU_ITEM_TYPE);

	// modify and reload instantly (OWN configuration)
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initMasterDomainTreeManager(MENU_ITEM_TYPE);
	assertTrue("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));

	// modify and reload instantly (BASE configuration)
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initMasterDomainTreeManagerByDefault(MENU_ITEM_TYPE);
	assertTrue("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("The state is incorrect.", nonBaseMgr.getMasterDomainTreeManager(MENU_ITEM_TYPE).getLocatorManager(ROOT, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
    }
}