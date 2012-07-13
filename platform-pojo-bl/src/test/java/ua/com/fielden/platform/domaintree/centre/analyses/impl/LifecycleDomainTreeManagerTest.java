package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
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
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeManagerTest());
	enhanceManagerWithBasicCalculatedProperties(dtm);
	return dtm;
    }

    protected static Set<Class<?>> createRootTypes_for_LifecycleDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeManagerTest());
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
    public void test_that_LifecycleProperty_can_be_set_and_altered() {
	// THE FIRST TIME -- returns DEFAULT VALUE //
	// default value should be NULL
	assertNull("The default LifecycleProperty should be Null.", dtm().getLifecycleProperty());

	// Alter and check //
	assertTrue("The first tick reference should be the same.", dtm() == dtm().setLifecycleProperty(new Pair<Class<?>, String>(MasterEntity.class, "simpleEntityProp")));
	assertEquals("The LifecycleProperty should be 'simpleEntityProp' from 'MasterEntity' class.", new Pair<Class<?>, String>(MasterEntity.class, "simpleEntityProp"), dtm().getLifecycleProperty());

	try {
	    dtm().setLifecycleProperty(new Pair<Class<?>, String>(MasterEntity.class, "booleanProp"));
	    fail("Non-lifecycle property should cause exception when someone is trying to set it as Lifecycle property for the manager.");
	} catch (final IllegalArgumentException e) {
	}
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
}
