package ua.com.fielden.platform.domaintree.centre.impl;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentationAndEnhancerTest;
import ua.com.fielden.platform.domaintree.testing.EntityWithCompositeKey;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterSyntheticEntity;

/**
 * A test for entity centres tree representation.
 * 
 * @author TG Team
 * 
 */
public class CentreDomainTreeRepresentationAndEnhancerTest extends AbstractDomainTreeRepresentationAndEnhancerTest {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Test initialisation ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ICentreDomainTreeManagerAndEnhancer dtm() {
        return (ICentreDomainTreeManagerAndEnhancer) just_a_dtm();
    }

    @BeforeClass
    public static void initDomainTreeTest() throws Exception {
        initialiseDomainTreeTest(CentreDomainTreeRepresentationAndEnhancerTest.class);
    }

    public static Object createDtm_for_CentreDomainTreeRepresentationAndEnhancerTest() {
        return new CentreDomainTreeManagerAndEnhancer(serialiser(), createRootTypes_for_CentreDomainTreeRepresentationAndEnhancerTest());
    }

    public static Object createIrrelevantDtm_for_CentreDomainTreeRepresentationAndEnhancerTest() {
        return null;
    }

    protected static Set<Class<?>> createRootTypes_for_CentreDomainTreeRepresentationAndEnhancerTest() {
        final Set<Class<?>> rootTypes = new HashSet<Class<?>>(createRootTypes_for_AbstractDomainTreeRepresentationAndEnhancerTest());
        rootTypes.add(EntityWithCompositeKey.class);
        rootTypes.add(EntityWithKeyTitleAndWithAEKeyType.class);
        rootTypes.add(MasterSyntheticEntity.class);
        return rootTypes;
    }

    public static void manageTestingDTM_for_CentreDomainTreeRepresentationAndEnhancerTest(final Object obj) {
        manageTestingDTM_for_AbstractDomainTreeRepresentationAndEnhancerTest(obj);
    }

    public static void performAfterDeserialisationProcess_for_CentreDomainTreeRepresentationAndEnhancerTest(final Object obj) {
        final ICentreDomainTreeManagerAndEnhancer dtmae = (ICentreDomainTreeManagerAndEnhancer) obj;
        dtmae.analysisKeys(); // this method will lazily initialise "currentAnalyses" -- it is essential to fully initialise centre manager
    }

    public static void assertInnerCrossReferences_for_CentreDomainTreeRepresentationAndEnhancerTest(final Object obj) {
        assertInnerCrossReferences_for_AbstractDomainTreeRepresentationAndEnhancerTest(obj);
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
}