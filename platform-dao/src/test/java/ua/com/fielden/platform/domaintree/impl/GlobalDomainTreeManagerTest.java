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
import ua.com.fielden.platform.domaintree.ILocatorManager.ILocatorManagerInner;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ENTITY-CENTRES/MASTERS LOCATORS MANAGEMENT /////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_CENTRE_saving_works_fine_with_just_initialised_LOCATORS_from_raw_construction() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);

	final ILocatorManagerInner locatorManager = (ILocatorManagerInner) dtm.getFirstTick();

	test_that_saving_works_fine_with_just_initialised_LOCATORS_from_raw_construction(property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);

	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_LOCATORS_from_raw_construction() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final IMasterDomainTreeManager mdtm = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);
	final String property = "entityProp.simpleEntityProp";

	final ILocatorManagerInner locatorManager = (ILocatorManagerInner) mdtm;

	test_that_saving_works_fine_with_just_initialised_LOCATORS_from_raw_construction(property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);

	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    private void test_that_saving_works_fine_with_just_initialised_LOCATORS_from_raw_construction(final String property, final ILocatorManagerInner locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION (from raw construction) /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	final ILocatorDomainTreeManagerAndEnhancer inst1 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNotNull("Should be not null after initialisation.", inst1);
	final ILocatorDomainTreeManagerAndEnhancer inst1FromProduceMethod = locatorManager.produceLocatorManagerByDefault(MasterEntity.class, property);
	assertFalse("The current instance should not be identical to instance from 'produce' method.", inst1 == inst1FromProduceMethod);
	assertTrue("The current instance should be 'equal' to instance from 'produce' method.", inst1.equals(inst1FromProduceMethod));
	assertFalse("The instance should not be 'changed' after initialisation (the user did nothing with initialised instance!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	// re-initialise a locator manager again -- should work as "reloading"
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	final ILocatorDomainTreeManagerAndEnhancer inst2 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNotNull("Should be not null after second initialisation (many initialisations are permitted, they should work as 'reloading').", inst2);
	final ILocatorDomainTreeManagerAndEnhancer inst2FromProduceMethod = locatorManager.produceLocatorManagerByDefault(MasterEntity.class, property);
	assertFalse("The current instance should not be identical to instance from 'produce' method (many initialisations are permitted, they should work as 'reloading').", inst2 == inst2FromProduceMethod);
	assertTrue("The current instance should be 'equal' to instance from 'produce' method (many initialisations are permitted, they should work as 'reloading').", inst2.equals(inst2FromProduceMethod));
	assertFalse("The instance should not be 'changed' after initialisation (the user did nothing with initialised instance!) (many initialisations are permitted, they should work as 'reloading').", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    @Test
    public void test_that_CENTRE_saving_works_fine_with_just_initialised_LOCATORS_from_default_config() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);

	final ILocatorManagerInner locatorManager = (ILocatorManagerInner) dtm.getFirstTick();

	test_that_saving_works_fine_with_just_initialised_LOCATORS_from_default_config(managerForNonBaseUser, property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);

	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_LOCATORS_from_default_config() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final IMasterDomainTreeManager mdtm = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);
	final String property = "entityProp.simpleEntityProp";

	final ILocatorManagerInner locatorManager = (ILocatorManagerInner) mdtm;

	test_that_saving_works_fine_with_just_initialised_LOCATORS_from_default_config(managerForNonBaseUser, property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);

	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    private void test_that_saving_works_fine_with_just_initialised_LOCATORS_from_default_config(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManagerInner locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION (from default configuration) ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	final ILocatorDomainTreeManagerAndEnhancer ldtmae = initDefaultLocatorForSomeTestType(managerForNonBaseUser);

	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));

	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	final ILocatorDomainTreeManagerAndEnhancer inst1 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNotNull("Should be not null after initialisation.", inst1);
	final ILocatorDomainTreeManagerAndEnhancer inst1FromProduceMethod = locatorManager.produceLocatorManagerByDefault(MasterEntity.class, property);
	assertFalse("The default instance should not be identical to instance from 'produce' method.", ldtmae == inst1FromProduceMethod);
	assertTrue("The default instance should be 'equal' to instance from 'produce' method.", ldtmae.equals(inst1FromProduceMethod));
	assertFalse("The current instance should not be identical to instance from 'produce' method.", inst1 == inst1FromProduceMethod);
	assertTrue("The current instance should be 'equal' to instance from 'produce' method.", inst1.equals(inst1FromProduceMethod));
	assertFalse("The instance should not be 'changed' after initialisation (the user did nothing with initialised instance!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	// re-initialise a locator manager again -- should work as "reloading"
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	final ILocatorDomainTreeManagerAndEnhancer inst2 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNotNull("Should be not null after second initialisation (many initialisations are permitted, they should work as 'reloading').", inst2);
	final ILocatorDomainTreeManagerAndEnhancer inst2FromProduceMethod = locatorManager.produceLocatorManagerByDefault(MasterEntity.class, property);
	assertFalse("The default instance should not be identical to instance from 'produce' method.", ldtmae == inst2FromProduceMethod);
	assertTrue("The default instance should be 'equal' to instance from 'produce' method.", ldtmae.equals(inst2FromProduceMethod));
	assertFalse("The current instance should not be identical to instance from 'produce' method (many initialisations are permitted, they should work as 'reloading').", inst2 == inst2FromProduceMethod);
	assertTrue("The current instance should be 'equal' to instance from 'produce' method (many initialisations are permitted, they should work as 'reloading').", inst2.equals(inst2FromProduceMethod));
	assertFalse("The instance should not be 'changed' after initialisation (the user did nothing with initialised instance!) (many initialisations are permitted, they should work as 'reloading').", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    @Test
    public void test_that_CENTRE_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);

	final IAddToCriteriaTickManager locatorManager = dtm.getFirstTick();
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

	ILocatorDomainTreeManagerAndEnhancer inst3;
	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);

	inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    @Test
    public void test_that_MASTER_saving_works_fine_with_just_initialised_and_modified_LOCATORS() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final IMasterDomainTreeManager mdtm = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);
	final String property = "entityProp.simpleEntityProp";

	final ILocatorManager locatorManager = mdtm;

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

	ILocatorDomainTreeManagerAndEnhancer inst3;
	test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(property, locatorManager);

	// save a box with locators
	managerForNonBaseUser.saveEntityMasterManager(MasterEntity.class);

	inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNull("Should be null after inteligent 'save' operation. It means that 'save' operation resets some instances to 'null' -- those instances that are fully equal to 'default' instances from 'produce' method.", inst3);
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART2(final String property, final ILocatorManager locatorManager) {
	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertNotNull("Should be not null after inteligent 'save' operation. It means that 'save' operation should not reset the instance to 'null' -- the instance is not equal to 'default' instances from 'produce' method.", inst3);

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION AS RELOADING ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	assertFalse("The instance should not be 'changed' after initialisation (the user did nothing!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    private void test_that_saving_works_fine_with_just_initialised_and_modified_LOCATORS_PART1(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManager locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION & MODIFICATION //////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	initDefaultLocatorForSomeTestType(managerForNonBaseUser);

	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));

	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	// MODIFY
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	assertTrue("The instance should be 'changed' after initialisation & modification (the user has checked some property!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    @Test
    public void test_that_CENTRE_LOCATORS_discarding_works_fine() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);

	final ILocatorManager locatorManager = dtm.getFirstTick();

	test_that_LOCATORS_discarding_works_fine(managerForNonBaseUser, property, locatorManager);
    }

    @Test
    public void test_that_MASTER_LOCATORS_discarding_works_fine() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final IMasterDomainTreeManager mdtm = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);
	final String property = "entityProp.simpleEntityProp";

	final ILocatorManager locatorManager = mdtm;

	test_that_LOCATORS_discarding_works_fine(managerForNonBaseUser, property, locatorManager);
    }

    private void test_that_LOCATORS_discarding_works_fine(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManager locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION & MODIFICATION //////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	initDefaultLocatorForSomeTestType(managerForNonBaseUser);

	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));

	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	// MODIFY
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	assertTrue("The instance should be 'changed' after initialisation & modification (the user has checked some property!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));

	locatorManager.discardLocatorManager(MasterEntity.class, property);

	final ILocatorDomainTreeManagerAndEnhancer inst3 = locatorManager.getLocatorManager(MasterEntity.class, property);
	assertFalse("The state should be discarded.", inst3.getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("The instance should be 'unchanged' after discard operation.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
    }

    @Test
    public void test_that_CENTRE_LOCATORS_global_saving_works_fine() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);

	final ILocatorManager locatorManager = dtm.getFirstTick();

	test_that_LOCATORS_global_saving_works_fine(managerForNonBaseUser, property, locatorManager);
    }

    @Test
    public void test_that_MASTER_LOCATORS_global_saving_works_fine() {
	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityMasterManager(MasterEntity.class);
	final IMasterDomainTreeManager mdtm = managerForNonBaseUser.getEntityMasterManager(MasterEntity.class);
	final String property = "entityProp.simpleEntityProp";

	final ILocatorManager locatorManager = mdtm;

	test_that_LOCATORS_global_saving_works_fine(managerForNonBaseUser, property, locatorManager);
    }

    private void test_that_LOCATORS_global_saving_works_fine(final IGlobalDomainTreeManager managerForNonBaseUser, final String property, final ILocatorManager locatorManager) {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION & MODIFICATION //////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));

	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	// MODIFY
	final ILocatorDomainTreeManagerAndEnhancer inst1 = locatorManager.getLocatorManager(MasterEntity.class, property);
	inst1.getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	assertTrue("The instance should be 'changed' after initialisation & modification (the user has checked some property!).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));

	// save a locator globally
	locatorManager.saveLocatorManagerGlobally(MasterEntity.class, property);

	assertFalse("Should not be identical.", inst1 == managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class));
	assertTrue("Should be equal.", inst1.equals(managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class)));
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

	// FREEEEEEEZEEEEEE all current changes
	nonBaseMgr.freezeEntityCentreManager(MasterEntity.class, "REPORT");
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
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
	nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().check(MasterEntity.class, "integerProp", true);
	assertTrue("Should be changed after modification after freezing.", nonBaseMgr.isChangedEntityCentreManager(MasterEntity.class, "REPORT"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "bigDecimalProp"));
	assertTrue("Should be checked after modification after freezing.", nonBaseMgr.getEntityCentreManager(MasterEntity.class, "REPORT").getFirstTick().isChecked(MasterEntity.class, "integerProp"));
	assertTrue("Should be freezed.", nonBaseMgr.isFreezedEntityCentreManager(MasterEntity.class, "REPORT"));

	// save (precisely "apply") after-freezing changes
	nonBaseMgr.saveEntityCentreManager(MasterEntity.class, "REPORT");
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
    public void test_that_CENTRE_LOCATORS_freezing_works_fine() {
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// INITIALISATION /////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	final IGlobalDomainTreeManager managerForNonBaseUser = createManagerForNonBaseUser();
	managerForNonBaseUser.initEntityCentreManager(MasterEntity.class, null);
	managerForNonBaseUser.saveAsEntityCentreManager(MasterEntity.class, null, NON_BASE_USERS_SAVE_AS);
	final ICentreDomainTreeManagerAndEnhancer dtm = managerForNonBaseUser.getEntityCentreManager(MasterEntity.class, NON_BASE_USERS_SAVE_AS);
	final String property = "entityProp.simpleEntityProp";
	dtm.getFirstTick().check(MasterEntity.class, property, true);
	final ILocatorManagerInner locatorManager = (ILocatorManagerInner) dtm.getFirstTick();

	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	initDefaultLocatorForSomeTestType(managerForNonBaseUser);
	assertNull("Should be null before creation.", locatorManager.getLocatorManager(MasterEntity.class, property));
	// initialise a brand new instance of locator
	locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "bigDecimalProp", true);
	locatorManager.acceptLocatorManager(MasterEntity.class, property);

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////// FREEEEEEEEEEEEEEEZZZZZZZZZZZZZZZZEEEEEEEEEEEEEEEEEEEEEEEE//////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	assertFalse("Should not be changed.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);

	assertTrue("Should be changed after modification.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertTrue("Should be checked after modification.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("Should be NOT freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// FREEEEEEEZEEEEEE all current changes
	locatorManager.freezeLocatorManager(MasterEntity.class, property);
	assertFalse("Should not be changed after freezing.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertTrue("Should be checked after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("Should be freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	////////////////////// Not permitted tasks after report has been freezed //////////////////////
	try {
	    locatorManager.freezeLocatorManager(MasterEntity.class, property);
	    fail("Double freezing is not permitted. Please do you job -- save/discard and freeze again if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    locatorManager.initLocatorManagerByDefault(MasterEntity.class, property);
	    fail("Init action is not permitted while report is freezed. Please do you job -- save/discard and Init it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    locatorManager.produceLocatorManagerByDefault(MasterEntity.class, property);
	    fail("Locator producing is not permitted. Please do you job -- save/discard and then produce new stuff if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    locatorManager.resetLocatorManager(MasterEntity.class, property);
	    fail("Reset action is not permitted while report is freezed. Please do you job -- save/discard and then Reset it if you need!");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    locatorManager.saveLocatorManagerGlobally(MasterEntity.class, property);
	    fail("Saving Globally is not permitted while report is freezed. Please do you job -- save/discard and then Save it Globally if you need!");
	} catch (final IllegalArgumentException e) {
	}

	// change smth.
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	assertTrue("Should be changed after modification after freezing.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertFalse("Should be unchecked after modification after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("Should be freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// discard after-freezing changes
	locatorManager.discardLocatorManager(MasterEntity.class, property);
	assertTrue("Should be changed after discard after freezing (due to existence of before-freezing changes).", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertTrue("Should be checked after discard after freezing (according to before-freezing changes).", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("Should be NOT freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// FREEEEEEEZEEEEEE all current changes (again)
	locatorManager.freezeLocatorManager(MasterEntity.class, property);
	assertFalse("Should not be changed after freezing.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertTrue("Should be checked after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("Should be checked after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "bigDecimalProp"));
	assertTrue("Should be freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// change smth.
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "bigDecimalProp", false);
	assertTrue("Should be changed after modification after freezing.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertFalse("Should be unchecked after modification after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("Should be unchecked after modification after freezing.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "bigDecimalProp"));
	assertTrue("Should be freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// save (precisely "apply") after-freezing changes
	locatorManager.acceptLocatorManager(MasterEntity.class, property);
	assertTrue("Should be changed after applying.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
	assertFalse("Should be unchecked after applying.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("Should be unchecked after applying.", locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().isChecked(EntityWithStringKeyType.class, "bigDecimalProp"));
	assertFalse("Should be NOT freezed.", locatorManager.isFreezedLocatorManager(MasterEntity.class, property));

	// return to the original version of the manager and check if it really is not changed
	locatorManager.getLocatorManager(MasterEntity.class, property).getSecondTick().check(EntityWithStringKeyType.class, "bigDecimalProp", true);

	assertFalse("Should not be changed after returning to original version.", locatorManager.isChangedLocatorManager(MasterEntity.class, property));
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
	baseMgr.getEntityMasterManager(MasterEntity.class).initLocatorManagerByDefault(MasterEntity.class, "entityProp.simpleEntityProp");
	baseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp");
	baseMgr.saveEntityMasterManager(MasterEntity.class);

	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should reset current master manager (because it was initialised from base configuration, not own)
	nonBaseMgr.discardEntityMasterManager(MasterEntity.class);
	assertNull("Should be resetted to empty! Intelligent discard operation.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	// check init methods again for user USER2
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// modify & save a current instance of master manager
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp");
	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
	// check init methods for another instance of application for user USER2
	final IGlobalDomainTreeManager newNonBaseMgr = createManagerForNonBaseUser();
	newNonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	// discard action which should do nothing with current instance of master manager, due to lack of connection with base configuration
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").isRunAutomatically());
	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").setRunAutomatically(false);

	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getEnhancer().addCalculatedProperty(EntityWithStringKeyType.class, "", "2 * integerProp", "New calc prop", "Double integer prop", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getEnhancer().apply();
	assertNotNull("Should be not null.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp"));
	assertFalse("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").isRunAutomatically());
	newNonBaseMgr.discardEntityMasterManager(MasterEntity.class);
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class));
	assertNotNull("Should be initialised.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	try {
	    newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getEnhancer().getCalculatedProperty(EntityWithStringKeyType.class, "newCalcProp");
	    fail("The calc prop should not be acceptable after discard operation.");
	} catch (final IncorrectCalcPropertyKeyException e) {
	}
	assertTrue("The state is incorrect.", newNonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").isRunAutomatically());
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
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).initLocatorManagerByDefault(MasterEntity.class, "entityProp.simpleEntityProp");
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp");
//	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
//
//	assertFalse("Should not be changed after 'save'.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
//	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);
//	assertFalse("Should not be changed after save.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
//	assertTrue("Should be changed after modification.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//	nonBaseMgr.discardEntityMasterManager(MasterEntity.class);
//	assertFalse("Should not be changed after discard.", nonBaseMgr.isChangedEntityMasterManager(MasterEntity.class));
//    }

    @Test
    public void test_that_MASTER_reloading_works_fine() {
	// create mgr for user USER1
	final IGlobalDomainTreeManager baseMgr = createManagerForBaseUser();
	baseMgr.initEntityMasterManager(MasterEntity.class);
	baseMgr.getEntityMasterManager(MasterEntity.class).initLocatorManagerByDefault(MasterEntity.class, "entityProp.simpleEntityProp");
	baseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	baseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp");
	baseMgr.saveEntityMasterManager(MasterEntity.class);
	// prepare a test by saving own configuration for USER2
	final IGlobalDomainTreeManager nonBaseMgr = createManagerForNonBaseUser();
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", true);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).acceptLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp");
	nonBaseMgr.saveEntityMasterManager(MasterEntity.class);

	// modify and reload instantly (OWN configuration)
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManager(MasterEntity.class);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));

	// modify and reload instantly (BASE configuration)
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().check(EntityWithStringKeyType.class, "integerProp", false);
	nonBaseMgr.initEntityMasterManagerByDefault(MasterEntity.class);
	assertTrue("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getFirstTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
	assertFalse("The state is incorrect.", nonBaseMgr.getEntityMasterManager(MasterEntity.class).getLocatorManager(MasterEntity.class, "entityProp.simpleEntityProp").getSecondTick().isChecked(EntityWithStringKeyType.class, "integerProp"));
    }
}