package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.EvenSlaverEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test for {@link SentinelDomainTreeManager}.
 * 
 * @author TG Team
 * 
 */
public class SentinelDomainTreeManagerTest extends AnalysisDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ISentinelDomainTreeManager dtm() {
        return (ISentinelDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(SentinelDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_SentinelDomainTreeManagerTest() {
        return new SentinelDomainTreeManager(serialiser(), createRootTypes_for_SentinelDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_SentinelDomainTreeManagerTest() {
        final Set<Class<?>> rootTypes = createRootTypes_for_SentinelDomainTreeManagerTest();
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

        enhanceManagerWithBasicCalculatedProperties(mgr);

        return mgr;
    }

    protected static Set<Class<?>> createRootTypes_for_SentinelDomainTreeManagerTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AnalysisDomainTreeManagerTest());
        rootTypes.remove(EvenSlaverEntity.class); // this entity has been excluded manually in parent tests
        return rootTypes;
    }

    public static void manageTestingDTM_for_SentinelDomainTreeManagerTest(final Object obj) {
        manageTestingDTM_for_AnalysisDomainTreeManagerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_SentinelDomainTreeManagerTest(final Object obj) {
        performAfterDeserialisationProcess_for_AnalysisDomainTreeManagerTest(obj);
    }

    public static void performAdditionalInitialisationProcess_for_SentinelDomainTreeManagerTest(final Object obj) {
        final SentinelDomainTreeManager mgr = (SentinelDomainTreeManager) obj;
        mgr.provideMetaStateForCountOfSelfDashboardProperty();
    }

    public static void assertInnerCrossReferences_for_SentinelDomainTreeManagerTest(final Object dtm) {
        assertInnerCrossReferences_for_AnalysisDomainTreeManagerTest(dtm);
    }

    public static String[] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_SentinelDomainTreeManagerTest() {
        return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AnalysisDomainTreeManagerTest();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void test_that_usage_management_works_correctly_for_first_tick() {
        //////////////////// Overridden to prohibit usage operation and return empty list at the beginning. ////////////////////

        // At the beginning the list of used properties should be empty.
        assertEquals("Value is incorrect.", Arrays.asList(), dtm().getFirstTick().usedProperties(MasterEntity.class));

        try {
            dtm().getFirstTick().use(MasterEntity.class, "booleanProp", true);
            fail("'Use' operation is prohibited due to automatic management of 'used' properties by 'check' operation.");
        } catch (final UnsupportedOperationException e) {
        }
    }

    @Override
    @Test
    public void test_that_usage_management_works_correctly_for_second_tick() {
        //////////////////// Overridden to prohibit usage operation and return list with only "countOfSelfDashboard" at the beginning. ////////////////////

        // At the beginning the list of used properties should consist of one special property.
        assertEquals("Value is incorrect.", Arrays.asList(SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD), dtm().getSecondTick().usedProperties(MasterEntity.class));
        assertTrue("Should be used.", dtm().getSecondTick().isUsed(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD));

        try {
            dtm().getSecondTick().use(MasterEntity.class, "booleanProp", true);
            fail("'Use' operation is prohibited due to automatic management of 'used' properties by 'check' operation.");
        } catch (final UnsupportedOperationException e) {
        }
    }

    @Override
    @Test
    public void test_that_unchecked_properties_actions_for_both_ticks_cause_exceptions_for_all_specific_logic() {
        final String message = "Unchecked property should cause IllegalArgument exception.";
        allLevelsWithoutCollections(new IAction() {
            public void action(final String name) {
                // FIRST TICK
                // usage manager
                try {
                    dtm().getFirstTick().isUsed(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
            }
        }, "uncheckedDateExprProp");
        oneLevel(new IAction() {
            public void action(final String name) {
                // SECOND TICK
                //usage manager
                try {
                    dtm().getSecondTick().isUsed(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
                // ordering
                try {
                    dtm().getSecondTick().toggleOrdering(MasterEntity.class, name);
                    fail(message);
                } catch (final IllegalArgumentException e) {
                }
            }
        }, "uncheckedAggExprProp1", "uncheckedAggExprProp2", "uncheckedAggExprProp3");
    }

    @Override
    @Test
    public void test_that_orderings_for_second_tick_are_default_and_can_be_altered() {
        // THE FIRST TIME -- returns DEFAULT VALUES //
        // Default ordering for the analysis's second tick should be empty.
        assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
        assertEquals("Value is incorrect.", Arrays.asList(), dtm().getRepresentation().getSecondTick().orderedPropertiesByDefault(MasterEntity.class));

        // Alter and check //
        dtm().getSecondTick().toggleOrdering(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
        assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD, Ordering.ASCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
        dtm().getSecondTick().toggleOrdering(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
        assertEquals("Value is incorrect.", Arrays.asList(new Pair<String, Ordering>(SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD, Ordering.DESCENDING)), dtm().getSecondTick().orderedProperties(MasterEntity.class));
        dtm().getSecondTick().toggleOrdering(MasterEntity.class, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD);
        assertEquals("Value is incorrect.", Arrays.asList(), dtm().getSecondTick().orderedProperties(MasterEntity.class));
    }

    @Override
    @Test
    public void test_that_Orderings_for_second_tick_can_be_changed_based_on_Usage_changes() {
        // the property countOfSelfDashboard is immutable checked and used (forever).
    }
}
