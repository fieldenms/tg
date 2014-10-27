package ua.com.platfrom.property.validator;

import static org.junit.Assert.*;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

public class StringValidatorTestCase {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void correct_email_address_should_be_permitted() {
        final EntityWithStringProperties entity = factory.newByKey(EntityWithStringProperties.class, "key1");

        final MetaProperty<String> emailProperty = entity.getProperty("email");
        assertTrue(emailProperty.isValid());

        entity.setEmail("nza.support@fielden.com.ua");
        assertTrue(emailProperty.isValid());
    }

    @Test
    public void email_address_requires_at_least_one_dot_in_its_second_part() {
        final EntityWithStringProperties entity = factory.newByKey(EntityWithStringProperties.class, "key1");

        final MetaProperty<String> emailProperty = entity.getProperty("email");
        entity.setEmail("support@fielden");
        assertFalse(emailProperty.isValid());
    }

    @Test
    public void email_address_requires_at_sign() {
        final EntityWithStringProperties entity = factory.newByKey(EntityWithStringProperties.class, "key1");

        final MetaProperty<String> emailProperty = entity.getProperty("email");
        entity.setEmail("support_fielden.com.au");
        assertFalse(emailProperty.isValid());
    }

    @Test
    public void blank_email_address_should_not_be_permitted() {
        final EntityWithStringProperties entity = factory.newByKey(EntityWithStringProperties.class, "key1");

        final MetaProperty<String> emailProperty = entity.getProperty("email");
        entity.setEmail("  ");
        assertFalse(emailProperty.isValid());
        entity.setEmail("");
        assertFalse(emailProperty.isValid());
    }

    @Test
    public void email_address_is_not_required_so_null_values_should_be_permitted() {
        final EntityWithStringProperties entity = factory.newByKey(EntityWithStringProperties.class, "key1");

        final MetaProperty<String> emailProperty = entity.getProperty("email");
        entity.setEmail("support@fielden.com.ua");
        assertEquals("support@fielden.com.ua", entity.getEmail());
        entity.setEmail(null);
        assertTrue(emailProperty.isValid());
        assertNull(entity.getEmail());
    }
}
