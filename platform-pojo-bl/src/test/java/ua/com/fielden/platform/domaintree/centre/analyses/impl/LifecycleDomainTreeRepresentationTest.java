package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithoutKeyType;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.MasterEntityWithUnionForIncludedPropertiesLogic;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;

/**
 * A test for {@link LifecycleDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ILifecycleDomainTreeRepresentation dtm() {
	return (ILifecycleDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(LifecycleDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_LifecycleDomainTreeRepresentationTest() {
	return new LifecycleDomainTreeRepresentation(serialiser(), createRootTypes_for_LifecycleDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_LifecycleDomainTreeRepresentationTest() {
	final CentreDomainTreeManagerAndEnhancer cdtmae = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeRepresentationTest());
	cdtmae.provideLifecycleAnalysesDatePeriodProperties(createRootTypes_for_LifecycleDomainTreeRepresentationTest());
	return cdtmae;
    }

    protected static Set<Class<?>> createRootTypes_for_LifecycleDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_LifecycleDomainTreeRepresentationTest(final Object obj) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_LifecycleDomainTreeRepresentationTest(final Object obj) {
	performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void assertInnerCrossReferences_for_LifecycleDomainTreeRepresentationTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_LifecycleDomainTreeRepresentationTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeRepresentationTest();
    }

    public static void performAdditionalInitialisationProcess_for_LifecycleDomainTreeRepresentationTest(final Object obj) {
	final LifecycleDomainTreeRepresentation repr = (LifecycleDomainTreeRepresentation) obj;
	repr.provideMetaStateForLifecycleAnalysesDatePeriodProperties();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Date Period properties ////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_Entity_Itself_properties_are_enabled_for_first_tick() {
	assertFalse("'Entity_Itself' property should be enabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, ""));
    }

    @Test
    public void test_that_date_period_properties_exist_and_are_included() {
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    assertFalse("'" + period.getPropertyName() + "' property should exist and be included.", dtm().isExcludedImmutably(MasterEntity.class, period.getPropertyName()));
	}
    }

    @Test
    public void test_that_date_period_properties_are_enabled_for_first_tick() {
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    assertFalse("'" + period.getPropertyName() + "' property should be enabled for distribution.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, period.getPropertyName()));
	}
    }

    @Test
    public void test_that_date_period_properties_are_disabled_for_second_tick() {
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    assertTrue("'" + period.getPropertyName() + "' property should be disabled for aggregation.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, period.getPropertyName()));
	}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Lifecycle @Monitoring properties //////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_that_Lifecycle_properties_are_NOT_disabled_for_second_tick() {
	assertFalse("Lifecycle property should be enabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "simpleEntityProp"));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category properties ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_that_Category_properties_are_NOT_disabled_for_second_tick() {
	final LifecycleDomainTreeRepresentation repr = (LifecycleDomainTreeRepresentation) dtm();
	repr.parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(MasterEntity.class, "", LifecycleDomainTreeRepresentation.CATEGORY_PROPERTY_MARKER, "Category 1", "Category 1 desc", CalculatedPropertyAttribute.NO_ATTR, "SELF");
	repr.parentCentreDomainTreeManager().getEnhancer().apply();

	assertFalse("Category property should be enabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, "category1"));
    }

    /// all others ///
    @Override
    @Test
    public void test_that_second_tick_for_all_properties_is_disabled() {
    }

    @Override
    @Test
    public void test_that_second_tick_for_calculated_properties_of_AGGREGATED_EXPRESSION_type_are_NOT_disabled() {
    }

    @Override
    @Test
    public void test_that_other_properties_are_not_disabled() {
    }

    ///////////////// overridden in order to reflect new included properties -- Date period properties /////////////////
    @Override
    @Test
    public void test_that_order_of_included_properties_is_correct_and_circular_references_manage_Dummy_property() {
	// , "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"
	assertEquals("Not root type -- should return empty list of included properties.", Collections.emptyList(), dtm().includedProperties(EntityWithoutKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "bigDecimalProp", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(EntityWithStringKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(EntityWithNormalNature.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// test that heavy-weight entities will be processed correctly
	dtm().includedProperties(MasterEntity.class);
	dtm().includedProperties(SlaveEntity.class);
	dtm().includedProperties(EvenSlaverEntity.class);
    }

    @Override
    @Test
    public void test_that_warming_up_of_included_properties_works_correctly() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	// warm up first-level circular property
	dtm().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.dummy-property", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// warm up n-th-level circular property (for example third)
	dtm().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.dummy-property", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    @Ignore
    public void test_that_included_properties_for_union_entities_hierarchy_are_correct_and_manage_Common_and_Union_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "unionEntityProp", "unionEntityProp.common-properties", "unionEntityProp.common-properties.desc", "unionEntityProp.common-properties.commonProp", "unionEntityProp.unionProp1", "unionEntityProp.unionProp1.dummy-property", "unionEntityProp.unionProp2", "unionEntityProp.unionProp2.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityWithUnionForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    public void test_that_manual_exclusion_is_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().excludeImmutably(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection.integerProp");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.dummy-property", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	dtm().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.dummy-property", "entityPropCollection", "entityPropCollection.moneyProp", "__YEAR", "__MONTH", "__FORTNIGHT", "__WEEK", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    public void test_entities_itself_first_tick_checking() {
	assertFalse("An entity itself (represented by empty 'property') should NOT be checked.", dtm().getFirstTick().isCheckedImmutably(MasterEntity.class, ""));
	assertFalse("By contract should be checked.", dtm().getFirstTick().isCheckedImmutably(EntityWithStringKeyType.class, ""));
	assertFalse("By contract should be enabled.", dtm().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
    }

    @Override
    @Test
    public void test_entities_itself_first_tick_disabling() {
	assertFalse("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, ""));
	assertFalse("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(SlaveEntity.class, ""));
	assertFalse("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithStringKeyType.class, ""));
	assertFalse("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithCompositeKey.class, ""));
	assertFalse("An entity itself (of any type) should be disabled for distribution properties.", dtm().getFirstTick().isDisabledImmutably(EntityWithKeyTitleAndWithAEKeyType.class, ""));
    }
}
