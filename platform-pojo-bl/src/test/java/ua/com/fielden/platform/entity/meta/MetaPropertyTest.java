package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.entities.EntityWithBce;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 *
 * This test case is complementary to AbstractEntityTest covering mainly meta-property functionality. A large number of test in AbstractEntityTest also pertain to meta-property
 * functionality.
 *
 * @author TG Team
 *
 */
public class MetaPropertyTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private EntityWithBce entity;

    @Before
    public void setUp() {
        entity = factory.newEntity(EntityWithBce.class, "key", "description"); // this ensures all listeners are removed
    }

    @Test
    public void not_assigned_properties_in_newly_instantiated_entities_are_dirty_and_not_marked_as_assigned() {
        assertTrue(entity.getProperty("propWithBce").isDirty());
        assertFalse(entity.getProperty("propWithBce").isAssigned());
        assertTrue("Property key was assigned by entity factory and thus should be recognised as assigned.", entity.getProperty("key").isAssigned());
    }

    @Test
    public void original_value_for_property_with_default_value_is_null_as_this_is_critical_for_persisting_new_entities() {
        assertNotNull(entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void new_created_entities_have_original_values_of_their_properties_equal_to_null_regardless_the_number_of_changes() {
        entity.setPropWithBce("some other value");
        assertEquals("some other value", entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("yet another value");
        assertEquals("yet another value", entity.getProperty("propWithBce").getValue());
        assertNull("Original value should remain null until entity gets persisted.", entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void fact_of_property_assignment_is_reflected_in_meta_property() {
        entity.setPropWithBce("some other value");
        assertTrue(entity.getProperty("propWithBce").isAssigned());
    }

    @Test
    public void valid_property_assignment_updates_last_attempted_value() {
        assertNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        entity.setPropWithBce("some other value");
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNull(entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void invalid_property_changes_does_not_affect_original_property() {
        entity.setPropWithBce("failure");
        assertEquals("default value", entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void invalid_property_changes_produces_last_invalid_value_information() {
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("failure");
        assertEquals("default value", entity.getProperty("propWithBce").getValue());
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNotNull(entity.getProperty("propWithBce").getLastInvalidValue());
        assertEquals(entity.getProperty("propWithBce").getLastAttemptedValue(), entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void resetting_invalid_property_changes_nulls_out_last_invalid_value_information() {
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("failure");
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNotNull(entity.getProperty("propWithBce").getLastInvalidValue());

        entity.setPropWithBce("good value");
        assertEquals("good value", entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNull(entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void is_changed_from_original_is_true_for_props_with_default_value_in_new_entities() {
        assertTrue(entity.getProperty("propWithBce").isChangedFromOriginal());
    }

    @Test
    public void is_changed_from_original_is_false_for_props_without_default_value_in_new_entities() {
        assertFalse(entity.getProperty("property2").isChangedFromOriginal());
    }

    @Test
    public void is_changed_from_original_is_true_after_prop_changes_for_new_entities() {
        entity.setProperty2("new value");
        assertTrue(entity.getProperty("property2").isChangedFromOriginal());
    }

    @Test
    public void required_property_with_custom_error_msg_uses_it_in_validation_results() {
        entity.setPropRequired(13);
        entity.setPropRequired(null);
        assertFalse(entity.getProperty("propRequired").isValid());
        assertEquals(Finder.findFieldByName(entity.getType(), "propRequired").getAnnotation(Required.class).value(), entity.getProperty("propRequired").getFirstFailure().getMessage());
    }

    @Test
    public void validationResult_returns_validation_error_if_property_validation_failed() {
        entity.setPropRequired(13);
        entity.setPropRequired(null);
        assertFalse(entity.getProperty("propRequired").isValid());
        
        final Result result = entity.getProperty("propRequired").validationResult();
        assertFalse(result.isSuccessful());
        assertFalse(result instanceof Warning);
        
        final String validationErrMsg = Finder.findFieldByName(entity.getType(), "propRequired").getAnnotation(Required.class).value();
        assertEquals(validationErrMsg, result.getMessage());
    }
    
    @Test
    public void validationResult_returns_successful_result_if_property_validation_succeeded() {
        entity.setPropRequired(13);
        assertTrue(entity.getProperty("propRequired").isValid());
        
        final Result result = entity.getProperty("propRequired").validationResult();
        assertTrue(result.isSuccessful());
        assertFalse(result instanceof Warning);
    }
    
    @Test
    public void validationResult_returns_warning_result_if_validation_completed_with_warning() {
        entity.setPropRequired(113);
        assertTrue(entity.getProperty("propRequired").isValid());
        
        final Result result = entity.getProperty("propRequired").validationResult();
        assertTrue(result.isSuccessful());
        assertTrue(result instanceof Warning);
    }

    @Test
    public void validationResult_returns_successful_result_if_no_validation_took_place() {
        assertTrue(entity.getProperty("propRequired").isValid());
        
        final Result result = entity.getProperty("propRequired").validationResult();
        assertTrue(result.isSuccessful());
        assertFalse(result instanceof Warning);
    }

    @Test
    public void setting_property_to_the_same_value_forcibly_does_not_affect_its_prev_value() {
        assertNull(entity.getProperty("property2").getPrevValue());
        entity.setProperty2("value 1");
        assertNull(entity.getProperty("property2").getPrevValue());
        entity.setProperty2("value 2");
        assertEquals("value 1", entity.getProperty("property2").getPrevValue());
       
        // set the same value forcibly
        entity.getProperty("property2").setValue("value 2", true);
        assertEquals("value 1", entity.getProperty("property2").getPrevValue());
    }

    @Test
    public void setting_prev_value_for_property_to_the_same_value_as_current_explicitly_does_not_change_prev_value() {
        assertEquals(0, entity.getProperty("property2").getValueChangeCount());
        entity.setProperty2("value 1");
        assertNull(entity.getProperty("property2").getPrevValue());
        assertEquals(1, entity.getProperty("property2").getValueChangeCount());
        entity.setProperty2("value 2");
        assertEquals("value 1", entity.getProperty("property2").getPrevValue());
        assertEquals(2, entity.getProperty("property2").getValueChangeCount());
        
        entity.getProperty("property2").setPrevValue(entity.getProperty2());
        assertEquals("value 1", entity.getProperty("property2").getPrevValue());
        assertEquals(2, entity.getProperty("property2").getValueChangeCount()); 
    }

}
