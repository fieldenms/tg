package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;

/**
 * A test for {@link LifecycleDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    @Override
    protected ILifecycleDomainTreeManager dtm() {
	return (ILifecycleDomainTreeManager) ((ICentreDomainTreeManagerAndEnhancer) just_a_dtm()).getAnalysisManager("Report");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_LifecycleDomainTreeRepresentationTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractAnalysisDomainTreeRepresentationTest();
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_LifecycleDomainTreeRepresentationTest(final ILifecycleDomainTreeManager dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final ICentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeRepresentationTest());
	centre.initAnalysisManagerByDefault("Report", AnalysisType.LIFECYCLE);
	final ILifecycleDomainTreeManager dtm = (ILifecycleDomainTreeManager) centre.getAnalysisManager("Report");
	manageTestingDTM_for_LifecycleDomainTreeRepresentationTest(dtm);
	centre.acceptAnalysisManager("Report");
	setDtmArray(serialiser().serialise(centre));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
