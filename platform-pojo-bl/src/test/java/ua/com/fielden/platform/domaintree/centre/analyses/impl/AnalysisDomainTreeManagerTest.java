package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;

/**
 * A test for {@link AnalysisDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeManagerTest extends AbstractAnalysisDomainTreeManagerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IAnalysisDomainTreeManager dtm() {
	return (IAnalysisDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(AnalysisDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_AnalysisDomainTreeManagerTest() {
	return new AnalysisDomainTreeManager(serialiser(), createRootTypes_for_AnalysisDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_AnalysisDomainTreeManagerTest() {
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AnalysisDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(dtm);
	return dtm;
    }

    protected static Set<Class<?>> createRootTypes_for_AnalysisDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_AnalysisDomainTreeManagerTest(final Object obj) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_AnalysisDomainTreeManagerTest(final Object obj) {
	performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeManagerTest(obj);
    }

    public static void assertInnerCrossReferences_for_AnalysisDomainTreeManagerTest(final Object dtm) {
	assertInnerCrossReferences_for_AbstractAnalysisDomainTreeManagerTest(dtm);
    }

    public static String [] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AnalysisDomainTreeManagerTest() {
	return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeManagerTest();
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
    public void test_that_VisibleDistributedValuesNumber_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be 0
	assertEquals("The default VisibleDistributedValuesNumber should be 0.", dtm().getVisibleDistributedValuesNumber(), 0);

	// Alter and check //
	assertTrue("The first tick reference should be the same", dtm() == dtm().setVisibleDistributedValuesNumber(3));
	assertEquals("The VisibleDistributedValuesNumber should be 3.", 3, dtm().getVisibleDistributedValuesNumber());
    }

    @Override
    public void test_that_PropertyUsageListeners_work() {
    }

    @Override
    public void test_that_PropertyOrderingListeners_work() {
    }
}
