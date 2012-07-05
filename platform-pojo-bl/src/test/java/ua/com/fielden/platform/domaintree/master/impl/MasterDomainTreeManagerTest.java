package ua.com.fielden.platform.domaintree.master.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeTest;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.utils.EntityUtils;


/**
 * A test for {@link MasterDomainTreeManager}.
 *
 * @author TG Team
 *
 */
public class MasterDomainTreeManagerTest extends AbstractDomainTreeTest {
    /**
     * Returns a testing manager. Can be overridden to return specific manager for specific descendant test.
     *
     * @return
     */
    @Override
    protected IMasterDomainTreeManager dtm() {
	return (IMasterDomainTreeManager) super.dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final IMasterDomainTreeManager mdtm = new MasterDomainTreeManager(serialiser(), createRootTypes_for_AbstractDomainTreeTest());
	setDtmArray(serialiser().serialise(mdtm));
    }

    @Test
    public void test_that_locator_actions_cause_exceptions_for_NON_ENTITY_types_of_properties() {
	final String message = "Non-AE property should cause IllegalArgument exception for locator-related logic.";
	allLevels(new IAction() {
	    public void action(final String name) {
		illegalAllLocatorActions(dtm(), message, name);
	    }
	}, "integerProp", "moneyProp", "booleanProp");
    }

    @Override
    @Test
    public void test_that_serialisation_works() throws Exception {
	final IMasterDomainTreeManager mdtm = dtm();
	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(mdtm));

	// test that serialisation works
	final byte[] array = getSerialiser().serialise(mdtm);
	assertNotNull("Serialised byte array should not be null.", array);
	final IMasterDomainTreeManager copy = getSerialiser().deserialise(array, IMasterDomainTreeManager.class);
	// final ICriteriaDomainTreeManager copy = getSerialiser().deserialise(array, ICriteriaDomainTreeManager.class);
	// final CriteriaDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, CriteriaDomainTreeManagerAndEnhancer.class);
	assertNotNull("Deserialised instance should not be null.", copy);

	// after deserialisation the instance should be fully defined (even for transient fields).
	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	// So it should be checked:
	assertTrue("After deserialisation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, mdtm));
    }

    @Override
    @Test
    public void test_that_equality_and_copying_works() {
	final IMasterDomainTreeManager mdtm = dtm();
	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(mdtm));

	final IMasterDomainTreeManager copy = EntityUtils.deepCopy(mdtm, getSerialiser());
	// after copying the instance should be fully defined (even for transient fields).
	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	// So it should be checked:
	assertTrue("After coping of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, mdtm));
	assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(copy, mdtm));
    }
}
