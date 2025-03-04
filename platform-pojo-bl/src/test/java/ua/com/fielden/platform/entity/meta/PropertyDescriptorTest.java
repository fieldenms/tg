package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

/**
 * Ensures correct instantiation of {@link PropertyDescriptor}.
 * 
 * @author TG Team
 * 
 */
public class PropertyDescriptorTest {

    @Test
    public void can_represented_ordinary_property() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "firstProperty");
        assertEquals("Incorrect property name.", "firstProperty", pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "First Property", pd.getKey());
        assertEquals("Incorrect property desc.", "used for testing", pd.getDesc());
    }

    @Test
    public void can_represent_property_key() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, AbstractEntity.KEY);
        assertEquals("Incorrect property name.", AbstractEntity.KEY, pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "Entity No", pd.getKey());
        assertEquals("Incorrect property desc.", "Key Property", pd.getDesc());
    }

    @Test
    public void can_represent_property_desc() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, AbstractEntity.DESC);
        assertEquals("Incorrect property name.", AbstractEntity.DESC, pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "Description", pd.getKey());
        assertEquals("Incorrect property desc.", "Description Property", pd.getDesc());
    }

    @Test
    public void supports_invarian_string_representation() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "firstProperty");
        final String expected = "ua.com.fielden.platform.test_entities.Entity:firstProperty";
        assertEquals("Incorrect to string conversion.", expected, pd.toString());
        final PropertyDescriptor<Entity> pdRestored = PropertyDescriptor.fromString(expected);
        assertEquals(pd, pdRestored);
    }

    @Test(expected=EntityException.class)
    public void reconstruction_from_string_fails_if_type_does_not_exist() throws Exception {
        PropertyDescriptor.fromString("ua.com.fielden.platform.entity.EntityDoesNotExist:firstProperty");
    }

    @Test(expected=EntityException.class)
    public void reconstruction_from_string_fails_if_property_does_not_exist() throws Exception {
        PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstPropertyDoesNotExist");
    }

    @Test
    public void inequality_holds() {
        final String one = "ua.com.fielden.platform.test_entities.Entity:firstProperty";
        final String two = "ua.com.fielden.platform.test_entities.Entity:observableProperty";
        assertNotEquals(PropertyDescriptor.fromString(one), PropertyDescriptor.fromString(two));
    }

    @Test
    public void reflexivity_holds_for_equality() {
        final PropertyDescriptor<?> pd = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        assertEquals(pd, pd);
    }

    @Test
    public void symmetry_holds_for_equality() {
        final PropertyDescriptor<?> pd1 = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        final PropertyDescriptor<?> pd2 = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        assertEquals(pd1, pd2);
        assertEquals(pd2, pd1);
        assertEquals(pd1.hashCode(), pd2.hashCode());
    }

    @Test
    public void transitivity_holds_for_equality() {
        final PropertyDescriptor<?> pd1 = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        final PropertyDescriptor<?> pd2 = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        final PropertyDescriptor<?> pd3 = PropertyDescriptor.fromString("ua.com.fielden.platform.test_entities.Entity:firstProperty");
        assertEquals(pd1, pd2);
        assertEquals(pd2, pd3);
        assertEquals(pd1, pd3);
        assertEquals(pd1.hashCode(), pd2.hashCode());
        assertEquals(pd2.hashCode(), pd3.hashCode());
        assertEquals(pd1.hashCode(), pd3.hashCode());
    }

    @Test
    public void PropertyDescriptor_key_has_all_default_String_validations_skipped() {
        final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
        final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
        final EntityFactory factory = injector.getInstance(EntityFactory.class);

        final PropertyDescriptor<?> pd = factory.newEntity(PropertyDescriptor.class);
        pd.setKey("  hello,  world  \n");
        assertTrue(pd.getProperty(AbstractEntity.KEY).isValid());
        assertEquals("  hello,  world  \n", pd.getKey());
    }

}