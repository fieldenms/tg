package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManager1;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;

/**
 * A test for {@link AbstractDomainTreeManager}.
 * 
 * @author TG Team
 * 
 */
public class AbstractDomainTreeManagerTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private int i, j;
    
    @Override
    protected IDomainTreeManager dtm() {
        return (IDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(AbstractDomainTreeManagerTest.class);
    }

    protected static Object createDtm_for_AbstractDomainTreeManagerTest() {
        return new DomainTreeManager1(factory(), createRootTypes_for_AbstractDomainTreeManagerTest());
    }

    protected static Object createIrrelevantDtm_for_AbstractDomainTreeManagerTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_AbstractDomainTreeManagerTest() {
        final Set<Class<?>> rootTypes = new HashSet<>(createRootTypes_for_AbstractDomainTreeTest());
        return rootTypes;
    }

    protected static void manageTestingDTM_for_AbstractDomainTreeManagerTest(final Object obj) {
        final IDomainTreeManager dtm = (IDomainTreeManager) obj;

        manageTestingDTM_for_AbstractDomainTreeTest(dtm.getRepresentation());

        dtm.getFirstTick().check(EntityWithStringKeyType.class, "", true);
        dtm.getRepresentation().getFirstTick().disableImmutably(EntityWithStringKeyType.class, "");
        dtm.getSecondTick().check(EntityWithStringKeyType.class, "", true);
        dtm.getRepresentation().getSecondTick().disableImmutably(EntityWithStringKeyType.class, "");
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                dtm.getFirstTick().check(MasterEntity.class, name, true);
                dtm.getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, name);
                dtm.getSecondTick().check(MasterEntity.class, name, true);
                dtm.getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, name);
            }
        }, "checkedManuallyProp");

        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                dtm.getSecondTick().check(MasterEntity.class, name, true);
                dtm.getRepresentation().getSecondTick().disableImmutably(MasterEntity.class, name);
            }
        }, "immutablyCheckedUntouchedProp");

        dtm.getFirstTick().checkedProperties(MasterEntity.class);
        dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    protected static void performAfterDeserialisationProcess_for_AbstractDomainTreeManagerTest(final Object obj) {
    }

    protected static void assertInnerCrossReferences_for_AbstractDomainTreeManagerTest(final Object obj) {
        final AbstractDomainTreeManager dtm = (AbstractDomainTreeManager) obj;
        final AbstractDomainTreeRepresentation dtr = (AbstractDomainTreeRepresentation) dtm.getRepresentation();

        AbstractDomainTreeRepresentationTest.assertInnerCrossReferences_for_AbstractDomainTreeRepresentationTest(dtr);

        final TickManager firstTm = (TickManager) dtm.getFirstTick();
        assertNotNull("Should be not null.", firstTm);
        assertNotNull("Should be not null.", firstTm.tr());
        assertNotNull("Should be not null.", firstTm.dtr());
        assertTrue("Should be identical.", dtr.getFirstTick() == firstTm.tr());
        assertTrue("Should be identical.", dtr == firstTm.dtr());

        final TickManager secondTm = (TickManager) dtm.getSecondTick();
        assertNotNull("Should be not null.", secondTm);
        assertNotNull("Should be not null.", secondTm.tr());
        assertNotNull("Should be not null.", secondTm.dtr());
        assertTrue("Should be identical.", dtr.getSecondTick() == secondTm.tr());
        assertTrue("Should be identical.", dtr == secondTm.dtr());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    protected void checkSomeProps(final IDomainTreeManager dtm) {
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                dtm.getSecondTick().check(MasterEntity.class, name, true);
            }
        }, "checkedUntouchedProp");
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                dtm.getSecondTick().check(MasterEntity.class, name, true);
            }
        }, "mutatedWithFunctionsProp");
    }

    protected void checkIllegally(final String name, final String message) {
        try {
            dtm().getFirstTick().check(MasterEntity.class, name, true);
            fail(message);
        } catch (final DomainTreeException e) {
        }
    }

    protected void isCheck_equals_to_state(final String name, final String message, final boolean state) {
        assertEquals(message, state, dtm().getFirstTick().isChecked(MasterEntity.class, name));
    }

    protected void uncheckLegally(final String name, final String message) {
        try {
            dtm().getFirstTick().check(MasterEntity.class, name, false);
            dtm().getFirstTick().check(MasterEntity.class, name, true);
        } catch (final Exception e) {
            fail(message + " Cause = [" + e.getMessage() + "]");
        }
    }

    protected void checkLegally(final String name, final String message) {
        try {
            dtm().getFirstTick().check(MasterEntity.class, name, true);
            dtm().getFirstTick().check(MasterEntity.class, name, false);
        } catch (final Exception e) {
            fail(message + " Cause = [" + e.getMessage() + "]");
        }
    }

    protected void isCheckIllegally(final String name, final String message) {
        try {
            dtm().getFirstTick().isChecked(MasterEntity.class, name);
            fail(message);
        } catch (final DomainTreeException e) {
        }
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Manage state legitimacy (CHECK) //////
    ////////////////////////////////////////////////////////////////
    @Ignore
    @Test
    public void test_that_CHECK_state_managing_for_excluded_properties_is_not_permitted() {
        final String message = "Excluded property should cause illegal argument exception while changing its state.";
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                checkIllegally(name, message);
            }
        }, "excludedManuallyProp");
    }
    
    @Ignore
    @Test
    public void test_that_CHECK_state_managing_for_disabled_properties_is_not_permitted() { // (disabled == immutably checked or unchecked)
        final String message1 = "Immutably unchecked property (disabled) should cause illegal argument exception while changing its state.";
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                checkIllegally(name, message1);
            }
        }, "disabledManuallyProp");
    }

    @Test
    public void test_that_CHECK_state_managing_for_rest_properties_is_permitted() {
        final String message = "Not disabled and not excluded property should NOT cause any exception while changing its state.";
        allLevelsWithoutCollections(new IAction() {
            @Override
            public void action(final String name) {
                if (!dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name)) {
                    uncheckLegally(name, message);
                }
            }
        }, "mutablyCheckedProp");
    }

    ///////////////////////////////////////////////////////////////////////
    ////////////////////// 2. Ask state checking / legitimacy (CHECK) /////
    ///////////////////////////////////////////////////////////////////////
    @Ignore
    @Test
    public void test_that_CHECK_state_asking_for_excluded_properties_is_not_permitted() {
        final String message = "Excluded property should cause illegal argument exception while asking its state.";
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                isCheckIllegally(name, message);
            }
        }, "excludedManuallyProp");
    }

    @Test
    public void test_that_CHECK_state_for_disabled_properties_is_correct() { // (disabled == immutably checked or unchecked)
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                if (!dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name)) {
                    final String message = "Immutably unchecked (disabled) property [" + name + "] should return 'false' CHECK state.";
                    isCheck_equals_to_state(name, message, false);
                }
            }
        }, "disabledManuallyProp");
    }

    @Test
    public void test_that_CHECK_state_for_untouched_properties_is_correct() { // correct state should be "unchecked"
        final String message = "Untouched property (also not muted in representation) should return 'false' CHECK state.";
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                if (!dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name)) {
                    isCheck_equals_to_state(name, message, false);
                }
            }
        }, "bigDecimalProp");
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
        // checked properties, defined in isChecked() contract
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                final String message = "Checked property [" + name
                        + "], defined in isChecked() contract, should return 'true' CHECK state, and after manual mutation its state should be desired.";
                isCheck_equals_to_state(name, message, true);
                dtm().getFirstTick().check(MasterEntity.class, name, false);
                isCheck_equals_to_state(name, message, false);
                dtm().getFirstTick().check(MasterEntity.class, name, true);
                isCheck_equals_to_state(name, message, true);
            }
        }, "mutablyCheckedProp");
    }

    @Test
    public void test_that_CHECK_state_for_mutated_by_Check_method_properties_is_actually_mutated() {
        // checked properties, mutated_by_Check_method
        allLevels(new IAction() {
            @Override
            public void action(final String name) {
                final String message = "Checked property [" + name
                        + "], defined in isChecked() contract, should return 'true' CHECK state, and after manual mutation its state should be desired.";
                dtm().getFirstTick().check(MasterEntity.class, name, false);
                isCheck_equals_to_state(name, message, false);
                dtm().getFirstTick().check(MasterEntity.class, name, true);
                isCheck_equals_to_state(name, message, true);
            }
        }, "mutablyCheckedProp");
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_CHECKed_properties_order_is_correct() throws Exception {
        checkSomeProps(dtm());

        // at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
        assertEquals("The checked properties are incorrect.", Arrays.asList("mutablyCheckedProp", "checkedManuallyProp", "entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());

        // next -- lightweight operation -- no loading will be performed
        assertEquals("The checked properties are incorrect.", Arrays.asList("mutablyCheckedProp", "checkedManuallyProp", "entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        // simple lightweight example
        assertEquals("The checked properties are incorrect.", Collections.emptyList(), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_CHECKed_properties_order_is_correct_and_can_be_altered() throws Exception {
        checkSomeProps(dtm());

        // at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
        assertEquals("The checked properties are incorrect.", Arrays.asList("mutablyCheckedProp", "checkedManuallyProp", "entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());

        // alter checked properties by checking / un-checking some property
        dtm().getFirstTick().check(MasterEntity.class, "integerProp", true);
        dtm().getFirstTick().check(MasterEntity.class, "entityProp.mutablyCheckedProp", false);

        // next -- lightweight operation -- no loading will be performed
        assertEquals("The checked properties are incorrect.", Arrays.asList("mutablyCheckedProp", "checkedManuallyProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp", "integerProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_CHECKed_properties_Move_Swap_operations_work() throws Exception {
        checkSomeProps(dtm());

        // at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
        // alter the order of properties
        dtm().getFirstTick().swap(MasterEntity.class, "mutablyCheckedProp", "entityProp.mutablyCheckedProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        dtm().getFirstTick().swap(MasterEntity.class, "mutablyCheckedProp", "entityProp.entityProp.mutablyCheckedProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        dtm().getFirstTick().moveToTheEnd(MasterEntity.class, "mutablyCheckedProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp", "mutablyCheckedProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        dtm().getFirstTick().move(MasterEntity.class, "mutablyCheckedProp", "entityProp.entityProp.mutablyCheckedProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "mutablyCheckedProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        dtm().getFirstTick().move(MasterEntity.class, "mutablyCheckedProp", "entityProp.checkedManuallyProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "mutablyCheckedProp", "entityProp.checkedManuallyProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
        dtm().getFirstTick().swap(MasterEntity.class, "mutablyCheckedProp", "entityProp.checkedManuallyProp");
        assertEquals("The checked properties are incorrect.", Arrays.asList("entityProp.mutablyCheckedProp", "checkedManuallyProp", "entityProp.entityProp.mutablyCheckedProp", "entityProp.checkedManuallyProp", "mutablyCheckedProp", "entityProp.entityProp.checkedManuallyProp", "collection.mutablyCheckedProp", "collection.checkedManuallyProp", "entityProp.collection.mutablyCheckedProp", "entityProp.collection.checkedManuallyProp", "entityProp.collection.slaveEntityProp.mutablyCheckedProp", "entityProp.collection.slaveEntityProp.checkedManuallyProp").toString(), dtm().getFirstTick().checkedProperties(MasterEntity.class).toString());
    }

    @Test
    public void test_that_CHECKed_properties_Move_Swap_operations_doesnot_work_for_non_checked_properties() {
        // at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
        // alter the order of properties
        try {
            dtm().getFirstTick().swap(MasterEntity.class, "unknown-prop", "entityProp.mutablyCheckedProp");
            fail("Non-existent or non-checked properties operation should fail.");
        } catch (final DomainTreeException e) {
        }
        try {
            dtm().getFirstTick().swap(MasterEntity.class, "mutablyCheckedProp", "entityProp.doubleProp"); // second is unchecked
            fail("Non-existent or non-checked properties operation should fail.");
        } catch (final DomainTreeException e) {
        }
        try {
            dtm().getFirstTick().move(MasterEntity.class, "unknown-prop", "entityProp.mutablyCheckedProp");
            fail("Non-existent or non-checked properties operation should fail.");
        } catch (final DomainTreeException e) {
        }
        try {
            dtm().getFirstTick().move(MasterEntity.class, "mutablyCheckedProp", "entityProp.doubleProp"); // second is unchecked
            fail("Non-existent or non-checked properties operation should fail.");
        } catch (final DomainTreeException e) {
        }
        try {
            dtm().getFirstTick().moveToTheEnd(MasterEntity.class, "doubleProp"); // unchecked
            fail("Non-existent or non-checked properties operation should fail.");
        } catch (final DomainTreeException e) {
        }
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_PropertyCheckingListeners_work() {
        i = 0;
        j = 0;
        assertEquals("Incorrect value 'i'.", 0, i);
        assertEquals("Incorrect value 'j'.", 0, j);

        dtm().getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
        dtm().getRepresentation().getFirstTick().disableImmutably(MasterEntity.class, "bigDecimalProp");
        assertEquals("Incorrect value 'i'.", 1, i);
        assertEquals("Incorrect value 'j'.", 0, j);

        dtm().getFirstTick().check(MasterEntity.class, "integerProp", true);
        assertEquals("Incorrect value 'i'.", 2, i);
        assertEquals("Incorrect value 'j'.", 0, j);

        dtm().getRepresentation().warmUp(MasterEntity.class, "entityProp.entityProp.slaveEntityProp");
        assertEquals("Incorrect value 'i'.", 3, i);
        assertEquals("Incorrect value 'j'.", 0, j);
    }

    @Test
    @Ignore("Ignored due to the removal of support for listeners. Need to revisit.")
    public void test_that_WeakPropertyCheckingListeners_work() {
        i = 0;
        j = 0;

        assertEquals("Incorrect value 'i'.", 0, i);
        assertEquals("Incorrect value 'j'.", 0, j);

        dtm().getFirstTick().check(MasterEntity.class, "bigDecimalProp", true);
        assertEquals("Incorrect value 'i'.", 1, i);
        assertEquals("Incorrect value 'j'.", 0, j);

        dtm().getFirstTick().check(MasterEntity.class, "integerProp", true);
        assertEquals("Incorrect value 'i'.", 1, i);
        assertEquals("Incorrect value 'j'.", 0, j);
    }
}
