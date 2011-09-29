package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentationTest;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for "analyses" tree representation.
 *
 * @author TG Team
 *
 */
public class AbstractAnalysisDomainTreeRepresentationTest extends AbstractDomainTreeRepresentationTest {
    @Override
    protected IAbstractAnalysisDomainTreeManagerAndEnhancer dtm() {
	return (IAbstractAnalysisDomainTreeManagerAndEnhancer)super.dtm();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractDomainTreeRepresentationTest();
	rootTypes.add(SlaveEntity.class);
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(final IAbstractAnalysisDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractDomainTreeRepresentationTest(dtm);

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IAbstractAnalysisDomainTreeManagerAndEnhancer dtm = new AbstractAnalysisDomainTreeManagerAndEnhancer1(serialiser(), createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    ////////////////////// 1. Excluding logic //////////////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 1.3. Type related logic //////////////////////
    @Test
    public void test_that_collections_itself_and_their_children_are_disabled() {
	assertTrue("Collection itself should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection"));
	assertTrue("Collection itself should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection"));

	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));

	assertTrue("Collection itself should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection"));
	assertTrue("Collection itself should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection"));

	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));
    }

    ////////////////////// 1.4. Annotation related logic //////////////////////
    @Override
    @Test
    public void test_that_children_of_crit_only_AE_or_AE_collection_property_are_excluded() {
    }

    @Test
    public void test_that_crit_only_properties_are_excluded() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Crit-only property should be excluded.", dtm().getRepresentation().isExcludedImmutably(MasterEntity.class, name));
	    }
	}, "critOnlyProp");
    }

    ////////////////////// 1.5. Recursive excluding logic //////////////////////

    ////////////////////////////////////////////////////////////////
    ////////////////////// 2. 1-st tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 2.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_first_tick_disabling() {
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(SlaveEntity.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
    }

    @Override
    @Test
    public void test_that_any_excluded_properties_first_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	super.test_that_any_excluded_properties_first_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception();

	try {
	    dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp");
	    fail("Excluded property should cause illegal argument exception.");
	} catch (final IllegalArgumentException e) {
	}
    }
    ////////////////////// 2.1. Specific disabling logic //////////////////////

    ////////////////////// 2.2. Type related logic //////////////////////
    @Test
    public void test_that_first_tick_for_properties_of_entity_with_AE_or_composite_key_type_are_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Property of 'entity with AE or composite key' type should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "entityPropWithAEKeyType", "entityWithCompositeKeyProp");
    }

    @Test
    public void test_that_first_tick_for_properties_of_non_entity_or_non_boolean_are_disabled() {
	// range types (except dates)
	// integer
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Property should be disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "integerProp", "doubleProp", "bigDecimalProp", "moneyProp", "stringProp", "dateProp");
    }

    @Test
    public void test_that_first_tick_for_properties_of_entity_or_date_or_boolean_are_NOT_disabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		assertFalse("Property should be not disabled.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "simpleEntityProp", "booleanProp");
    }

    @Test
    public void test_that_first_tick_for_integer_EXPR_calculated_properties_originated_from_date_property_are_enabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.EXPRESSION, "dateProp", Integer.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("EXPRESSION calculated properties of integer type based on date property should be enabled for first tick.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "dateExprProp");
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_second_tick_disabling() {
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithNormalNature.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getRepresentation().getSecondTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterSyntheticEntity.class, ""));
    }

    @Override
    @Test
    public void test_that_any_excluded_properties_second_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	super.test_that_any_excluded_properties_second_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception();
	try {
	    dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp");
	    fail("Excluded property should cause illegal argument exception.");
	} catch (final IllegalArgumentException e) {
	}
    }
    ////////////////////// 3.1. Specific disabling logic //////////////////////
    ////////////////////// 3.2. Type related logic //////////////////////

    @Test
    public void test_that_second_tick_for_all_properties_is_disabled() {
	// TODO check whether it's correct
	allLevels(new IAction() {
	    public void action(final String name) {
		assertTrue("Property should be disabled.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "integerProp", "doubleProp", "bigDecimalProp", "moneyProp", "stringProp", "simpleEntityProp", "entityPropWithAEKeyType", "entityWithCompositeKeyProp", "dateProp", "booleanProp");
    }

    @Test
    public void test_that_second_tick_for_calculated_properties_of_AGGREGATED_EXPRESSION_type_are_NOT_disabled() {
	// TODO check whether it's correct
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "integerProp", Integer.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "intAggExprProp");

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "bigDecimalProp", BigDecimal.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "bigDecimalAggExprProp");

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "moneyProp", Money.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "moneyAggExprProp");

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "doubleProp", BigDecimal.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "doubleAggExprProp");

	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		dtm().getEnhancer().addCalculatedProperty(new CalculatedProperty(MasterEntity.class, name, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, "stringProp", String.class, "expr", "title", "desc"));
		dtm().getEnhancer().apply();

		assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	    }
	}, "stringAggExprProp");
    }

    ////////////////////// 3.3. Type related logic //////////////////////
    @Override
    @Test
    public void test_that_other_properties_are_not_disabled() {
	assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "simpleEntityProp"));
	assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.simpleEntityProp"));
	assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.simpleEntityProp"));
    }

    @Override
    protected List<Function> enhanceFunctionsWithCollectionalAttributes(final List<Function> functions) {
	// for collectional properties in analysis representation logic (available functions) there should not be available ALL and ANY attributes.
	// see test_that_any_property_has_type_related_functions() test for more details.
	return functions;
    }

    //////////////////////4. Specific analysis logic //////////////////////
    @Test
    public void test_that_excluded_properties_actions_for_second_ticks_cause_exceptions_for_all_specific_logic() {
	final String message = "Excluded property should cause IllegalArgument exception.";
	allLevels(new IAction() {
	    public void action(final String name) {
		// isOrderingDisabled/disableOrdering
		try {
		    dtm().getRepresentation().getSecondTick().isOrderingDisabledImmutably(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}

		// get/set Ordering by default
		try {
		    dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EvenSlaverEntity.class);
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
		try {
		    dtm().getRepresentation().getSecondTick().setOrderedPropertiesByDefault(EvenSlaverEntity.class, new ArrayList<Pair<String, Ordering>>());
		    fail(message);
		} catch (final IllegalArgumentException e) {
		}
	    }
	}, "excludedManuallyProp");
    }

    // TODO tests were taken from CriteiraDomainTreeRepresentationTest. Check whether they are correct. Does "ordering" methods must conflict with disabling methods?
    @Test
    public void test_that_ordering_disablements_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// none of the properties should have ordering disabled by default.
	checkOrSetMethodValues(false, "dateProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");
	checkOrSetMethodValues(false, "integerProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");

	// Alter DEFAULT and check //
	allLevels(new IAction() {
	    public void action(final String name) {
		dtm().getRepresentation().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
	    }
	}, "dateProp");
	checkOrSetMethodValues(true, "dateProp", dtm().getRepresentation().getSecondTick(), "isOrderingDisabledImmutably");
    }

    @Test
    public void test_that_orderings_by_default_for_second_tick_are_desired_and_can_be_altered(){
	// DEFAULT CONTRACT //
	// Default ordering for the analysis's second tick should be empty.
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EntityWithCompositeKey.class));
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter DEFAULT and check //
	final List<Pair<String, Ordering>> ordering = Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING));
	dtm().getRepresentation().getSecondTick().setOrderedPropertiesByDefault(MasterEntity.class, ordering);
	assertEquals("Default value is incorrect.", ordering, dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
    }
}
