package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.util.Set;

import org.junit.BeforeClass;

import ua.com.fielden.platform.treemodel.rules.criteria.analyses.ILifecycleDomainTreeManager.ILifecycleDomainTreeManagerAndEnhancer;

/**
 * A test for {@link LifecycleDomainTreeRepresentation}.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeRepresentationTest extends AbstractAnalysisDomainTreeRepresentationTest {
    @Override
    protected ILifecycleDomainTreeManagerAndEnhancer dtm() {
	return (ILifecycleDomainTreeManagerAndEnhancer) super.dtm();
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
    protected static void manageTestingDTM_for_LifecycleDomainTreeRepresentationTest(final ILifecycleDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractAnalysisDomainTreeRepresentationTest(dtm);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final ILifecycleDomainTreeManagerAndEnhancer dtm = new LifecycleDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_LifecycleDomainTreeRepresentationTest());
	manageTestingDTM_for_LifecycleDomainTreeRepresentationTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
