package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;

/**
 * A test for {@link AnalysisDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    @Override
    protected IAnalysisDomainTreeManager dtm() {
	return (IAnalysisDomainTreeManager) ((ICentreDomainTreeManagerAndEnhancer) just_a_dtm()).getAnalysisManager("Report");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_AnalysisDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest();
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(final IAnalysisDomainTreeManager dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final ICentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AnalysisDomainTreeRepresentationTest());
	centre.initAnalysisManagerByDefault("Report", AnalysisType.SIMPLE);
	final IAnalysisDomainTreeManager dtm = (IAnalysisDomainTreeManager) centre.getAnalysisManager("Report");
	manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(dtm);
	centre.acceptAnalysisManager("Report");
	setDtmArray(serialiser().serialise(centre));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
