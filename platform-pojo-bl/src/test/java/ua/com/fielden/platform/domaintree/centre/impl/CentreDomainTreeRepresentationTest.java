package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentationTest;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for entity centres tree representation.
 * 
 * @author TG Team
 * 
 */
public class CentreDomainTreeRepresentationTest extends AbstractDomainTreeRepresentationTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ICentreDomainTreeRepresentation dtm() {
        return (ICentreDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(CentreDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_CentreDomainTreeRepresentationTest() {
        return new CentreDomainTreeRepresentation(serialiser(), createRootTypes_for_CentreDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_CentreDomainTreeRepresentationTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeRepresentationTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeRepresentationTest());
        rootTypes.add(EntityWithCompositeKey.class);
        rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
        rootTypes.add(MasterSyntheticEntity.class);
        return rootTypes;
    }

    public static void manageTestingDTM_for_CentreDomainTreeRepresentationTest(final Object obj) {
        manageTestingDTM_for_AbstractDomainTreeTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_CentreDomainTreeRepresentationTest(final Object dtr) {
    }

    public static void assertInnerCrossReferences_for_CentreDomainTreeRepresentationTest(final Object dtm) {
        assertInnerCrossReferences_for_AbstractDomainTreeRepresentationTest(dtm);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Excluding logic //////////////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 2.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_first_tick_disabling() {
        super.test_entities_itself_first_tick_disabling();
        assertFalse("Entity itself (represented by empty 'property') with composite key type should be enabled.", dtm().getFirstTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
        assertTrue("Entity itself (represented by empty 'property') with AE key type should be disabled.", dtm().getFirstTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
    }

    ////////////////////// 2.2. Annotation related logic //////////////////////
    @Test
    public void test_that_first_tick_for_crit_only_properties_are_disabled() {
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    @Test
    public void test_that_first_tick_for_result_only_properties_and_their_children_are_disabled() {
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp"));
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp"));
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp"));
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp"));
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp"));
        assertTrue("Result-only property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp"));

        // (1-level children)
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp.integerProp"));

        // (2-level children)
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "resultOnlyProp.entityProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.resultOnlyProp.slaveEntityProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.resultOnlyProp.entityProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.resultOnlyProp.slaveEntityProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.resultOnlyProp.entityProp.integerProp"));
        assertTrue("Result-only property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.resultOnlyProp.slaveEntityProp.integerProp"));
    }

    ////////////////////// 2.3. Type related logic //////////////////////
    @Test
    public void test_that_first_tick_for_properties_of_entity_with_AE_key_type_are_disabled() {
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityPropWithAEKeyType"));
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_second_tick_disabling() {
        super.test_entities_itself_second_tick_disabling();
        assertFalse("Entity itself (represented by empty 'property') with composite key type should be enabled.", dtm().getSecondTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
        assertTrue("Entity itself (represented by empty 'property') with AE key type should be disabled.", dtm().getSecondTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
        assertFalse("An synthetic entity itself (represented by empty 'property') should NOT be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterSyntheticEntity.class, ""));
    }

    ////////////////////// 3.2. Annotation related logic //////////////////////
    @Test
    public void test_that_second_tick_for_crit_only_properties_are_disabled() {
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
        assertTrue("Crit-only property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    @Test
    public void test_that_second_tick_for_collectional_properties_children_are_disabled() {
        // (1-level children)
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.collection.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.collection.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.collection.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.collection.integerProp"));

        // (2-level children)
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.collection.entityProp.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.collection.slaveEntityProp.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.collection.entityProp.integerProp"));
        assertTrue("Collectional property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.collection.slaveEntityProp.integerProp"));
    }

    ////////////////////// 3.3. Type related logic //////////////////////
    @Test
    public void test_that_second_tick_for_properties_of_entity_with_AE_key_type_are_disabled() {
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.entityPropWithAEKeyType"));
        assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.entityPropWithAEKeyType"));
    }

    ////////////////////// 4.2. Annotation related logic //////////////////////
    @Test
    public void test_that_first_tick_for_crit_only_properties_are_checked() {
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    ////////////////////// 4.3. Disabling of immutable checked properties //////////////////////
    @Override
    @Test
    public void test_that_checked_properties_first_tick_are_actually_disabled() {
        super.test_that_checked_properties_first_tick_are_actually_disabled();
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.critOnlyProp"));
        assertFalse("At this stage critOnly props should be enabled and unchecked.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.critOnlyProp"));
    }

    ////////////////////// 6. Specific entity-centre logic //////////////////////
    @Test
    public void test_that_excluded_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
        final String message = "Excluded property should cause IllegalArgument exception.";
        allLevels(new IAction() {
            public void action(final String name) {
                // get/set for value1/2 by default
                try {
                    dtm().getFirstTick().getValueByDefault(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().getEmptyValueFor(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().setValueByDefault(MasterEntity.class, name, "a value");
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().getValue2ByDefault(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().get2EmptyValueFor(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().setValue2ByDefault(MasterEntity.class, name, "a value");
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }

                // isOrderingDisabled/disableOrdering
                try {
                    dtm().getSecondTick().isOrderingDisabledImmutably(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }

                // get/set Ordering by default
                try {
                    dtm().getSecondTick().orderedPropertiesByDefault(EvenSlaverEntity.class);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getSecondTick().setOrderedPropertiesByDefault(EvenSlaverEntity.class, new ArrayList<Pair<String, Ordering>>());
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }

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
            }
        }, "excludedManuallyProp");
    }

    @Test
    public void test_that_non_DoubleEditor_and_non_Boolean_properties_Value2_action_for_first_tick_cause_exceptions() {
        final String message = "Non Double Editor (and non boolean) property should cause IllegalArgument exception.";
        allLevels(new IAction() {
            public void action(final String name) {
                // get/set for value2 by default
                try {
                    dtm().getFirstTick().getValue2ByDefault(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().get2EmptyValueFor(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                try {
                    dtm().getFirstTick().setValue2ByDefault(MasterEntity.class, name, "a value");
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
            }
        }, "mutablyCheckedProp", "stringProp", "entityProp");
    }

    @Test
    public void test_that_values_1_and_2_by_default_for_first_tick_are_desired_and_can_be_altered() {
        checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValue2ByDefault");

        checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "get2EmptyValueFor");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "get2EmptyValueFor");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "get2EmptyValueFor");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "get2EmptyValueFor");

        checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getFirstTick(), "getEmptyValueFor");

        checkOrSetMethodValues(new ArrayList<String>() {
            {
                add("a value for crit only");
            }
        }, "critOnlyAEProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues(new ArrayList<String>() {
            {
                add("a value for crit only");
            }
        }, "critOnlyAEProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getEmptyValueFor");

        checkOrSetMethodValues("a value for str", "stringProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues("a value for str", "stringProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getEmptyValueFor");

        checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "setValue2ByDefault");
        checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "get2EmptyValueFor");

        final Date d = new Date();
        checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "setValue2ByDefault");
        checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "get2EmptyValueFor");

        checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "setValue2ByDefault");
        checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "get2EmptyValueFor");

        checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "setValueByDefault");
        checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValueByDefault");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getEmptyValueFor");
        checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "setValue2ByDefault");
        checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValue2ByDefault");
        checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "get2EmptyValueFor");
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

    @Test
    public void test_that_ordering_disablements_for_second_tick_are_desired_and_can_be_altered() {
        // DEFAULT CONTRACT //
        // none of the properties should have ordering disabled by default.
        checkOrSetMethodValues(false, "dateProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");
        checkOrSetMethodValues(false, "integerProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");

        // Alter DEFAULT and check //
        allLevels(new IAction() {
            public void action(final String name) {
                dtm().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
            }
        }, "dateProp");
        checkOrSetMethodValues(true, "dateProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");
    }

    @Test
    public void test_that_orderings_by_default_for_second_tick_are_desired_and_can_be_altered() {
        // DEFAULT CONTRACT //
        // entities with simple key should have ASC ordering on that key
        assertEquals("Default value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.ASCENDING)), dtm().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
        // entities with composite and other complicated key should have no ordering applied
        assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedPropertiesByDefault(EntityWithCompositeKey.class));
        assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedPropertiesByDefault(EntityWithKeyTitleAndWithAEKeyType.class));

        // Alter DEFAULT and check //
        final List<Pair<String, Ordering>> ordering = Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING));
        dtm().getSecondTick().setOrderedPropertiesByDefault(MasterEntity.class, ordering);
        assertEquals("Default value is incorrect.", ordering, dtm().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
    }
}
