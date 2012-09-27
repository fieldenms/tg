package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentationTest;
import ua.com.fielden.platform.domaintree.testing.AbstractAnalysisDomainTreeRepresentation1;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for "analyses" tree representation.
 *
 * @author TG Team
 *
 */
public class AbstractAnalysisDomainTreeRepresentationTest extends AbstractDomainTreeRepresentationTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IAbstractAnalysisDomainTreeRepresentation dtm() {
	return (IAbstractAnalysisDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(AbstractAnalysisDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_AbstractAnalysisDomainTreeRepresentationTest() {
	return new AbstractAnalysisDomainTreeRepresentation1(serialiser(), createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_AbstractAnalysisDomainTreeRepresentationTest() {
	return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
    }

    protected static Set<Class<?>> createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeRepresentationTest());
	rootTypes.add(SlaveEntity.class);
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	return rootTypes;
    }

    public static void manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(final Object obj) {
	manageTestingDTM_for_AbstractDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeRepresentationTest(final Object obj) {
	CentreDomainTreeManagerAndEnhancer.initAnalysisManagerReferencesOn((IAbstractAnalysisDomainTreeRepresentation) obj, (ICentreDomainTreeManagerAndEnhancer) irrelevantDtm());
    }

    public static void assertInnerCrossReferences_for_AbstractAnalysisDomainTreeRepresentationTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractDomainTreeRepresentationTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeRepresentationTest() {
	final String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual = new String [1];
	fieldWhichReferenceShouldNotBeDistictButShouldBeEqual[0] = "parentCentreDomainTreeManager";
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual;
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
	assertTrue("Collection itself should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection"));
	assertTrue("Collection itself should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection"));

	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));

