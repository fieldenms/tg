package ua.com.fielden.platform.domaintree.master.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeTest;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;


/**
 * A test for {@link MasterDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class MasterDomainTreeManagerTest extends AbstractDomainTreeTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected IMasterDomainTreeManager dtm() {
	return (IMasterDomainTreeManager) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
	initialiseDomainTreeTest(MasterDomainTreeManagerTest.class);
    }

    public static Object createDtm_for_MasterDomainTreeManagerTest() {
	return new MasterDomainTreeManager(serialiser(), createRootTypes_for_MasterDomainTreeManagerTest());
    }

    public static Object createIrrelevantDtm_for_MasterDomainTreeManagerTest() {
	return null;
    }

    protected static Set<Class<?>> createRootTypes_for_MasterDomainTreeManagerTest() {
	final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeTest());
	return rootTypes;
    }

    public static void manageTestingDTM_for_MasterDomainTreeManagerTest(final Object obj) {
    }

    public static void performAfterDeserialisationProcess_for_MasterDomainTreeManagerTest(final Object obj) {
    }

    public static void assertInnerCrossReferences_for_MasterDomainTreeManagerTest(final Object obj) {
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_that_locator_actions_cause_exceptions_for_NON_ENTITY_types_of_properties() {
	final String message = "Non-AE property should cause IllegalArgument exception for locator-related logic.";
	allLevels(new IAction() {
	    public void action(final String name) {
		illegalAllLocatorActions(dtm(), message, name);
	    }
	}, "integerProp", "moneyProp", "booleanProp");
    }
}
