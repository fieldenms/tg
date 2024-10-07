package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.annotation.Final.ERR_REASSIGNMENT;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithFinalValidation;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

/**
 * A test case for properties declared as {@link Final}.
 */
public class FinalPropertiesTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void non_persistedOnly_property_does_not_permit_assignment_more_than_once_before_and_after_entity_is_persisted() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propNonPersistedAsFinal");
        assertTrue(mp.isValid());
        assertTrue(mp.isEditable());

        entity.setPropNonPersistedAsFinal("value1");
        assertEquals("value1", mp.getValue());
        assertFalse(mp.isEditable());

        // let's try changing the property again
        entity.setPropNonPersistedAsFinal("value2");
        assertFalse(mp.isValid());
        assertEquals(ERR_REASSIGNMENT.formatted(mp.getTitle(), EntityWithFinalValidation.ENTITY_TITLE), mp.getFirstFailure().getMessage());
        assertEquals("value1", mp.getValue()); // the value is not changed

        // let's now mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertFalse(mp.isEditable());
        entity.setPropNonPersistedAsFinal("value2");
        assertFalse(mp.isValid());
        assertEquals(ERR_REASSIGNMENT.formatted(mp.getTitle(), EntityWithFinalValidation.ENTITY_TITLE), mp.getFirstFailure().getMessage());
        assertEquals("value1", mp.getValue()); // the value is not changed
    }

    @Test
    public void null_valued_non_persistedOnly_but_not_nullIsValue_property_permits_assignment_for_persisted_entities() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propNonPersistedAsFinal");

        // let's now mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertNull(mp.getValue()); // still null
        assertTrue(mp.isEditable());
        entity.setPropNonPersistedAsFinal("value1");
        assertTrue(mp.isValid());
        assertEquals("value1", mp.getValue());
        assertFalse(mp.isEditable());
    }

    @Test
    public void null_valued_non_persistedOnly_but_nullIsValue_property_does_not_permit_assignment_for_persisted_entities() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propNonPersistedAndNullAsFinalValue");

        // let's now mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertFalse(mp.isEditable());
        entity.setPropNonPersistedAndNullAsFinalValue("value1");
        assertFalse(mp.isValid());
        assertEquals(ERR_REASSIGNMENT.formatted(mp.getTitle(), EntityWithFinalValidation.ENTITY_TITLE), mp.getFirstFailure().getMessage());
        assertNull(mp.getValue());   // the value is not changed
    }

    @Test
    public void persistedOnly_permits_assignment_more_than_once_while_entity_is_not_persisted() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);

        final MetaProperty<String> mp = entity.getProperty("propNonNullAsFinalValue");
        assertTrue(mp.isValid());
        assertTrue(mp.isEditable());

        entity.setPropNonNullAsFinalValue("value1");
        assertEquals("value1", mp.getValue());
        assertTrue(mp.isEditable());

        // let's try changing the property again
        entity.setPropNonNullAsFinalValue("value2");
        assertEquals("value2", mp.getValue());
        assertTrue(mp.isValid());

        // let's now mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertFalse(mp.isEditable());
        entity.setPropNonNullAsFinalValue("value3");
        assertFalse(mp.isValid());
        assertEquals(ERR_REASSIGNMENT.formatted(mp.getTitle(), EntityWithFinalValidation.ENTITY_TITLE), mp.getFirstFailure().getMessage());
        assertEquals("value2", mp.getValue()); // the value is not changed
    }

    @Test
    public void null_valued_persistedOnly_but_not_nullIsValue_property_permits_assignment_for_persisted_entities() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);
        final MetaProperty<String> mp = entity.getProperty("propNonNullAsFinalValue");
        // let's mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertNull(mp.getValue());
        assertTrue(mp.isEditable());

        entity.setPropNonNullAsFinalValue("value1");
        assertTrue(mp.isValid());
        assertEquals("value1", mp.getValue());
        assertTrue(mp.isEditable()); // still editable as the change was made, but not yet persisted
        mp.resetValues(); // assigns original value to the current one, mimicing that it was persisted
        assertFalse(mp.isEditable());
    }

    @Test
    public void null_valued_persistedOnly_and_nullIsValue_does_not_permit_assignment_for_persisted_entities() {
        final var entity = factory.newEntity(EntityWithFinalValidation.class);
        final MetaProperty<String> mp = entity.getProperty("propNullAsFinalValue");
        // let's mimic that entity is persisted and try changing the property
        entity.setId(1L);
        mp.resetValues(); // assigns original value to the current one
        assertNull(mp.getValue());
        assertFalse(mp.isEditable());

        entity.setPropNullAsFinalValue("value1");
        assertFalse(mp.isValid());
        assertEquals(ERR_REASSIGNMENT.formatted(mp.getTitle(), EntityWithFinalValidation.ENTITY_TITLE), mp.getFirstFailure().getMessage());
        assertNull(mp.getValue());
    }

}
