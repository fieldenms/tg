package ua.com.fielden.platform.entity.validation;

import static org.junit.Assert.*;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.test_entities.CompositionalEntity;
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

}