	assertTrue("Collection itself should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection"));
	assertTrue("Collection itself should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection"));

	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp"));
	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.entityProp.integerProp"));
	assertTrue("Collection property child should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.slaveEntityProp.integerProp"));
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
		assertTrue("Crit-only property should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, name));
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
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(SlaveEntity.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
    }

    @Override
    @Test
    public void test_that_any_excluded_properties_first_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	super.test_that_any_excluded_properties_first_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception();

	try {
	    dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp");
	    fail("Excluded property should cause illegal argument exception.");
	} catch (final IllegalArgumentException e) {
	}
    }
    ////////////////////// 2.1. Specific disabling logic //////////////////////

    ////////////////////// 2.2. Type related logic //////////////////////
    @Test
    public void test_that_first_tick_for_properties_of_entity_with_AE_key_type_are_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    assertTrue("Property of 'entity with AE key' type should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
		}
	    }
	}, "entityPropWithAEKeyType");
    }

    @Test
    public void test_that_first_tick_for_properties_of_non_entity_or_non_boolean_or_non_string_are_disabled() {
	// range types (except dates)
	// integer
	allLevels(new IAction() {
	    public void action(final String name) {
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    assertTrue("Property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
		}
	    }
	}, "integerProp", "doubleProp", "bigDecimalProp", "moneyProp", "dateProp");
    }

    @Test
    public void test_that_first_tick_for_properties_of_entity_or_date_or_boolean_or_string_are_NOT_disabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    assertFalse("Property should be not disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
		}
	    }
	}, "simpleEntityProp", "booleanProp", "stringProp");
    }

    @Test
    public void test_that_first_tick_for_integer_EXPR_calculated_properties_originated_from_date_property_are_enabled() {
	allLevelsWithoutCollections(new IAction() {
	    public void action(final String name) {
		((AbstractAnalysisDomainTreeRepresentation) dtm()).parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(MasterEntity.class, name, "YEAR(dateProp)", "Calc date prop", "Desc", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
		((AbstractAnalysisDomainTreeRepresentation) dtm()).parentCentreDomainTreeManager().getEnhancer().apply();
		if (!dtm().isExcludedImmutably(MasterEntity.class, name(name, "calcDateProp"))) {
		    assertFalse("EXPRESSION calculated properties of integer type based on date property should be enabled for first tick.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, name(name, "calcDateProp")));
		}
	    }
	}, ""); // calcDateProp
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////
    @Override
    @Test
    public void test_entities_itself_second_tick_disabling() {
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getSecondTick().isDisabledImmutably(EntityWithNormalNature.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getSecondTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getSecondTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
	assertTrue("All second tick properties should be disabled, including entity itself", dtm().getSecondTick().isDisabledImmutably(MasterSyntheticEntity.class, ""));
    }

    @Override
    @Test
    public void test_that_any_excluded_properties_second_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception() {
	super.test_that_any_excluded_properties_second_tick_disabling_and_isDisabled_checking_cause_IllegalArgument_exception();
	try {
	    dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "critOnlyProp");
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
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    assertTrue("Property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
		}
	    }
	}, "integerProp", "doubleProp", "bigDecimalProp", "moneyProp", "stringProp", "simpleEntityProp", "entityPropWithAEKeyType", "entityWithCompositeKeyProp", "dateProp", "booleanProp");
    }

    protected void checkSecTickEnablementForAGGR_EXPRessions(final String originationProperty, final int i, final String contextPath) {
	((AbstractAnalysisDomainTreeRepresentation) dtm()).parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(MasterEntity.class, contextPath, "MAX(" + originationProperty + ")", originationProperty + " aggr expr " + i, "Desc", CalculatedPropertyAttribute.NO_ATTR, originationProperty);
	((AbstractAnalysisDomainTreeRepresentation) dtm()).parentCentreDomainTreeManager().getEnhancer().apply();
	final String name = originationProperty + "AggrExpr" + i;
	if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
	    assertFalse("AGGREGATED_EXPRESSION calculated properties should be enabled for second tick.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, name));
	}
    }

    @Test
    public void test_that_second_tick_for_calculated_properties_of_AGGREGATED_EXPRESSION_type_are_NOT_disabled() {
	// TODO check whether it's correct
	checkSecTickEnablementForAGGR_EXPRessions("integerProp", 1, "");
	checkSecTickEnablementForAGGR_EXPRessions("integerProp", 2, "entityProp");
	checkSecTickEnablementForAGGR_EXPRessions("integerProp", 3, "entityProp.entityProp");

	checkSecTickEnablementForAGGR_EXPRessions("bigDecimalProp", 1, "");
	checkSecTickEnablementForAGGR_EXPRessions("bigDecimalProp", 2, "entityProp");
	checkSecTickEnablementForAGGR_EXPRessions("bigDecimalProp", 3, "entityProp.entityProp");

	checkSecTickEnablementForAGGR_EXPRessions("moneyProp", 1, "");
	checkSecTickEnablementForAGGR_EXPRessions("moneyProp", 2, "entityProp");
	checkSecTickEnablementForAGGR_EXPRessions("moneyProp", 3, "entityProp.entityProp");

	// checkSecTickEnablementForAGGR_EXPRessions("doubleProp", 1, ""); // Double properties is not supported at all, BigDecimal should be used instead
	// checkSecTickEnablementForAGGR_EXPRessions("doubleProp", 2, "entityProp"); // Double properties is not supported at all, BigDecimal should be used instead
	// checkSecTickEnablementForAGGR_EXPRessions("doubleProp", 3, "entityProp.entityProp"); // Double properties is not supported at all, BigDecimal should be used instead

	checkSecTickEnablementForAGGR_EXPRessions("stringProp", 1, "");
	checkSecTickEnablementForAGGR_EXPRessions("stringProp", 2, "entityProp");
	checkSecTickEnablementForAGGR_EXPRessions("stringProp", 3, "entityProp.entityProp");
    }

    ////////////////////// 3.3. Type related logic //////////////////////
    @Override
    @Test
    public void test_that_other_properties_are_not_disabled() {
	if (!dtm().isExcludedImmutably(MasterEntity.class, "simpleEntityProp")) {
	    assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "simpleEntityProp"));
	}
	if (!dtm().isExcludedImmutably(MasterEntity.class, "entityProp.simpleEntityProp")) {
	    assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.simpleEntityProp"));
	}
	if (!dtm().isExcludedImmutably(MasterEntity.class, "entityProp.entityProp.simpleEntityProp")) {
	    assertTrue("All second tick properties including simple entity properties should be disabled", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.entityProp.simpleEntityProp"));
	}
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
	    }
	}, "excludedManuallyProp");
    }

    // TODO tests were taken from CriteiraDomainTreeRepresentationTest. Check whether they are correct. Does "ordering" methods must conflict with disabling methods?
    @Test
    public void test_that_ordering_disablements_for_second_tick_are_desired_and_can_be_altered() {
	// DEFAULT CONTRACT //
	// none of the properties should have ordering disabled by default.
	if (!dtm().isExcludedImmutably(MasterEntity.class, "dateProp")) {
	    checkOrSetMethodValues(false, "dateProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");
	}
	if (!dtm().isExcludedImmutably(MasterEntity.class, "integerProp")) {
	    checkOrSetMethodValues(false, "integerProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");
	}

	// Alter DEFAULT and check //
	allLevels(new IAction() {
	    public void action(final String name) {
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    dtm().getSecondTick().disableOrderingImmutably(MasterEntity.class, name);
		}
	    }
	}, "dateProp");
	if (!dtm().isExcludedImmutably(MasterEntity.class, "dateProp")) {
	    checkOrSetMethodValues(true, "dateProp", dtm().getSecondTick(), "isOrderingDisabledImmutably");
	}
    }

    @Test
    public void test_that_orderings_by_default_for_second_tick_are_desired_and_can_be_altered(){
	// DEFAULT CONTRACT //
	// Default ordering for the analysis's second tick should be empty.
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedPropertiesByDefault(EntityWithCompositeKey.class));
	assertEquals("Default value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedPropertiesByDefault(EntityWithKeyTitleAndWithAEKeyType.class));

	// Alter DEFAULT and check //
	final List<Pair<String, Ordering>> ordering = Arrays.asList(new Pair<String, Ordering>("integerProp", Ordering.ASCENDING), new Pair<String, Ordering>("moneyProp", Ordering.DESCENDING));
	dtm().getSecondTick().setOrderedPropertiesByDefault(MasterEntity.class, ordering);
	assertEquals("Default value is incorrect.", ordering, dtm().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));
    }

