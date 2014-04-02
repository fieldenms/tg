package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;

/**
 * A test for {@link CentreDomainTreeManager}.
 * 
 * @author TG Team
 * 
 */
public class LocatorDomainTreeManagerTest extends CentreDomainTreeManagerTest {
    //    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    //    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //    @Override
    //    protected ILocatorDomainTreeManagerAndEnhancer dtm() {
    //	return (ILocatorDomainTreeManagerAndEnhancer) super.dtm();
    //    }
    //
    //    /**
    //     * Creates root types.
    //     *
    //     * @return
    //     */
    //    protected static Set<Class<?>> createRootTypes_for_LocatorDomainTreeManagerTest() {
    //	final Set<Class<?>> rootTypes = createRootTypes_for_CentreDomainTreeManagerTest();
    //	return rootTypes;
    //    }
    //
    //    /**
    //     * Provides a testing configuration for the manager.
    //     *
    //     * @param dtm
    //     */
    //    protected static void manageTestingDTM_for_LocatorDomainTreeManagerTest(final ILocatorDomainTreeManager dtm) {
    //	manageTestingDTM_for_CentreDomainTreeManagerTest(dtm);
    //
    //	dtm.getFirstTick().checkedProperties(MasterEntity.class);
    //	dtm.getSecondTick().checkedProperties(MasterEntity.class);
    //    }
    //
    //    @BeforeClass
    //    public static void initDomainTreeTest() {
    //	final ILocatorDomainTreeManagerAndEnhancer dtm = new LocatorDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LocatorDomainTreeManagerTest());
    //	manageTestingDTM_for_LocatorDomainTreeManagerTest(dtm);
    //	setDtmArray(serialiser().serialise(dtm));
    //    }
    //
    //    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    //    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ILocatorDomainTreeManager dtm() {
        return (ILocatorDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(LocatorDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_LocatorDomainTreeManagerTest() {
        return new LocatorDomainTreeManager(serialiser(), createRootTypes_for_LocatorDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_LocatorDomainTreeManagerTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_LocatorDomainTreeManagerTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_CentreDomainTreeManagerTest());
        return rootTypes;
    }

    public static void manageTestingDTM_for_LocatorDomainTreeManagerTest(final Object obj) {
        manageTestingDTM_for_CentreDomainTreeManagerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_LocatorDomainTreeManagerTest(final Object obj) {
    }

    public static void assertInnerCrossReferences_for_LocatorDomainTreeManagerTest(final Object dtm) {
        assertInnerCrossReferences_for_CentreDomainTreeManagerTest(dtm);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_UseForAutocompletion_can_be_set_and_altered() {
        // THE FIRST TIME -- returns DEFAULT VALUE //
        // default value should be FALSE
        assertFalse("UseForAutocompletion by default should be FALSE", dtm().isUseForAutocompletion());

        // Alter and check //
        assertTrue("The manager reference should be the same.", dtm() == dtm().setUseForAutocompletion(true));
        assertTrue("UseForAutocompletion should be true.", dtm().isUseForAutocompletion());
    }

    @Test
    public void test_that_SearchBy_can_be_set_and_altered() {
        // THE FIRST TIME -- returns DEFAULT VALUE //
        // default value should be SearchBy.KEY
        assertEquals("SearchBy by default should be SearchBy.KEY.", SearchBy.KEY, dtm().getSearchBy());

        // Alter and check //
        assertTrue("The manager reference should be the same.", dtm() == dtm().setSearchBy(SearchBy.DESC_AND_KEY));
        assertEquals("SearchBy should be SearchBy.DESC_AND_KEY.", SearchBy.DESC_AND_KEY, dtm().getSearchBy());
    }

    @Override
    public void test_that_runAutomatically_can_be_set_and_altered() {
        // THE FIRST TIME -- returns DEFAULT VALUE //
        // default value should be TRUE (overridden from CriteriaDTM behaviour)
        assertTrue("Run automatically by default should be TRUE.", dtm().isRunAutomatically());

        // Alter and check //
        assertTrue("The manager reference should be the same", dtm() == dtm().setRunAutomatically(false));
        assertFalse("Run automatically should be FALSE.", dtm().isRunAutomatically());
    }

    @Override
    public void test_that_column_number_for_first_tick_can_be_set_and_altered() {
        // THE FIRST TIME -- returns DEFAULT VALUE //
        // default value should be 1 (overridden from CriteriaDTM behaviour)
        assertEquals("The default column number should be 1.", dtm().getFirstTick().getColumnsNumber(), 1);

        // Alter and check //
        assertTrue("The first tick reference should be the same.", dtm().getFirstTick() == dtm().getFirstTick().setColumnsNumber(3));
        assertEquals("The number of columns should be 3", dtm().getFirstTick().getColumnsNumber(), 3);
    }

    @Override
    public void test_that_CHECK_state_for_mutated_by_isChecked_method_properties_is_desired_and_after_manual_mutation_is_actually_mutated() {
        // this test is redundant due to lack of special isChecked logic in CriteriaDomainTreeManager
    }
}
