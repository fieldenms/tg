package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.IPropertyValueListener;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAnalysisListener;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToCriteriaTickManager;
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

    @Override
    protected ICentreDomainTreeManagerAndEnhancer dtm() {
	return (ICentreDomainTreeManagerAndEnhancer) super.dtm();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractDomainTreeManagerTest();
	rootTypes.add(MasterEntityForCentreDomainTree.class);
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	rootTypes.add(EntityForCentreCheckedProperties.class);
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_CentreDomainTreeManagerTest(final ICentreDomainTreeManager dtm) {
	manageTestingDTM_for_AbstractDomainTreeTest(dtm);

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);

	// check some properties to test its values, exclusiveness, etc.
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm.getRepresentation().getFirstTick().checkImmutably(MasterEntity.class, name);
		dtm.getRepresentation().getSecondTick().checkImmutably(MasterEntity.class, name);
	    }
	}, "critOnlySingleAEProp", "critOnlyAEProp");
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

	// initialise analysis to ensure that equals / serialisation / copying works
	dtm.initAnalysisManagerByDefault("New Pivot analysis.", AnalysisType.PIVOT);
	dtm.acceptAnalysisManager("New Pivot analysis.");
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_CentreDomainTreeManagerTest());
	manageTestingDTM_for_CentreDomainTreeManagerTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
	// this test is redundant due to lack of special isChecked logic in CriteriaDomainTreeManager
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
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered(){
	// THE FIRST TIME -- returns DEFAULT VALUES //
	// entities with simple key should have ASC ordering on that key
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	// entities with composite and other complicated key should have no ordering applied
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithCompositeKey.class));
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter and check //
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.ASCENDING)), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "integerProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "moneyProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "moneyProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "integerProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.DESCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "integerProp");
	assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
	dtm().getSecondTick().toggleOrdering(MasterEntity.class, "moneyProp");
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
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
	assertEquals("The default column number should be 2", dtm().getFirstTick().getColumnsNumber(), 2);

	// Alter and check //
	assertTrue("The first tick reference should be the same",dtm().getFirstTick() == dtm().getFirstTick().setColumnsNumber(3));
	assertEquals("The number of columns should be 3", dtm().getFirstTick().getColumnsNumber(), 3);
    }

    @Test
    public void test_initialisation_discarding_and_saving_Analyses() {
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.PIVOT, "A brand new PIVOT analysis");
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.SIMPLE, "A brand new SIMPLE analysis");
	test_initialisation_discarding_and_saving_of_Analyses(AnalysisType.LIFECYCLE, "A brand new LIFECYCLE analysis");
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

	IAbstractAnalysisDomainTreeManagerAndEnhancer currentMgrBeforeFreeze = dtm().getAnalysisManager(name2);

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

    private ILocatorDomainTreeManagerAndEnhancer initDefaultLocatorForSomeTestType(final IGlobalDomainTreeManager managerForNonBaseUser) {
	// initialise a default locator for type EntityWithStringKeyType which will affect initialisation of [MasterEntity.entityProp.simpleEntityProp] property.
	final ILocatorDomainTreeManagerAndEnhancer ldtmae = new LocatorDomainTreeManagerAndEnhancer(getSerialiser(), new HashSet<Class<?>>() {{ add(EntityWithStringKeyType.class); }});
	ldtmae.getFirstTick().check(EntityWithStringKeyType.class, "integerProp", true);
	managerForNonBaseUser.getGlobalRepresentation().setLocatorManagerByDefault(EntityWithStringKeyType.class, ldtmae);
	assertFalse("Should not be the same instance, it should be retrived every time.", ldtmae == managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class));
	assertTrue("Should be equal instance, because no one has been changed default locator.", ldtmae.equals(managerForNonBaseUser.getGlobalRepresentation().getLocatorManagerByDefault(EntityWithStringKeyType.class)));
	return ldtmae;
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
    protected void test_that_manager_instantiation_works_for_inner_cross_references(final IDomainTreeManagerAndEnhancer dtm) {
	super.test_that_manager_instantiation_works_for_inner_cross_references(dtm);

	// check if "serialiser" is initialised for first tick manager after passing it into CriteriDomainTreeManager context (into constructor).
	final CentreDomainTreeManagerAndEnhancer dtme = (CentreDomainTreeManagerAndEnhancer) dtm;
	final CentreDomainTreeManager abstractDtm = (CentreDomainTreeManager) dtme.base();
	final AddToCriteriaTickManager firstTm = (AddToCriteriaTickManager) abstractDtm.getFirstTick();
	assertNotNull("Should be not null.", firstTm);
	assertNotNull("Should be not null.", firstTm.getSerialiser());
	assertTrue("Should be identical.", abstractDtm.getSerialiser() == firstTm.getSerialiser());
    }

    @Override
    public void test_that_PropertyCheckingListeners_work() {
    }

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
	final byte[] array = getSerialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
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
	final byte[] array = getSerialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
	assertEquals("The checked properties are incorrect.", Arrays.asList("bigDecimalProp", "0-placeholder-origin-1-1", "1-placeholder-origin-1-2"), copy.getFirstTick().checkedProperties(rootForCheckedPropsTesting));
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
	final byte[] array = getSerialiser().serialise(dtm());
	final IDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, IDomainTreeManagerAndEnhancer.class);
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

}
