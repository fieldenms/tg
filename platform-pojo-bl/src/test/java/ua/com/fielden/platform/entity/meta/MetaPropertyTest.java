package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.entities.EntityWithBce;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

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
    public void not_assigned_properties_in_newly_instantiated_entities_should_be_dirty_and_not_marked_as_assigned() {
        assertTrue(entity.getProperty("propWithBce").isDirty());
        assertFalse(entity.getProperty("propWithBce").isAssigned());
        assertTrue("Property key was assigned by entity factory and thus should be recognised as assigned.", entity.getProperty("key").isAssigned());
    }

    @Test
    public void original_value_for_property_with_default_should_be_null_none_the_less() {
        assertNotNull(entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void new_created_entities_shoul_have_original_values_of_their_properties_equal_to_null_regardless_the_number_of_changes() {
        entity.setPropWithBce("some other value");
        assertEquals("some other value", entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("yet another value");
        assertEquals("yet another value", entity.getProperty("propWithBce").getValue());
        assertNull("Original value should remain null until entity gets persisted.", entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void fact_of_property_assignment_should_be_reflectd_in_meta_property() {
        entity.setPropWithBce("some other value");
        assertTrue(entity.getProperty("propWithBce").isAssigned());
    }

    @Test
    public void valid_property_assignment_should_update_last_attempted_value() {
        assertNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        entity.setPropWithBce("some other value");
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNull(entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void invalid_property_changes_should_not_affect_original_property() {
        entity.setPropWithBce("failure");
        assertEquals("default value", entity.getProperty("propWithBce").getValue());
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
    }

    @Test
    public void invalid_property_changes_should_produce_last_invalid_value_information() {
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("failure");
        assertEquals("default value", entity.getProperty("propWithBce").getValue());
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNotNull(entity.getProperty("propWithBce").getLastInvalidValue());
        assertEquals(entity.getProperty("propWithBce").getLastAttemptedValue(), entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void resetting_invalid_property_changes_should_null_out_last_invalid_value_information() {
        assertNull(entity.getProperty("propWithBce").getOriginalValue());
        entity.setPropWithBce("failure");
        assertNotNull(entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNotNull(entity.getProperty("propWithBce").getLastInvalidValue());

        entity.setPropWithBce("good value");
        assertEquals("good value", entity.getProperty("propWithBce").getLastAttemptedValue());
        assertNull(entity.getProperty("propWithBce").getLastInvalidValue());
    }

    @Test
    public void is_changed_from_original_shoud_be_true_for_props_with_default_value_in_new_entities() {
        assertTrue(entity.getProperty("propWithBce").isChangedFromOriginal());
    }

    @Test
    public void is_changed_from_original_shoud_be_false_for_props_without_default_value_in_new_entities() {
        assertFalse(entity.getProperty("property2").isChangedFromOriginal());
    }

    @Test
    public void is_changed_from_original_shoud_be_true_after_prop_changes_in_new_entities2() {
        entity.setProperty2("new value");
        assertTrue(entity.getProperty("property2").isChangedFromOriginal());
    }

    @Test
    public void required_property_with_custom_error_msg_should_use_it_in_validation_result() {
        entity.setPropRequired(13);
        entity.setPropRequired(null);
        assertFalse(entity.getProperty("propRequired").isValid());
        assertEquals(Finder.findFieldByName(entity.getType(), "propRequired").getAnnotation(Required.class).value(), entity.getProperty("propRequired").getFirstFailure().getMessage());
    }

}
