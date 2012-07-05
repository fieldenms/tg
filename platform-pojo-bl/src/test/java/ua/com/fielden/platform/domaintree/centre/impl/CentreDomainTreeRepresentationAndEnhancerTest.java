package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentationAndEnhancerTest;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A test for entity centres tree representation.
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeRepresentationAndEnhancerTest extends AbstractDomainTreeRepresentationAndEnhancerTest {
    @Override
    protected ICentreDomainTreeManagerAndEnhancer dtm() {
	return (ICentreDomainTreeManagerAndEnhancer) super.dtm();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates root types.
     *
     * @return
     */
    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeRepresentationAndEnhancerTest() {
	final Set<Class<?>> rootTypes = createRootTypes_for_AbstractDomainTreeRepresentationAndEnhancerTest();
	rootTypes.add(EntityWithCompositeKey.class);
	rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
	rootTypes.add(MasterSyntheticEntity.class);
	return rootTypes;
    }

    /**
     * Provides a testing configuration for the manager.
     *
     * @param dtm
     */
    protected static void manageTestingDTM_for_CentreDomainTreeRepresentationAndEnhancerTest(final ICentreDomainTreeManagerAndEnhancer dtm) {
	manageTestingDTM_for_AbstractDomainTreeRepresentationAndEnhancerTest(dtm);

	dtm.getFirstTick().checkedProperties(MasterEntity.class);
	dtm.getSecondTick().checkedProperties(MasterEntity.class);
    }

    @BeforeClass
    public static void initDomainTreeTest() {
	final ICentreDomainTreeManagerAndEnhancer dtm = new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_CentreDomainTreeRepresentationAndEnhancerTest());
	manageTestingDTM_for_CentreDomainTreeRepresentationAndEnhancerTest(dtm);
	setDtmArray(serialiser().serialise(dtm));
    }

    @Override
    @Before
    public void initEachTest() throws Exception {
	super.initEachTest();
	dtm().analysisKeys(); // this method will lazily initialise "currentAnalyses" -- it is essential to fully initialise centre manager
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// End of Test initialisation ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    ////////////////////// 3. 2-nd tick disabling logic ////////////
    ////////////////////////////////////////////////////////////////

    ////////////////////// 3.0. Non-applicable properties exceptions //////////////////////

    ////////////////////// 3.2. Annotation related logic //////////////////////


    ////////////////////// 3.3. Type related logic //////////////////////

    ////////////////////// 4.3. Disabling of immutable checked properties //////////////////////
    ////////////////////// 5. Calculated properties logic //////////////////////
    @Test
    public void test_that_first_tick_for_AGGR_EXPR_calculated_properties_are_disabled() {
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "", "2 * MAX(1 * 2 * integerProp)", "Aggr expr prop 1", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp", "2 * MAX(1 * 2 * integerProp)", "Aggr expr prop 2", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.entityProp", "2 * MAX(1 * 2 * integerProp)", "Aggr expr prop 3", "Desc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dtm().getEnhancer().apply();

	assertTrue("AGGREGATED EXPRESSION calculated property [" + "aggrExprProp1" + "] should be disabled for first tick.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "aggrExprProp1"));
	assertTrue("AGGREGATED EXPRESSION calculated property [" + "aggrExprProp2" + "] should be disabled for first tick.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "aggrExprProp2"));
	assertTrue("AGGREGATED EXPRESSION calculated property [" + "aggrExprProp3" + "] should be disabled for first tick.", dtm().getRepresentation().getFirstTick().isDisabledImmutably(MasterEntity.class, "aggrExprProp3"));
    }

    @Test
    public void test_that_second_tick_for_ATTR_COLL_EXPR_calculated_properties_are_disabled() {
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection", "2 * integerProp", "Attr Coll Expr Prop1", "desc", CalculatedPropertyAttribute.ALL, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "entityProp.collection.simpleEntityProp", "2 * integerProp", "Attr Coll Expr Prop2", "desc", CalculatedPropertyAttribute.ANY, "integerProp");
	dtm().getEnhancer().addCalculatedProperty(MasterEntity.class, "collection", "2 * integerProp", "Attr Coll Expr Prop3", "desc", CalculatedPropertyAttribute.ALL, "integerProp");
	dtm().getEnhancer().apply();

	assertTrue("ATTRIBUTED COLLECTIONAL EXPRESSION calculated properties should be disabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.attrCollExprProp1"));
	assertTrue("ATTRIBUTED COLLECTIONAL EXPRESSION calculated properties should be disabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "entityProp.collection.simpleEntityProp.attrCollExprProp2"));
	assertTrue("ATTRIBUTED COLLECTIONAL EXPRESSION calculated properties should be disabled for second tick.", dtm().getRepresentation().getSecondTick().isDisabledImmutably(MasterEntity.class, "collection.attrCollExprProp3"));
    }

    @Override
    @Test
    public void test_that_serialisation_works() throws Exception {
	final ICentreDomainTreeManagerAndEnhancer dtm = dtm();
	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm));
	test_that_manager_instantiation_works_for_inner_cross_references(dtm);

	// test that serialisation works
	final byte[] array = getSerialiser().serialise(dtm);
	assertNotNull("Serialised byte array should not be null.", array);
	final ICentreDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, ICentreDomainTreeManagerAndEnhancer.class);
	// final ICriteriaDomainTreeManager copy = getSerialiser().deserialise(array, ICriteriaDomainTreeManager.class);
	// final CriteriaDomainTreeManagerAndEnhancer copy = getSerialiser().deserialise(array, CriteriaDomainTreeManagerAndEnhancer.class);
	assertNotNull("Deserialised instance should not be null.", copy);

	copy.analysisKeys(); // this method will lazily initialise "currentAnalyses" -- it is essential to fully initialise centre manager
	// after deserialisation the instance should be fully defined (even for transient fields).
	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	// So it should be checked:
	assertTrue("After deserialisation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm));
	test_that_manager_instantiation_works_for_inner_cross_references(copy);
    }

    @Override
    @Test
    public void test_that_equality_and_copying_works() {
	final ICentreDomainTreeManagerAndEnhancer dtm = dtm();
	dtm.getEnhancer().apply();
	assertTrue("After normal instantiation of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialised(dtm));

	final ICentreDomainTreeManagerAndEnhancer copy = EntityUtils.deepCopy(dtm, getSerialiser());

	copy.analysisKeys(); // this method will lazily initialise "currentAnalyses" -- it is essential to fully initialise centre manager
	// after copying the instance should be fully defined (even for transient fields).
	// for our convenience (in "Domain Trees" logic) all fields are "final" and should be not null after normal construction.
	// So it should be checked:
	assertTrue("After coping of the manager all the fields should be initialised (including transient).", allDomainTreeFieldsAreInitialisedReferenceDistinctAndEqualToCopy(copy, dtm));
	test_that_manager_instantiation_works_for_inner_cross_references(copy);
	assertTrue("The copy instance should be equal to the original instance.", EntityUtils.equalsEx(copy, dtm));
    }
}