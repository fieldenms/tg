package ua.com.fielden.platform.entity.meta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.entity.Entity;

/**
 * Ensures correct instantiation of {@link PropertyDescriptor}.
 * 
 * @author TG Team
 * 
 */
public class PropertyDescriptorTest {

    @Test
    public void test_that_ordinary_property_is_described_correctly() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "firstProperty");
        assertEquals("Incorrect property name.", "firstProperty", pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "First Property", pd.getKey());
        assertEquals("Incorrect property desc.", "used for testing", pd.getDesc());
    }

    @Test
    public void test_that_property_key_is_described_correctly() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "key");
        assertEquals("Incorrect property name.", "key", pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "Entity No", pd.getKey());
        assertEquals("Incorrect property desc.", "Key Property", pd.getDesc());
    }

    @Test
    public void test_that_property_desc_is_described_correctly() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "desc");
        assertEquals("Incorrect property name.", "desc", pd.getPropertyName());
        assertEquals("Incorrect entity type.", Entity.class, pd.getEntityType());
        assertEquals("Incorrect property title.", "Description", pd.getKey());
        assertEquals("Incorrect property desc.", "Description Property", pd.getDesc());
    }

    @Test
    public void test_that_to_string_conversion_works() {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "firstProperty");
        assertEquals("Incorrect to string conversion.", "ua.com.fielden.platform.entity.Entity:firstProperty", pd.toString());
    }

    @Test
    public void test_that_from_string_conversion_works() throws Exception {
        final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "firstProperty");
        final PropertyDescriptor<Entity> pdRestored = PropertyDescriptor.fromString("ua.com.fielden.platform.entity.Entity:firstProperty");
        assertEquals("Incorrect to string conversion.", pd, pdRestored);
    }

}
