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

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.IPropertyValueListener;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerTest;
import ua.com.fielden.platform.domaintree.testing.EntityForCentreCheckedProperties;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForCentreDomainTree;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;


/**
 * A test for {@link CentreDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManagerTest extends AbstractDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ICentreDomainTreeManager dtm() {
	return (ICentreDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(CentreDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_CentreDomainTreeManagerTest() {
	return new CentreDomainTreeManager(serialiser(), createRootTypes_for_CentreDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_CentreDomainTreeManagerTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeManagerTest());
	rootTypes.add(MasterEntityForCentreDomainTree.class);
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	rootTypes.add(EntityForCentreCheckedProperties.class);
	return rootTypes;
    }

    public static void manageTestingDTM_for_CentreDomainTreeManagerTest(final Object obj) {
	final ICentreDomainTreeManager dtm = (ICentreDomainTreeManager) obj;

	manageTestingDTM_for_AbstractDomainTreeTest(dtm.getRepresentation());

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);

	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getFirstTick().check(MasterEntity.class, name, true);
	    }
	}, "stringProp", "booleanProp", "dateProp", "integerProp", "moneyProp", "mutablyCheckedProp");
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm.getSecondTick().check(MasterEntity.class, name, true);
	    }
	}, "stringProp", "booleanProp", "dateProp", "integerProp", "moneyProp");

	// for ordering second tick test
	dtm.getSecondTick().check(MasterEntity.class, "", true);

	// check 'entityProp.simpleEntityProp' for Locators test
	dtm.getFirstTick().check(MasterEntity.class, "entityProp.simpleEntityProp", true);
	dtm.getFirstTick().check(MasterEntity.class, "simpleEntityProp", true);
    }

    public static void performAfterDeserialisationProcess_for_CentreDomainTreeManagerTest(final Object obj) {
    }

    public static void assertInnerCrossReferences_for_CentreDomainTreeManagerTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractDomainTreeManagerTest(dtm);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
	                 // this test is redundant due to lack of special isChecked logic in CriteriaDomainTreeManager
	// super.test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated();

	dtm().getFirstTick().setColumnsNumber(3);
	// checked properties, defined in isChecked() contract
	allLevels(new IAction() {
	    public void action(final String name) {
		final String message = "Checked property [" + name + "], defined in isChecked() contract, should return 'true' CHECK state, and after manual mutation its state should be desired.";
		isCheck_equals_to_state(name, message, true);

		assertEquals("", 3, dtm().getFirstTick().getColumnsNumber());

		System.out.println("=========== " + dtm().getFirstTick().checkedProperties(MasterEntity.class));

		assertTrue("Should contain placeholder. In this case it means that even 'checked by contract' properties are added interactively" +
				" using all necessary custom actions (e.g. placeholder management etc.)", containsPlaceHolder(dtm().getFirstTick().checkedProperties(MasterEntity.class)));

		dtm().getFirstTick().check(MasterEntity.class, name, false);
		isCheck_equals_to_state(name, message, false);
		dtm().getFirstTick().check(MasterEntity.class, name, true);
		isCheck_equals_to_state(name, message, true);
	    }
	}, "mutablyCheckedProp");
    }

    private boolean containsPlaceHolder(final List<String> checkedProperties) {
	System.out.println("\t\t\t" + checkedProperties);
	for (final String name : checkedProperties) {
	    if (AbstractDomainTree.isPlaceholder(name)) {
		return true;
	    }
	}
	return false;
    }

    ////////////////////// 6. Specific entity-centre logic //////////////////////
    @Test
    public void test_that_unchecked_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Unchecked property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		illegalAllLocatorActions(dtm().getFirstTick(), message, name);

		// get / set / isEmpty value 1 / 2
		try {
		    dtm().getFirstTick().getValue(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().isValueEmpty(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setValue(MasterEntity.class, name, "a value");
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getValue2(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().is2ValueEmpty(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setValue2(MasterEntity.class, name, "a value");
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// get / set Exclusive 1 / 2
		try {
		    dtm().getFirstTick().getExclusive(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setExclusive(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getExclusive2(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setExclusive2(MasterEntity.class, name, false);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// get / set date Prefix/Mnemonic/AndBefore
		try {
		    dtm().getFirstTick().getDatePrefix(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setDatePrefix(MasterEntity.class, name, DateRangePrefixEnum.CURR);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getDateMnemonic(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setDateMnemonic(MasterEntity.class, name, MnemonicEnum.DAY);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getAndBefore(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setAndBefore(MasterEntity.class, name, false);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// orNull / Not
		try {
		    dtm().getFirstTick().getOrNull(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setOrNull(MasterEntity.class, name, false);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getNot(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setNot(MasterEntity.class, name, false);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}

		// SECOND TICK

		// ordering/width
		try {
		    dtm().getSecondTick().toggleOrdering(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().getWidth(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getSecondTick().setWidth(MasterEntity.class, name, 87);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "uncheckedProp");
    }

    @Test
    public void test_that_locator_actions_cause_exceptions_for_NON_ENTITY_types_of_properties() {
	final String message = "Non-AE property should cause IllegalArgument exception for locator-related logic.";
	allLevels(new IAction() {
	    public void action(final String name) {
		illegalAllLocatorActions(dtm().getFirstTick(), message, name);
	    }
	}, "integerProp", "moneyProp", "booleanProp");
    }

    @Test
    public void test_that_Exclusive_actions_cause_exceptions_for_non_DoubleEditor_types_of_properties() {
	final String message = "Non-RANGE property should cause IllegalArgument exception for Exclusive-related logic.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// FIRST TICK
		// locators
		try {
		    dtm().getFirstTick().getExclusive(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setExclusive(MasterEntity.class, name, false);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getExclusive2(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setExclusive2(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "booleanProp", "mutablyCheckedProp", "stringProp", "entityProp", "simpleEntityProp");
    }

    @Test
    public void test_that_non_DoubleEditor_and_non_Boolean_properties_Value2_action_for_first_tick_cause_exceptions() {
	final String message = "Non Double Editor (and non boolean) property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// get/set for value2 by default
		try {
		    dtm().getFirstTick().getValue2(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().is2ValueEmpty(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setValue2(MasterEntity.class, name, "a value");
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "mutablyCheckedProp", "stringProp", "entityProp", "simpleEntityProp");
    }

    @Test
    public void test_that_Date_related_actions_cause_exceptions_for_NON_DATE_types_of_properties() {
	final String message = "Non-DATE property should cause IllegalArgument exception for Date-related logic.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// FIRST TICK
		// locators
		try {
		    dtm().getFirstTick().getDatePrefix(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setDatePrefix(MasterEntity.class, name, DateRangePrefixEnum.CURR);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getDateMnemonic(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setDateMnemonic(MasterEntity.class, name, MnemonicEnum.DAY);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getAndBefore(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().setAndBefore(MasterEntity.class, name, true);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "booleanProp", "stringProp", "simpleEntityProp", "bigDecimalProp");
    }

    @Test
    public void test_that_values_1_and_2_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues(true, "critOnlySingleAEProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "critOnlyAEProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "stringProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(true, "integerProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "integerProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(true, "moneyProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "moneyProp", dtm().getFirstTick(), "is2ValueEmpty");

	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "critOnlySingleAEProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "critOnlySingleAEProp", dtm().getFirstTick(), "isValueEmpty");

	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "critOnlyAEProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "critOnlyAEProp", dtm().getFirstTick(), "isValueEmpty");

	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "stringProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "stringProp", dtm().getFirstTick(), "isValueEmpty");

	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "is2ValueEmpty");

	final Date d = new Date();
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "is2ValueEmpty");

	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "is2ValueEmpty");

	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "moneyProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "moneyProp", dtm().getFirstTick(), "isValueEmpty");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "moneyProp", dtm().getFirstTick(), "is2ValueEmpty");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(false, "moneyProp", dtm().getFirstTick(), "is2ValueEmpty");
    }

    @Test
    public void test_that_Exclusive_1_and_2_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be NULL
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getExclusive");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getExclusive");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getExclusive2");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getExclusive2");

	// Alter and check //
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "setExclusive", Boolean.class);
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "setExclusive", Boolean.class);
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "setExclusive2", Boolean.class);
	checkOrSetMethodValues(true, "integerProp", dtm().getFirstTick(), "setExclusive2", Boolean.class);

	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "getExclusive");
	checkOrSetMethodValues(false, "integerProp", dtm().getFirstTick(), "getExclusive");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "getExclusive2");
	checkOrSetMethodValues(true, "integerProp", dtm().getFirstTick(), "getExclusive2");
    }

    @Test
    public void test_that_date_prefix_mnemonic_and_AndBefore_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be NULL
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getDatePrefix");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getDateMnemonic");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getAndBefore");

	// Alter and check //
	checkOrSetMethodValues(DateRangePrefixEnum.NEXT, "dateProp", dtm().getFirstTick(), "setDatePrefix", DateRangePrefixEnum.class);
	checkOrSetMethodValues(MnemonicEnum.DAY, "dateProp", dtm().getFirstTick(), "setDateMnemonic", MnemonicEnum.class);
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "setAndBefore", Boolean.class);

	checkOrSetMethodValues(DateRangePrefixEnum.NEXT, "dateProp", dtm().getFirstTick(), "getDatePrefix");
	checkOrSetMethodValues(MnemonicEnum.DAY, "dateProp", dtm().getFirstTick(), "getDateMnemonic");
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "getAndBefore");
    }

    @Test
    public void test_that_OrNull_and_Not_for_first_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be NULL
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getOrNull");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getNot");

	// Alter and check //
	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "setOrNull", Boolean.class);
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "setNot", Boolean.class);

	checkOrSetMethodValues(true, "dateProp", dtm().getFirstTick(), "getOrNull");
	checkOrSetMethodValues(false, "dateProp", dtm().getFirstTick(), "getNot");
    }

    @Test
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered() {
	// entities with composite and other complicated key should have no ordering applied (default first time)
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithCompositeKey.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithKeyTitleAndWithAEKeyType.class));

	final Class<?> root = MasterEntity.class;
	final String keyProp = "", prop2 = "integerProp", prop3 = "moneyProp";

	// THE FIRST TIME -- returns DEFAULT VALUES -- for ALL APPROPRIATELY CHECKED PROPERTIES //
	assertTrue("At first the property should be checked.", dtm().getSecondTick().isChecked(root, keyProp));
	// entities with simple key should have ASC ordering on that key
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(root));

	// THE FIRST TIME -- returns DEFAULT VALUES -- for SOME NOT CHECKED PROPERTIES //
	dtm().getSecondTick().check(root, keyProp, false);
	assertFalse("The property should be not checked.", dtm().getSecondTick().isChecked(root, keyProp));
	// entities with simple key should have ASC ordering on that key, but only if the key is checked
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(root));
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(root));

	// THE FIRST TIME -- returns DEFAULT VALUES -- for ALL APPROPRIATELY CHECKED PROPERTIES //
	dtm().getSecondTick().check(root, keyProp, true);
	assertTrue("The property should be checked.", dtm().getSecondTick().isChecked(root, keyProp));
	// entities with simple key should have ASC ordering on that key
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(root));

	// Alter and check //
	dtm().getSecondTick().toggleOrdering(root, keyProp);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, keyProp);
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop2);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(prop2, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop3);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(prop2, Ordering.ASCENDING), new Pair<String, Ordering>(prop3, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop3);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(prop2, Ordering.ASCENDING), new Pair<String, Ordering>(prop3, Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop2);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(prop2, Ordering.DESCENDING), new Pair<String, Ordering>(prop3, Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop2);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(prop3, Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().toggleOrdering(root, prop3);
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(root));
    }

    @Test
    public void test_that_Orderings_for_second_tick_can_be_changed_based_on_Checking_changes() {
	final Class<?> root = MasterEntity.class;
	final String keyProp = "", prop2 = "integerProp", prop3 = "moneyProp";

	assertTrue("The property should be checked.", dtm().getSecondTick().isChecked(root, keyProp));
	assertTrue("The property should be checked.", dtm().getSecondTick().isChecked(root, prop2));
	assertTrue("The property should be checked.", dtm().getSecondTick().isChecked(root, prop3));

	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().check(root, keyProp, false);
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(root));
	dtm().getSecondTick().check(root, keyProp, true);

	dtm().getSecondTick().toggleOrdering(root, prop2);
	dtm().getSecondTick().toggleOrdering(root, prop2);
	dtm().getSecondTick().toggleOrdering(root, prop3);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING), new Pair<String, Ordering>(prop2, Ordering.DESCENDING), new Pair<String, Ordering>(prop3, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));

	// un-check middle property
	dtm().getSecondTick().check(root, prop2, false);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING), new Pair<String, Ordering>(prop3, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));

	// un-check right property
	dtm().getSecondTick().check(root, prop3, false);
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(keyProp, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(root));

	// un-check last property
	dtm().getSecondTick().check(root, keyProp, false);
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(root));
    }

    @Test
    public void test_that_Width_for_second_tick_are_default_for_the_first_time_and_can_be_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// default value should be NULL
	checkOrSetMethodValuesForNonCollectional(80, "dateProp", dtm().getSecondTick(), "getWidth");
	checkOrSetMethodValuesForNonCollectional(85, "dateProp", dtm().getRepresentation().getSecondTick(), "setWidthByDefault", int.class);
	checkOrSetMethodValuesForNonCollectional(85, "dateProp", dtm().getSecondTick(), "getWidth");

	// Alter and check //
	checkOrSetMethodValuesForNonCollectional(95, "dateProp", dtm().getSecondTick(), "setWidth", int.class);

	checkOrSetMethodValuesForNonCollectional(95, "dateProp", dtm().getSecondTick(), "getWidth");
    }

    @Test
    public void test_that_column_number_for_first_tick_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be 2
	assertEquals("The default column number should be 2.", dtm().getFirstTick().getColumnsNumber(), 2);

	try {
	    dtm().getFirstTick().setColumnsNumber(0);
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getFirstTick().setColumnsNumber(-1);
	    fail("Should be failed.");
	} catch (final IllegalArgumentException e) {
	}

	// Alter and check //
	assertTrue("The first tick reference should be the same", dtm().getFirstTick() == dtm().getFirstTick().setColumnsNumber(3));
	assertEquals("The number of columns should be 3", dtm().getFirstTick().getColumnsNumber(), 3);
    }

    private ILocatorDomainTreeManagerAndEnhancer initDefaultLocatorForSomeTestType(final IGlobalDomainTreeManager managerForNonBaseUser) {
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	final ILocatorDomainTreeManagerAndEnhancer ldtmae = new LocatorDomainTreeManagerAndEnhancer(serialiser(), new HashSet<Class<?>>() {{ add(EntityWithStringKeyType.class); }});
	ldtmae.getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	managerForNonBaseUser.getGlobalRepresentation().setLocatorManagerByDefault(EntityWithStringKeyType.class, ldtmae);
	assertFalse("Should not be the same instance, it should be retrived every time.", ldtmae == managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class));
	assertTrue("Should be equal instance, because no one has been changed default locator.", ldtmae.equals(managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class)));
	return ldtmae;
    }

    @Test
    public void test_that_runAutomatically_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be FALSE
	assertFalse("Run automatically by default should be FALSE", dtm().isRunAutomatically());

	// Alter and check //
	assertTrue("The manager reference should be the same",dtm() == dtm().setRunAutomatically(true));
	assertTrue("Run automatically should be true", dtm().isRunAutomatically());
    }

    @Override
    public void test_that_PropertyCheckingListeners_work() {
    }

    private static int i, j;

    @Test
    public void test_that_PropertyValue1And2Listeners_work() {
	i = 0; j = 0;
	final IPropertyValueListener listener1 = new IPropertyValueListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Object newValue, final Object oldState) {
		i++;
	    }
	};
	dtm().getFirstTick().addPropertyValueListener(listener1);
	final IPropertyValueListener listener2 = new IPropertyValueListener() {
	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Object newValue, final Object oldState) {
		j++;
	    }
	};
	dtm().getFirstTick().addPropertyValue2Listener(listener2);

	final String prop = "booleanProp";
	dtm().getFirstTick().check(MasterEntity.class, prop, true);
	assertEquals("Incorrect value 'i'.", 0, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getFirstTick().setValue(MasterEntity.class, prop, true);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 0, j);

	dtm().getFirstTick().setValue2(MasterEntity.class, prop, true);
	assertEquals("Incorrect value 'i'.", 1, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	dtm().getFirstTick().setValue(MasterEntity.class, prop, false);
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 1, j);

	dtm().getFirstTick().setValue2(MasterEntity.class, prop, false);
	assertEquals("Incorrect value 'i'.", 2, i);
	assertEquals("Incorrect value 'j'.", 2, j);
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Checked properties with PLACEHOLDERS ///////////////
    ////////////////////////////////////////////////////////////////////////////////

    private static final Class<?> rootForCheckedPropsTesting = EntityForCentreCheckedProperties.class;

    @Override
    @Test
    public void test_that_CHECKed_properties_order_is_correct() throws Exception {
	// at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	// next -- lightweight operation -- no loading will be performed
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = serialiser().serialise(dtm());
	final IDomainTreeManager copy = serialiser().deserialise(array, IDomainTreeManager.class);
	assertEquals("The checked properties are incorrect.", Arrays.asList(), copy.getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	// simple lightweight example
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    public void test_that_CHECKed_properties_order_is_correct_and_can_be_altered() throws Exception {
	dtm().getFirstTick().setColumnsNumber(3);

	// at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	assertEquals("Columns number is incorrect.", 3, dtm().getFirstTick().getColumnsNumber());

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "moneyProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "booleanProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "bigDecimalProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "bigDecimalProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "bigDecimalProp", true);
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("2-placeholder-origin-0-0", "moneyProp", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "moneyProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "2-placeholder-origin-0-1", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("3-placeholder-origin-0-0", "2-placeholder-origin-0-1", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "booleanProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = serialiser().serialise(dtm());
	final IDomainTreeManager copy = serialiser().deserialise(array, IDomainTreeManager.class);
	assertEquals("The checked properties are incorrect.", Arrays.asList("bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), copy.getFirstTick().checkedProperties(rootForCheckedPropsTesting));
    }

    @Test
    public void test_that_CHECKed_properties_and_placeholders_are_dependent_on_ColumnsNumber() throws Exception {
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(3);
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	assertEquals("Columns number is incorrect.", 3, dtm().getFirstTick().getColumnsNumber());

	// check first property and change "columns number" to reflect the change in "checked properties"
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(1);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(3);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	// check next (second) property and change "columns number" to reflect the change in "checked properties"
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "moneyProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(1);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(4);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "0-placeholder-origin-0-2", "1-placeholder-origin-0-3"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	// check next (third) property and change "columns number" to reflect the change in "checked properties"
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "booleanProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "1-placeholder-origin-0-3"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "1-placeholder-origin-0-3"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(3);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "0-placeholder-origin-1-1"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(1);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	// misc
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "moneyProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().setColumnsNumber(2);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", false);
	assertEquals("The checked properties are incorrect.", Arrays.asList("0-placeholder-origin-0-0", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
    }

    @Override
    @Test
    public void test_that_CHECKed_properties_Move_Swap_operations_work() throws Exception {
	dtm().getFirstTick().setColumnsNumber(3);
	assertEquals("The checked properties are incorrect.", Arrays.asList(), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	assertEquals("Columns number is incorrect.", 3, dtm().getFirstTick().getColumnsNumber());

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-0-1", "1-placeholder-origin-0-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().swap(rootForCheckedPropsTesting, "integerProp", "1-placeholder-origin-0-2");
	assertEquals("The checked properties are incorrect.", Arrays.asList("1-placeholder-origin-0-2", "0-placeholder-origin-0-1", "integerProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	dtm().getFirstTick().swap(rootForCheckedPropsTesting, "integerProp", "0-placeholder-origin-0-1");
	assertEquals("The checked properties are incorrect.", Arrays.asList("1-placeholder-origin-0-2", "integerProp", "0-placeholder-origin-0-1"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));
	dtm().getFirstTick().swap(rootForCheckedPropsTesting, "integerProp", "1-placeholder-origin-0-2");
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "1-placeholder-origin-0-2", "0-placeholder-origin-0-1"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "moneyProp", true);
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "booleanProp", true);
	dtm().getFirstTick().check(rootForCheckedPropsTesting, "bigDecimalProp", true);
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "moneyProp", "booleanProp", "bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().swap(rootForCheckedPropsTesting, "moneyProp", "0-placeholder-origin-1-1");
	assertEquals("The checked properties are incorrect.", Arrays.asList("integerProp", "0-placeholder-origin-1-1", "booleanProp", "bigDecimalProp", "moneyProp", "1-placeholder-origin-1-2"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	dtm().getFirstTick().check(rootForCheckedPropsTesting, "integerProp", false);
	dtm().getFirstTick().swap(rootForCheckedPropsTesting, "booleanProp", "1-placeholder-origin-1-2");
	assertEquals("The checked properties are incorrect.", Arrays.asList("bigDecimalProp", "moneyProp", "booleanProp"), dtm().getFirstTick().checkedProperties(rootForCheckedPropsTesting));

	try {
	    dtm().getFirstTick().move(rootForCheckedPropsTesting, "bullshitProp", "bullshitProp");
	    fail("Move operation should be unsupported.");
	} catch (final UnsupportedOperationException e) {
	}
	try {
	    dtm().getFirstTick().moveToTheEnd(rootForCheckedPropsTesting, "bullshitProp");
	    fail("MoveToTheEnd operation should be unsupported.");
	} catch (final UnsupportedOperationException e) {
	}

	// serialise and deserialise and then check the order of "checked properties"
	final byte[] array = serialiser().serialise(dtm());
	final IDomainTreeManager copy = serialiser().deserialise(array, IDomainTreeManager.class);
	assertEquals("The checked properties are incorrect.", Arrays.asList("bigDecimalProp", "moneyProp", "booleanProp"), copy.getFirstTick().checkedProperties(rootForCheckedPropsTesting));
    }

    @Override
    @Test
    public void test_that_CHECKed_properties_Move_Swap_operations_doesnot_work_for_non_checked_properties() {
	// at first the manager will be initialised the first time and its "included" and then "checked" props will be initialised (heavy operation)
	// alter the order of properties
	try {
	    dtm().getFirstTick().swap(rootForCheckedPropsTesting, "unknown-prop", "integerProp");
	    fail("Non-existent properties operation should fail.");
	} catch (final IllegalArgumentException e) {
	}
	try {
	    dtm().getFirstTick().swap(rootForCheckedPropsTesting, "integerProp", "bigDecimalProp"); // both are unchecked
	    fail("Non-checked properties operation should fail.");
	} catch (final IllegalArgumentException e) {
	}

	try {
	    dtm().getFirstTick().move(rootForCheckedPropsTesting, "bullshitProp", "bullshitProp");
	    fail("Move operation should be unsupported.");
	} catch (final UnsupportedOperationException e) {
	}
	try {
	    dtm().getFirstTick().moveToTheEnd(rootForCheckedPropsTesting, "bullshitProp");
	    fail("MoveToTheEnd operation should be unsupported.");
	} catch (final UnsupportedOperationException e) {
	}
    }
}
