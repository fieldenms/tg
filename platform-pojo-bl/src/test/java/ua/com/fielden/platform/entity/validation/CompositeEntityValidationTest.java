package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.*;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.CompositionalEntity;
import ua.com.fielden.platform.entity.validation.test_entities.CompositionalEntityWithOptionalKeyMembers;
import ua.com.fielden.platform.entity.validation.test_entities.CompositionalEntityWithTransactionalKeyMembers;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case for entity validation related to requiredness of properties.
 *
 * @author TG Team
 *
 */
public class CompositeEntityValidationTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void composite_entity_with_unassigned_transactional_key_members_should_be_recognised_as_valid() {
        final CompositionalEntityWithTransactionalKeyMembers entity = factory.newEntity(CompositionalEntityWithTransactionalKeyMembers.class);
        assertTrue(entity.getProperty("date").isValid());
        assertTrue(entity.getProperty("user").isValid());
        assertTrue(entity.isValid().isSuccessful());
    }

    @Test
    public void composite_entity_with_unassigned_key_members_should_be_recognised_as_invalid() {
        final CompositionalEntity entity = factory.newEntity(CompositionalEntity.class);
        assertTrue(entity.getProperty("key1").isValid()); // still true as there was no attempt to assign any value
        assertTrue(entity.getProperty("key2").isValid()); // still true as there was no attempt to assign any value
        assertFalse(entity.isValid().isSuccessful()); // but the overall entity validation should fail due to requiredness of composite keys
    }

    @Test
    public void composite_entity_with_unassigned_optional_key_member_should_be_recognised_as_valid() {
        final CompositionalEntityWithOptionalKeyMembers entity = factory.newEntity(CompositionalEntityWithOptionalKeyMembers.class);
        entity.setKey1(78943);
        assertTrue(entity.getProperty("key1").isValid());
        assertTrue(entity.getProperty("key2").isValid());
        assertTrue(entity.isValid().isSuccessful());
    }

    @Test
    public void composite_entity_with_optional_key_member_should_be_recognised_as_valid_when_all_members_are_assigned_to_non_null_values() {
        final CompositionalEntityWithOptionalKeyMembers entity = factory.newEntity(CompositionalEntityWithOptionalKeyMembers.class);
        entity.setKey1(78943);
        entity.setKey2("value");
        assertTrue(entity.getProperty("key1").isValid());
        assertTrue(entity.getProperty("key2").isValid());
        assertTrue(entity.isValid().isSuccessful());
    }

    @Test
    public void reassigning_optional_key_member_to_null_should_be_permitted() {
        final CompositionalEntityWithOptionalKeyMembers entity = factory.newEntity(CompositionalEntityWithOptionalKeyMembers.class);
        entity.setKey1(78943);
        entity.setKey2("value");
        assertNotNull(entity.getKey2());
        entity.setKey2(null);

        assertTrue(entity.getProperty("key1").isValid());
        assertTrue(entity.getProperty("key2").isValid());
        assertNull(entity.getKey2());
        assertTrue(entity.isValid().isSuccessful());
    }

}
