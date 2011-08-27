package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domain.tree.EntityWithCompositeKey;
import ua.com.fielden.platform.domain.tree.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domain.tree.MasterEntity;
import ua.com.fielden.platform.domain.tree.MasterSyntheticEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeManager.ICriteriaDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeManagerTest;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;


/**
 * A test for {@link CriteriaDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class CriteriaDomainTreeManagerTest extends AbstractDomainTreeManagerTest {
    @Override
    protected ICriteriaDomainTreeManagerAndEnhancer dtm() {
	return (ICriteriaDomainTreeManagerAndEnhancer) super.dtm();
    }

    @Override
    protected Set<Class<?>> createRootTypes() {
	final Set<Class<?>> rootTypes = super.createRootTypes();
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	return rootTypes;
    }

    @Override
    protected ICriteriaDomainTreeManagerAndEnhancer createManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	return new CriteriaDomainTreeManagerAndEnhancer(serialiser, rootTypes);
    }

    @Override
    protected void manageTestingDTM(final IDomainTreeManager dtm) {
        super.manageTestingDTM(dtm);

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
        dtm().initAnalysisManagerByDefault("New Pivot analysis.", AnalysisType.PIVOT);
        dtm().acceptAnalysisManager("New Pivot analysis.");
    }

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
		// FIRST TICK
		// locators
		try {
		    dtm().getFirstTick().initLocatorManagerByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().produceLocatorManagerByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().discardLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().acceptLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().saveLocatorManagerGlobally(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().isChangedLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		// get / set value 1 / 2
		try {
		    dtm().getFirstTick().getValue(MasterEntity.class, name);
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
		// FIRST TICK
		// locators
		try {
		    dtm().getFirstTick().initLocatorManagerByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().produceLocatorManagerByDefault(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().discardLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().acceptLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().saveLocatorManagerGlobally(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().getLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getFirstTick().isChangedLocatorManager(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "integerProp", "moneyProp", "booleanProp");
    }

    @Test
    public void test_that_Exclusive_actions_cause_exceptions_for_NON_RANGE_types_of_properties() {
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
	}, "booleanProp", "stringProp", "simpleEntityProp");
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
	checkOrSetMethodValues(null, "critOnlySingleAEProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new ArrayList<String>(), "critOnlyAEProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("", "stringProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "dateProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "integerProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(null, "moneyProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues("a value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues("a new value for single crit only", "critOnlySingleAEProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(new ArrayList<String>() {{ add("a new value for crit only"); }}, "critOnlyAEProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues("a value for str", "stringProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues("a new value for str", "stringProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(false, "booleanProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(false, "booleanProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(true, "booleanProp", dtm().getFirstTick(), "getValue2");

	final Date d = new Date();
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(d, "dateProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(d, "dateProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(new Date(d.getTime() + 10000), "dateProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(0, "integerProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(0, "integerProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(100, "integerProp", dtm().getFirstTick(), "getValue2");

	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValueByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "setValue");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "getValue");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getRepresentation().getFirstTick(), "setValue2ByDefault");
	checkOrSetMethodValues(new Money(new BigDecimal(0.0)), "moneyProp", dtm().getFirstTick(), "getValue2");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "setValue2");
	checkOrSetMethodValues(new Money(new BigDecimal(100.0)), "moneyProp", dtm().getFirstTick(), "getValue2");
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
	final String name = "A brand new analysis";
	assertNull("Should be null before creation.", dtm().getAnalysisManager(name));

	try {
	    dtm().removeAnalysisManager(name);
	    fail("The removal of non-existent analysis should fail.");
	} catch (final IllegalArgumentException e) {
	}

	// initialise a brand new instance of analysis (e.g. pivot)
	dtm().initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	try {
	    dtm().initAnalysisManagerByDefault(name, AnalysisType.SIMPLE);
	    fail("The creation of analysis with same name should fail.");
	} catch (final IllegalArgumentException e) {
	}
	assertTrue("The instance should be 'changed' after initialisation.", dtm().isChangedAnalysisManager(name));

	// obtain a current instance of analysis and alter it
	IPivotDomainTreeManager pivotMgr = (IPivotDomainTreeManager) dtm().getAnalysisManager(name);
	pivotMgr.getFirstTick().check(MasterEntity.class, "simpleEntityProp", true);
	assertTrue("The instance should remain 'changed' after some operations.", dtm().isChangedAnalysisManager(name));

	// discard just created analysis
	dtm().discardAnalysisManager(name);
	assertNull("Should be null after discarding.", dtm().getAnalysisManager(name));
	assertFalse("The instance should be 'unchanged' after discard operation.", dtm().isChangedAnalysisManager(name));

	// initialise a brand new instance of analysis again (e.g. pivot)
	dtm().initAnalysisManagerByDefault(name, AnalysisType.PIVOT);
	assertTrue("The instance should be 'changed' after initialisation.", dtm().isChangedAnalysisManager(name));
	// obtain a current instance of analysis and alter it
	pivotMgr = (IPivotDomainTreeManager) dtm().getAnalysisManager(name);
	pivotMgr.getFirstTick().check(MasterEntity.class, "simpleEntityProp", true);
	assertTrue("The instance should remain 'changed' after some operations.", dtm().isChangedAnalysisManager(name));

	// accept just created analysis
	dtm().acceptAnalysisManager(name);
	assertFalse("The instance should become 'unchanged' after accept operation.", dtm().isChangedAnalysisManager(name));
	assertNotNull("Should be not null after accepting.", dtm().getAnalysisManager(name));
	assertTrue("Should be checked.", dtm().getAnalysisManager(name).getFirstTick().isChecked(MasterEntity.class, "simpleEntityProp"));

	// alter and discard
	dtm().getAnalysisManager(name).getFirstTick().check(MasterEntity.class, "booleanProp", true);
	assertTrue("The instance should become 'changed' after some operations.", dtm().isChangedAnalysisManager(name));
	dtm().discardAnalysisManager(name);
	assertFalse("The instance should be 'unchanged' after discard operation.", dtm().isChangedAnalysisManager(name));
	assertNotNull("Should be not null after accept operation.", dtm().getAnalysisManager(name));
	assertTrue("Should be checked.", dtm().getAnalysisManager(name).getFirstTick().isChecked(MasterEntity.class, "simpleEntityProp"));
	assertFalse("Should be unchecked after discarding.", dtm().getAnalysisManager(name).getFirstTick().isChecked(MasterEntity.class, "booleanProp"));
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
	final CriteriaDomainTreeManagerAndEnhancer dtme = (CriteriaDomainTreeManagerAndEnhancer) dtm;
	final CriteriaDomainTreeManager abstractDtm = (CriteriaDomainTreeManager) dtme.base();
	final AddToCriteriaTickManager firstTm = (AddToCriteriaTickManager) abstractDtm.getFirstTick();
	assertNotNull("Should be not null.", firstTm);
	assertNotNull("Should be not null.", firstTm.getSerialiser());
	assertTrue("Should be identical.", abstractDtm.getSerialiser() == firstTm.getSerialiser());
    }

    @Override
    public void test_that_CHECKed_properties_order_is_correct() throws Exception {
    }
    
    @Override
    public void test_that_CHECKed_properties_Move_Swap_operations_work() throws Exception {
    }
}
