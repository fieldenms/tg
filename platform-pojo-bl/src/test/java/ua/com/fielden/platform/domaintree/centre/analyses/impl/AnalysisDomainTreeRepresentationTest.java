package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;

/**
 * A test for {@link AnalysisDomainTreeRepresentation}.
 * 
 * @author TG Team
 * 
 */
public class AnalysisDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IAnalysisDomainTreeRepresentation dtm() {
        return (IAnalysisDomainTreeRepresentation) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(AnalysisDomainTreeRepresentationTest.class);
    }

    public static Object createDtm_for_AnalysisDomainTreeRepresentationTest() {
        return new AnalysisDomainTreeRepresentation(serialiser(), createRootTypes_for_AnalysisDomainTreeRepresentationTest());
    }

    public static Object createIrrelevantDtm_for_AnalysisDomainTreeRepresentationTest() {
        return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AnalysisDomainTreeRepresentationTest());
    }

    protected static Set<Class<?>> createRootTypes_for_AnalysisDomainTreeRepresentationTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest());
        return rootTypes;
    }

    public static void manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(final Object obj) {
        manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_AnalysisDomainTreeRepresentationTest(final Object obj) {
        performAfterDeserialisationProcess_for_AbstractAnalysisDomainTreeRepresentationTest(obj);
    }

    public static void assertInnerCrossReferences_for_AnalysisDomainTreeRepresentationTest(final Object dtm) {
        assertInnerCrossReferences_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    public static String[] fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AnalysisDomainTreeRepresentationTest() {
        return fieldWhichReferenceShouldNotBeDistictButShouldBeEqual_for_AbstractAnalysisDomainTreeRepresentationTest();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
