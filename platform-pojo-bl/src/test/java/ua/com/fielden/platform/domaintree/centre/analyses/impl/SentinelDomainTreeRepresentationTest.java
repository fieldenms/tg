package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;

/**
 * A test for {@link SentinelDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class SentinelDomainTreeRepresentationTest extends AnalysisDomainTreeRepresentationTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ISentinelDomainTreeRepresentation dtm() {
	return (ISentinelDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(SentinelDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_SentinelDomainTreeRepresentationTest() {
	return new SentinelDomainTreeRepresentation(serialiser(), createRootTypes_for_SentinelDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_SentinelDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_SentinelDomainTreeRepresentationTest();
	final CentreDomainTreeManagerAndEnhancer mgr = new CentreDomainTreeManagerAndEnhancer(serialiser(), rootTypes);
	mgr.provideSentinelAnalysesAggregationProperty(rootTypes);
	// provide sentinel properties to test exclusion logic
	mgr.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "case When integerProp > 10 thEn \"GreEn\" wheN integerProp > 5 thEn \"yelloW\" else \"red\" end", "Sentinel 1", "Desc", CalculatedPropertyAttribute.NO_ATTR, null);
	mgr.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "case When MONTH(dateProp) > 10 thEn \"REd\" else \"green\" end", "Sentinel 2", "Desc", CalculatedPropertyAttribute.NO_ATTR, null);
	// incorrectly formed sentinel -- no correct string values (neither "green" nor "red")
	mgr.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "case When MONTH(dateProp) > 10 thEn \"Unknown1\" else \"unknown2\" end", "Pseudo sentinel 1", "Desc", CalculatedPropertyAttribute.NO_ATTR, null);
	// incorrectly formed sentinel -- not string property at all
	mgr.getEnhancer().addCalculatedProperty(MasterEntity.class, "", "case When MONTH(dateProp) > 10 thEn \"1\" else \"2\" end", "Pseudo sentinel 2", "Desc", CalculatedPropertyAttribute.NO_ATTR, null);
	mgr.getEnhancer().apply();

	return mgr;
    }

    protected static Set<Class<?>> createRootTypes_for_SentinelDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AnalysisDomainTreeRepresentationTest());
	rootTypes.remove(EvenSlaverEntity.class); // this entity has been excluded manually in parent tests
	return rootTypes;
    }

    public static void manageTestingDTM_for_SentinelDomainTreeRepresentationTest(final Object obj) {
	manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_SentinelDomainTreeRepresentationTest(final Object obj) {
	performAfterDeserialisationProcess_for_AnalysisDomainTreeRepresentationTest(obj);
    }

    public static void performAdditionalInitialisationProcess_for_SentinelDomainTreeRepresentationTest(final Object obj) {
	final SentinelDomainTreeRepresentation repr = (SentinelDomainTreeRepresentation) obj;
	repr.provideMetaStateForCountOfSelfDashboardProperty();
    }

    public static void assertInnerCrossReferences_for_SentinelDomainTreeRepresentationTest(final Object dtm) {
	assertInnerCrossReferences_for_AnalysisDomainTreeRepresentationTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_SentinelDomainTreeRepresentationTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AnalysisDomainTreeRepresentationTest();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Test
    public void test_that_collections_itself_and_their_children_are_disabled() {
	// collections (and children) should be excluded
    }

    @Override
    @Test
    public void test_that_any_property_has_type_related_functions() {
	// all of the properties in base method are excluded here
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////// EXCLUDING ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_entity_itself_is_NOT_excluded() {
	assertFalse("Entity itself should be NOT excluded.", dtm().isExcludedImmutably(MasterEntity.class, ""));
    }

    @Test
    public void test_that_CountOfSelfDashboard_property_is_NOT_excluded() {
	assertFalse("countOfSelfDashboard property should be NOT excluded.", dtm().isExcludedImmutably(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD));
    }

    @Test
    public void test_that_CountOfSelfDashboard_property_is_disabled_for_both_ticks() {
	assertTrue("countOfSelfDashboard property should be disabled.", dtm().getFirstTick().isDisabledImmutably(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD));
	assertTrue("countOfSelfDashboard property should be disabled.", dtm().getSecondTick().isDisabledImmutably(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD));
    }

    @Test
    public void test_that_sentinels_are_NOT_excluded() {
	assertFalse("Sentinel property should be NOT excluded.", dtm().isExcludedImmutably(MasterEntity.class, "sentinel1"));
	assertFalse("Sentinel property should be NOT excluded.", dtm().isExcludedImmutably(MasterEntity.class, "sentinel2"));
    }

    @Test
    public void test_that_all_other_properties_are_excluded() {
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "dateProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "integerProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "entityProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "entityProp.dateProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "entityProp.integerProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "collection"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "collection.dateProp"));
	assertTrue("Property that is not 'sentinel' and not 'countOfSelfDashboard' should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "collection.integerProp"));

	assertTrue("'Pseudo' sentinel property should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "pseudoSentinel1"));
	assertTrue("'Pseudo' sentinel property should be excluded.", dtm().isExcludedImmutably(MasterEntity.class, "pseudoSentinel2"));
    }

    // Following tests are irrelevant because "excluding" rules have been completely changed. See the above tests.
    @Override
    @Test
    public void test_that_Desc_properties_without_DescTitle_on_parent_type_are_excluded_and_otherwise_NOT_excluded() {
    }

    @Override
    @Test
    public void test_that_Key_properties_with_AE_KeyType_and_with_KeyTitle_on_parent_type_are_NOT_excluded() {
    }

    @Override
    @Test
    public void test_that_collections_itself_are_NOT_excluded() {
    }

    @Override
    @Test
    public void test_that_NOT_link_properties_are_NOT_excluded() {
    }

    @Override
    @Test
    public void test_that_order_of_included_properties_is_correct_and_circular_references_manage_Dummy_property() {
    }

    @Override
    @Test
    public void test_that_warming_up_of_included_properties_works_correctly() {
    }

    @Override
    @Test
    public void test_that_included_properties_for_union_entities_hierarchy_are_correct_and_manage_Common_and_Union_properties() {
    }

    @Override
    @Test
    public void test_that_manual_exclusion_is_correctly_reflected_in_Included_properties() {
    }

    @Override
    @Test
    public void test_that_second_tick_for_calculated_properties_of_AGGREGATED_EXPRESSION_type_are_NOT_disabled() {
	// they all are excluded
    }
}