//    @Override
//    @Test
//    public void test_that_serialisation_works() throws Exception {
//	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm()));
//	test_that_manager_instantiation_works_for_inner_cross_references(dtm());
//
//	// test that serialisation works
//	final byte[] array = getSerialiser().serialise(dtm());
//	assertNotNull("Serialised byte array should not be null.", array);
//	final Object copy = getSerialiser().deserialise(array, Object.class);
//	// final ICriteriaDomainTreeManager copy = getSerialiser().deserialise(array, ICriteriaDomainTreeManager.class);
//	// final CriteriaDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, CriteriaDomainTreeManagerAndEnhancer.class);
//	assertNotNull("Deserialised instance should not be null.", copy);
//
//	CentreDomainTreeManager.initAnalysisManagerReferencesOn((IAbstractAnalysisDomainTreeManager) copy, dtm().parentCentreDomainTreeManager());
//
//	// after deserialisation the instance should be fully defined (even for transient fields).
//	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
//	// So it should be checked:
//	assertTrue("After deserialisation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm(), "parentCentreDomainTreeManager"));
//	test_that_manager_instantiation_works_for_inner_cross_references(copy);
//    }
//
//    @Override
//    @Test
//    public void test_that_equality_and_copying_works() {
//	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm()));
//
//	final Object copy = CentreDomainTreeManager.copyAnalysis(dtm(), getSerialiser());
//	// after copying the instance should be fully defined (even for transient fields).
//	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
//	// So it should be checked:
//	assertTrue("After coping of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm(), "parentCentreDomainTreeManager"));
//	test_that_manager_instantiation_works_for_inner_cross_references(copy);
//	assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(copy, dtm()));
//    }

    @Override
    @Test
    public void test_entities_itself_second_tick_checking() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be checked.", dtm().getSecondTick().isCheckedImmutably(MasterEntity.class, ""));
	assertFalse("By contract should be unchecked.", dtm().getSecondTick().isCheckedImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("By contract should NOT be disabled.", dtm().getSecondTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
    }

    @Override
    @Test
    public void test_that_checked_properties_first_tick_are_actually_disabled() {
	allLevels(new IAction() {
	    public void action(final String name) {
		if (!dtm().isExcludedImmutably(MasterEntity.class, name)) {
		    assertTrue("By contract of 'disabled' it should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, name));
		}
	    }
	}, "checkedManuallyProp");
    }

    @Override
    @Test
    public void test_entities_itself_first_tick_checking() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be checked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, ""));
	assertFalse("By contract should be checked.", dtm().getFirstTick().isCheckedImmutably(EntityWithStringKeyType.class, ""));
	assertTrue("By contract should be enabled.", dtm().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
    }
}
