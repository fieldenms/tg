package ua.com.fielden.platform.domaintree.centre.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;

/**
 * A test for entity locators tree representation.
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeRepresentationTest extends CentreDomainTreeRepresentationTest {
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
//    protected static Set<Class<?>> createRootTypes_for_LocatorDomainTreeRepresentationTest() {
//	final Set<Class<?>> rootTypes = createRootTypes_for_CentreDomainTreeRepresentationTest();
//	return rootTypes;
//    }
//
//    /**
//     * Provides a testing configuration for the manager.
//     *
//     * @param dtm
//     */
//    protected static void manageTestingDTM_for_LocatorDomainTreeRepresentationTest(final ILocatorDomainTreeManagerAndEnhancer dtm) {
//	manageTestingDTM_for_CentreDomainTreeRepresentationTest(dtm);
//
//	dtm.getFirstTick().checkedProperties(MasterEntity.class);
//	dtm.getSecondTick().checkedProperties(MasterEntity.class);
//    }
//
//    @BeforeClass
//    public static void initDomainTreeTest() {
//	final ILocatorDomainTreeManagerAndEnhancer dtm = new LocatorDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LocatorDomainTreeRepresentationTest());
//	manageTestingDTM_for_LocatorDomainTreeRepresentationTest(dtm);
//	setDtmArray(serialiser().serialise(dtm));
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ILocatorDomainTreeRepresentation dtm() {
	return (ILocatorDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(LocatorDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_LocatorDomainTreeRepresentationTest() {
	return new LocatorDomainTreeRepresentation(serialiser(), createRootTypes_for_LocatorDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_LocatorDomainTreeRepresentationTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_LocatorDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_CentreDomainTreeRepresentationTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_LocatorDomainTreeRepresentationTest(final Object obj) {
	manageTestingDTM_for_CentreDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_LocatorDomainTreeRepresentationTest(final Object dtr) {
    }

    public static void assertInnerCrossReferences_for_LocatorDomainTreeRepresentationTest(final Object dtm) {
	assertInnerCrossReferences_for_CentreDomainTreeRepresentationTest(dtm);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
