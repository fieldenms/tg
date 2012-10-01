package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
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

    ///////////////// overridden in order to reflect new included properties -- Date period properties /////////////////
    @Override
    @Test
    public void test_that_order_of_included_properties_is_correct_and_circular_references_manage_Dummy_property() {
	// , "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"
	assertEquals("Not root type -- should return empty list of included properties.", Collections.emptyList(), dtm().includedProperties(EntityWithoutKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "bigDecimalProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(EntityWithStringKeyType.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(EntityWithNormalNature.class));
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// test that heavy-weight entities will be processed correctly
	dtm().includedProperties(MasterEntity.class);
	dtm().includedProperties(SlaveEntity.class);
	dtm().includedProperties(EvenSlaverEntity.class);
    }

    @Override
    @Test
    public void test_that_warming_up_of_included_properties_works_correctly() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	// warm up first-level circular property
	dtm().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropCollection.moneyProp", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));

	// warm up n-th-level circular property (for example third)
	dtm().warmUp(MasterEntityForIncludedPropertiesLogic.class, "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.desc", "entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.desc", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.dummy-property", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropOfSelfType.entityPropCollection.moneyProp", "entityPropOfSelfType.entityProp", "entityPropOfSelfType.entityProp.integerProp", "entityPropOfSelfType.entityProp.moneyProp", "entityPropOfSelfType.entityPropCollection", "entityPropOfSelfType.entityPropCollection.integerProp", "entityPropOfSelfType.entityPropCollection.moneyProp", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    public void test_that_included_properties_for_union_entities_hierarchy_are_correct_and_manage_Common_and_Union_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "unionEntityProp", "unionEntityProp.common-properties", "unionEntityProp.common-properties.desc", "unionEntityProp.common-properties.commonProp", "unionEntityProp.unionProp1", "unionEntityProp.unionProp1.nonCommonPropFrom1", "unionEntityProp.unionProp2", "unionEntityProp.unionProp2.nonCommonPropFrom2", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityWithUnionForIncludedPropertiesLogic.class));
    }

    @Override
    @Test
    public void test_that_manual_exclusion_is_correctly_reflected_in_Included_properties() {
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.integerProp", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
	dtm().excludeImmutably(MasterEntityForIncludedPropertiesLogic.class, "entityPropCollection.integerProp");
	assertEquals("Incorrect included properties.", Arrays.asList("", "desc", "integerProp", "entityPropOfSelfType", "entityPropOfSelfType.dummy-property", "entityProp", "entityProp.integerProp", "entityProp.moneyProp", "entityPropCollection", "entityPropCollection.moneyProp", "__WEEK", "__YEAR", "__MONTH", "__FORTNIGHT", "__DAY"), dtm().includedProperties(MasterEntityForIncludedPropertiesLogic.class));
    }
}
