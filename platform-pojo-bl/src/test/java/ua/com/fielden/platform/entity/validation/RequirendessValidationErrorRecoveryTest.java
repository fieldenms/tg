package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequiredness;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case for entity validation and error recovery related to requiredness of properties, including property dependency handling.
 *
 * @author TG Team
 *
 */
public class RequirendessValidationErrorRecoveryTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void prop_with_no_value_becomes_required_and_should_remain_valid() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        // preconditions
        assertTrue("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property precondition failed.", entity.getProp1());
        assertTrue("Entity precondition failed.", entity.isValid().isSuccessful());

        // changes
        entity.getProperty("prop1").setRequired(true);

        // postconditions
        assertTrue("Property postcondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property postcondition failed.", entity.getProp1());
        assertFalse("Entity postcondition failed.", entity.isValid().isSuccessful());
    }

    @Test
    public void required_prop_attempted_to_set_null_with_subsequent_requirendess_removal_should_become_valid_and_null() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        entity.getProperty("prop1").setRequired(true);
        entity.setProp1(42);
        // preconditions
        assertTrue("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNotNull("Property precondition failed.", entity.getProp1());
        assertTrue("Entity precondition failed.", entity.isValid().isSuccessful());

        // changes 1
        entity.setProp1(null);

        // intermediate postcondition
        assertFalse("Property intermediate postcondition failed.", entity.getProperty("prop1").isValid());
        assertNotNull("Property intermediate postcondition failed.", entity.getProp1());
        assertFalse("Entity intermediate postcondition failed.", entity.isValid().isSuccessful());

        // changes 2
        entity.getProperty("prop1").setRequired(false);

        // final postconditions
        assertTrue("Property postcondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property postcondition failed.", entity.getProp1());
        assertTrue("Entity postcondition failed.", entity.isValid().isSuccessful());
    }

    @Test
    public void required_and_invalid_due_setting_null_prop_should_become_valid_once_its_dependent_on_prop_resets_requiredness_in_ACE() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        entity.getProperty("prop1").setRequired(true);
        entity.setProp1(42);
        entity.setProp1(null);

        // preconditions
        assertFalse("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNotNull("Property precondition failed.", entity.getProp1());
        assertFalse("Entity precondition failed.", entity.isValid().isSuccessful());

        // changes
        entity.setProp3(31);

        // postconditions
        assertTrue("Property postcondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property postcondition failed.", entity.getProp1());
        assertTrue("Entity postcondition failed.", entity.isValid().isSuccessful());
    }

    @Test
    public void required_and_invalid_due_setting_null_prop_should_remain_valid_once_its_dependent_on_prop_does_not_change_its_requiredness() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        entity.getProperty("prop1").setRequired(true);
        entity.setProp1(42);
        entity.setProp1(null);

        // preconditions
        assertFalse("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNotNull("Property precondition failed.", entity.getProp1());
        assertFalse("Entity precondition failed.", entity.isValid().isSuccessful());

        // changes
        entity.setProp2(31);

        // postconditions
        assertFalse("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNotNull("Property precondition failed.", entity.getProp1());
        assertFalse("Entity precondition failed.", entity.isValid().isSuccessful());
    }

    @Test
    public void prop_non_required_with_null_value_should_remain_valid_if_its_dependent_on_prop_does_change_its_requiredness_in_ACE() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        // preconditions
        assertTrue("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property precondition failed.", entity.getProp1());
        assertTrue("Entity precondition failed.", entity.isValid().isSuccessful());

        // changes
        entity.setProp4(31);

        // postconditions
        assertTrue("Property precondition failed.", entity.getProperty("prop1").isRequired());
        assertTrue("Property precondition failed.", entity.getProperty("prop1").isValid());
        assertNull("Property precondition failed.", entity.getProp1());
        assertFalse("Entity precondition failed.", entity.isValid().isSuccessful());
    }

    @Test
    public void required_boolean_property_is_valid_when_true_and_invalid_when_false_with_appropriate_error_msg() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        final MetaProperty<Boolean> mp = entity.getProperty("prop5");
        assertFalse(mp.getValue());
        assertTrue(mp.isValidWithRequiredCheck(true));

        final String errorMsg = "Must be set to true!";
        mp.setRequired(true, errorMsg); // let's use a custom error message
        assertFalse(mp.getValue());
        assertFalse(mp.isValidWithRequiredCheck(true));
        assertEquals(errorMsg, mp.getFirstFailure().getMessage());

        mp.setValue(true);
        assertTrue(mp.getValue());
        assertTrue(mp.isValidWithRequiredCheck(true));

        mp.setRequired(true, ""); // let's empty the custom error message to check the defaut one
        mp.setValue(false);
        assertTrue(mp.getValue());
        assertFalse(mp.isValid());
        assertEquals("Required property [Prop 5] must be true for entity [Entity With Dynamic Requiredness].", mp.getFirstFailure().getMessage());
    }

    @Test
    public void relaxing_requiredness_for_boolean_property_after_unsuccessful_attempt_to_assings_false() {
        final EntityWithDynamicRequiredness entity = factory.newByKey(EntityWithDynamicRequiredness.class, "KEY VALUE");
        final MetaProperty<Boolean> mp = entity.getProperty("prop5");
        assertFalse(mp.getValue());
        assertTrue(mp.isValidWithRequiredCheck(true));

        mp.setRequired(true);
        assertFalse(mp.getValue());
        assertFalse(mp.isValidWithRequiredCheck(true));

        mp.setValue(true);
        assertTrue(mp.getValue());
        assertTrue(mp.isValidWithRequiredCheck(true));
        
        mp.setValue(false);
        assertFalse(mp.getLastInvalidValue());
        assertFalse(mp.getLastAttemptedValue());
        assertTrue(mp.getValue());
        assertFalse(mp.isValid());

        mp.setRequired(false);
        assertFalse(mp.getValue());
        assertTrue(mp.isValid());
    }

}