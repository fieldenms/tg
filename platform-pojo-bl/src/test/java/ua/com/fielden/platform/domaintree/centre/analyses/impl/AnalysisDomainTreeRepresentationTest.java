package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation;

/**
 * A test for {@link AnalysisDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    @Override
    protected IAnalysisDomainTreeManagerAndEnhancer dtm() {
	return (IAnalysisDomainTreeManagerAndEnhancer) super.dtm();
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
    protected static void manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(final IAnalysisDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IAnalysisDomainTreeManagerAndEnhancer dtm = new AnalysisDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_AnalysisDomainTreeRepresentationTest());
	manageTestingDTM_for_AnalysisDomainTreeRepresentationTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
