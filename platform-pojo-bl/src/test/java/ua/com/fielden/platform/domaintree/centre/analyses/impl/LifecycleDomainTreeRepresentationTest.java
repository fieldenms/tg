package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;

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
	return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeRepresentationTest());
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
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
