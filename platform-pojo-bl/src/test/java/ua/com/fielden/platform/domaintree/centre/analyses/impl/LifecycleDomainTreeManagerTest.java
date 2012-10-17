package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntityDatePropCategorizer.MasterEntityDatePropCategory;
import ua.com.fielden.platform.domaintree.testing.MasterEntitySimpleEntityPropCategorizer.MasterEntitySimpleEntityPropCategory;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for {@link LifecycleDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeManagerTest extends AbstractAnalysisDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ILifecycleDomainTreeManager dtm() {
	return (ILifecycleDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(LifecycleDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_LifecycleDomainTreeManagerTest() {
	return new LifecycleDomainTreeManager(serialiser(), createRootTypes_for_LifecycleDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_LifecycleDomainTreeManagerTest() {
	final CentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeManagerTest());
	dtm.provideLifecycleAnalysesDatePeriodProperties(createRootTypes_for_LifecycleDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(dtm);
	return dtm;
    }

    protected static Set<Class<?>> createRootTypes_for_LifecycleDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
	rootTypes.remove(EvenSlaverEntity.class); // this entity has been excluded manually in parent tests
	return rootTypes;
    }

    public static void manageTestingDTM_for_LifecycleDomainTreeManagerTest(final Object obj) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_LifecycleDomainTreeManagerTest(final Object obj) {
	performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void assertInnerCrossReferences_for_LifecycleDomainTreeManagerTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractAnalysisDomainTreeManagerTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_LifecycleDomainTreeManagerTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeManagerTest();
    }

    public static void performAdditionalInitialisationProcess_for_LifecycleDomainTreeManagerTest(final Object obj) {
	final LifecycleDomainTreeManager mgr = (LifecycleDomainTreeManager) obj;
	mgr.provideMetaStateForLifecycleAnalysesDatePeriodProperties();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void test_that_usage_management_works_correctly_for_first_tick() {
        //////////////////// Overridden to provide "single-selection" logic, instead of "multiple-selection" as in abstract parent class ////////////////////

	// At the beginning the list of used properties should be empty.
	assertEquals("Value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));

	// Add "use properties" and see whether list of "used properties" is correctly ordered.
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", true);
	assertTrue("The property should be used.", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("booleanProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", true);
	assertTrue("The property should be used.", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertFalse("The property should be NOT used.", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("dateExprProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityProp", true);
	assertTrue("The property should be used.", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityProp"));
	assertFalse("The property should be NOT used.", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertFalse("The property should be NOT used.", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "dateExprProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "dateExprProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "booleanProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "booleanProp"));
	assertEquals("value is incorrect.", Arrays.asList("simpleEntityProp"), dtm().getFirstTick().usedProperties(MasterEntity.class));
	dtm().getFirstTick().use(MasterEntity.class, "simpleEntityProp", false);
	assertFalse("The property shouldn't be used", dtm().getFirstTick().isUsed(MasterEntity.class, "simpleEntityProp"));
	assertEquals("value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));
    }

    @Test
    public void test_that_FROM_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be NULL
	assertNull("The default FROM should be Null.", dtm().getFrom());

	// Alter and check //
	final Date d = new Date();
	assertTrue("The first tick reference should be the same.", dtm() == dtm().setFrom(d));
	assertEquals("The FROM should be altered correctly.", new Date(d.getTime()), dtm().getFrom());
    }


    @Test
    public void test_that_TO_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be NULL
	assertNull("The default TO should be Null.", dtm().getTo());

	// Alter and check //
	final Date d = new Date();
	assertTrue("The first tick reference should be the same.", dtm() == dtm().setTo(d));
	assertEquals("The TO should be altered correctly.", new Date(d.getTime()), dtm().getTo());
    }

    @Test
    public void test_that_IsTotal_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be False
	assertEquals("The default IsTotal value should be FALSE.", false, dtm().isTotal());

	// Alter and check //
	assertTrue("The first tick reference should be the same.", dtm() == dtm().setTotal(true));
	assertEquals("The IsTotal value should be adjusted.", true, dtm().isTotal());
    }

    @Override
    public void test_that_PropertyUsageListeners_work() {
    }

    @Override
    public void test_that_PropertyOrderingListeners_work() {
    }

    ///////////////////////// Date Period checking /////////////////////////
    @Test
    public void test_that_date_period_properties_are_checked_for_first_tick() {
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    assertTrue("'" + period.getPropertyName() + "' property should be checked (mutably) for distribution.", dtm().getFirstTick().isChecked(MasterEntity.class, period.getPropertyName()));
	}
    }

    @Test
    public void test_that_date_period_properties_are_unchecked_for_second_tick() {
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    assertFalse("'" + period.getPropertyName() + "' property should be checked (mutably) for categories (aka aggregation).", dtm().getSecondTick().isChecked(MasterEntity.class, period.getPropertyName()));
	}
    }

    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Lifecycle properties checking /////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    @Test
    public void test_that_changing_lifecycle_property_leads_to_tree_expanding_by_category_markers_for_second_tick() throws InstantiationException, IllegalAccessException {
	final CentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(centre);
	centre.initAnalysisManagerByDefault("Lifecycle report", AnalysisType.LIFECYCLE);
	final ILifecycleDomainTreeManager dtm = (ILifecycleDomainTreeManager) centre.getAnalysisManager("Lifecycle report");

	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be NULL
	assertNull("The default LifecycleProperty should be Null.", dtm.getLifecycleProperty());
	assertFalse("At the first time, the included properties should not contain any category.", dtm.getRepresentation().includedProperties(MasterEntity.class).contains("available"));
	assertEquals("At the first time, the checked properties should be empty.", Arrays.asList(), dtm.getSecondTick().checkedProperties(MasterEntity.class));
	assertEquals("At the first time, the used properties should be empty as well as 'checked' properties.", Arrays.asList(), dtm.getSecondTick().usedProperties(MasterEntity.class));
	assertEquals("At the first time, no categories exist in the domain (no lifecycle property has been selected).", Arrays.asList(), dtm.getSecondTick().allCategories(MasterEntity.class));
	assertEquals("At the first time, no categories exist in the domain (no lifecycle property has been selected).", Arrays.asList(), dtm.getSecondTick().currentCategories(MasterEntity.class));

	// Alter LifecycleProperty by checking it in the second tick //
	dtm.getSecondTick().check(MasterEntity.class, "simpleEntityProp", true);
	assertEquals("The LifecycleProperty should be 'simpleEntityProp' from 'MasterEntity' class.", new Pair<Class<?>, String>(MasterEntity.class, "simpleEntityProp"), dtm.getLifecycleProperty());
	assertTrue("The included properties should contain categories.", dtm.getRepresentation().includedProperties(MasterEntity.class).contains("available"));
	assertEquals("The checked properties consist of ONE lifecycle property and several 'all category' properties.", Arrays.asList("simpleEntityProp", "available", "broken", "unoperational"), dtm.getSecondTick().checkedProperties(MasterEntity.class));
	assertEquals("The used properties consist of several 'main category' properties.", Arrays.asList("available", "broken"), dtm.getSecondTick().usedProperties(MasterEntity.class));
	assertEquals("'simpleEntityProp' lifecycle property has been selected -- its categories should be returned.", Arrays.asList(MasterEntitySimpleEntityPropCategory.AVAILABLE, MasterEntitySimpleEntityPropCategory.BROKEN, MasterEntitySimpleEntityPropCategory.UNOPERATIONAL), dtm.getSecondTick().allCategories(MasterEntity.class));
	assertEquals("'simpleEntityProp' lifecycle property has been selected -- its categories (selected!) should be returned.", Arrays.asList(MasterEntitySimpleEntityPropCategory.AVAILABLE, MasterEntitySimpleEntityPropCategory.BROKEN), dtm.getSecondTick().currentCategories(MasterEntity.class));

	// Remove LifecycleProperty by unchecking it in the second tick //
	dtm.getSecondTick().check(MasterEntity.class, "simpleEntityProp", false);
	assertNull("LifecycleProperty has become Null.", dtm.getLifecycleProperty());
	assertFalse("The included properties should not contain any category.", dtm.getRepresentation().includedProperties(MasterEntity.class).contains("available"));
	assertEquals("The 'checked' properties became empty.", Arrays.asList(), dtm.getSecondTick().checkedProperties(MasterEntity.class));
	assertEquals("The 'used' properties became empty.", Arrays.asList(), dtm.getSecondTick().usedProperties(MasterEntity.class));
	assertEquals("No categories exist in the domain (no lifecycle property has been selected).", Arrays.asList(), dtm.getSecondTick().allCategories(MasterEntity.class));
	assertEquals("No categories exist in the domain (no lifecycle property has been selected).", Arrays.asList(), dtm.getSecondTick().currentCategories(MasterEntity.class));

	// Alter LifecycleProperty by checking it in the second tick and then check another lifecycle property //
	dtm.getSecondTick().check(MasterEntity.class, "simpleEntityProp", true);
	dtm.getSecondTick().check(MasterEntity.class, "dateProp", true);

	assertEquals("The LifecycleProperty should be 'dateProp' from 'MasterEntity' class.", new Pair<Class<?>, String>(MasterEntity.class, "dateProp"), dtm.getLifecycleProperty());
	assertTrue("The included properties should contain categories.", dtm.getRepresentation().includedProperties(MasterEntity.class).contains("future"));
	assertEquals("The checked properties consist of ONE lifecycle property and several 'all category' properties.", Arrays.asList("dateProp", "future", "now", "past"), dtm.getSecondTick().checkedProperties(MasterEntity.class));
	assertEquals("The used properties consist of several 'main category' properties.", Arrays.asList("future", "past"), dtm.getSecondTick().usedProperties(MasterEntity.class));
	assertEquals("'dateProp' lifecycle property has been selected -- its categories should be returned.", Arrays.asList(MasterEntityDatePropCategory.FUTURE, MasterEntityDatePropCategory.NOW, MasterEntityDatePropCategory.PAST), dtm.getSecondTick().allCategories(MasterEntity.class));
	assertEquals("'dateProp' lifecycle property has been selected -- its categories (selected!) should be returned.", Arrays.asList(MasterEntityDatePropCategory.FUTURE, MasterEntityDatePropCategory.PAST), dtm.getSecondTick().currentCategories(MasterEntity.class));
    }

    //////////////////////////// TODO ////////////////////////////
    @Override
    @Test
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered() {
	////////////////////////////TODO ////////////////////////////
    }

    @Override
    @Test
    public void test_that_Orderings_for_second_tick_can_be_changed_based_on_Usage_changes() {
	////////////////////////////TODO ////////////////////////////
    }

    @Override
    @Test
    public void test_that_usage_management_works_correctly_for_second_tick() {
	////////////////////////////TODO ////////////////////////////
    }
}
