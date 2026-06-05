package ua.com.fielden.platform.requiredness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequiredness;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequirednessCo;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/// A test case for entity validation and error recovery related to requiredness of boolean properties.
/// It also covers a special case of mutually exclusive boolean properties, which is a practical use case.
///
/// Term "mebp" is used in test names.
/// It stands for "mutually exclusive boolean properties".
///
public class RequirednessForBooleanPropsIncludingMutuallyExclusiveTest extends AbstractDaoTestCase {

    /**
     * The only reason for this test is to ensure valid initial state for entity EntityWithDynamicRequiredness,
     * which is required by some other tests. So if a regression happens, this test would catch it.
     */
    @Test
    public void mutually_exclusive_boolean_props_are_required_initially() {
        final EntityWithDynamicRequiredness entity = new_(EntityWithDynamicRequiredness.class, "KEY VALUE");
        assertTrue(entity.getProperty("prop6").isRequired());
        entity.getProperty("prop6").isValidWithRequiredCheck(true);
        final Result resProp6 = entity.getProperty("prop6").getFirstFailure();
        assertNotNull(resProp6);
        assertEquals(EntityWithDynamicRequirednessCo.ERR_REQUIRED, resProp6.getMessage());
        assertTrue(entity.getProperty("prop7").isRequired());
        entity.getProperty("prop7").isValidWithRequiredCheck(true);
        final Result resProp7 = entity.getProperty("prop7").getFirstFailure();
        assertNotNull(resProp7);
        assertEquals(EntityWithDynamicRequirednessCo.ERR_REQUIRED, resProp7.getMessage());
        assertTrue(entity.getProperty("prop8").isRequired());
        entity.getProperty("prop8").isValidWithRequiredCheck(true);
        final Result resProp8 = entity.getProperty("prop8").getFirstFailure();
        assertNotNull(resProp8);
        assertEquals(EntityWithDynamicRequirednessCo.ERR_REQUIRED, resProp8.getMessage());
    }

    @Test
    public void mebp_only_one_can_be_true_at_a_time_and_only_that_one_is_required() {
        final EntityWithDynamicRequiredness entity = new_(EntityWithDynamicRequiredness.class, "KEY VALUE");
        entity.setProp6(true);
        assertTrue(entity.getProperty("prop6").isRequired());
        assertFalse(entity.getProperty("prop7").isRequired());
        assertFalse(entity.getProperty("prop8").isRequired());
        assertTrue(entity.isProp6());
        assertFalse(entity.isProp7());
        assertFalse(entity.isProp8());
        entity.setProp7(true);
        assertFalse(entity.getProperty("prop6").isRequired());
        assertTrue(entity.getProperty("prop7").isRequired());
        assertFalse(entity.getProperty("prop8").isRequired());
        assertFalse(entity.isProp6());
        assertTrue(entity.isProp7());
        assertFalse(entity.isProp8());
        entity.setProp8(true);
        assertFalse(entity.getProperty("prop6").isRequired());
        assertFalse(entity.getProperty("prop7").isRequired());
        assertTrue(entity.getProperty("prop8").isRequired());
        assertFalse(entity.isProp6());
        assertFalse(entity.isProp7());
        assertTrue(entity.isProp8());
    }

    @Test
    public void mebp_chaging_from_true_to_false_is_unsuccessful_property_remains_required_and_other_props_are_unaffected() {
        final EntityWithDynamicRequiredness entity = new_(EntityWithDynamicRequiredness.class, "KEY VALUE");
        final MetaProperty<Boolean> mpProp6 = entity.getProperty("prop6");
        final MetaProperty<Boolean> mpProp7 = entity.getProperty("prop7");
        final MetaProperty<Boolean> mpProp8 = entity.getProperty("prop8");

        entity.setProp6(true);
        assertTrue(mpProp6.isRequired());
        entity.setProp6(false);
        assertTrue(entity.isProp6());
        assertFalse(mpProp6.getLastAttemptedValue());
        assertFalse(entity.isProp7());
        assertFalse(entity.isProp8());
        assertTrue(mpProp6.isRequired());
        assertFalse(mpProp7.isRequired());
        assertFalse(mpProp8.isRequired());

        entity.setProp7(true);
        assertTrue(entity.isProp7());
        entity.setProp7(false);
        assertFalse(entity.isProp6());
        assertTrue(entity.isProp7());
        assertFalse(mpProp7.getLastAttemptedValue());
        assertFalse(entity.isProp8());
        assertFalse(mpProp6.isRequired());
        assertTrue(mpProp7.isRequired());
        assertFalse(mpProp8.isRequired());

        entity.setProp8(true);
        assertTrue(entity.isProp8());
        entity.setProp8(false);
        assertFalse(entity.isProp6());
        assertFalse(entity.isProp7());
        assertTrue(entity.isProp8());
        assertFalse(mpProp8.getLastAttemptedValue());
        assertFalse(mpProp6.isRequired());
        assertFalse(mpProp7.isRequired());
        assertTrue(mpProp8.isRequired());
    }

    @Test
    public void mebp_changing_true_to_false_associates_requiredness_validation_error_only_with_that_single_property() {
        final EntityWithDynamicRequiredness entity = co$(EntityWithDynamicRequiredness.class).findByKeyAndFetch(EntityWithDynamicRequirednessCo.FETCH_PROVIDER.fetchModel(), "EWR-01");
        final MetaProperty<Boolean> mpProp6 = entity.getProperty("prop6");
        final MetaProperty<Boolean> mpProp7 = entity.getProperty("prop7");
        final MetaProperty<Boolean> mpProp8 = entity.getProperty("prop8");

        assertTrue(entity.isProp6());
        entity.setProp6(false);
        assertFalse(entity.isValid().isSuccessful());
        assertNotNull(mpProp6.getFirstFailure());
        assertNull(mpProp7.getFirstFailure());
        assertNull(mpProp8.getFirstFailure());

        entity.setProp7(true);
        assertTrue(entity.isValid().isSuccessful());        
        entity.setProp7(false);
        assertFalse(entity.isValid().isSuccessful());
        assertNull(mpProp6.getFirstFailure());
        assertNotNull(mpProp7.getFirstFailure());
        assertNull(mpProp8.getFirstFailure());

        entity.setProp8(true);
        assertTrue(entity.isValid().isSuccessful());        
        entity.setProp8(false);
        assertFalse(entity.isValid().isSuccessful());
        assertNull(mpProp6.getFirstFailure());
        assertNull(mpProp7.getFirstFailure());
        assertNotNull(mpProp8.getFirstFailure());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        if (useSavedDataPopulationScript()) {
            return;
        }
        
        save(new_(EntityWithDynamicRequiredness.class, "EWR-01").setProp1(10).setProp6(true));
    }

}