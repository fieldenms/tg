package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DynamicPropertyAccessTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonTestEntityModuleWithPropertyFactory())
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void value_of_declared_property_can_be_accessed() {
        final var entity = factory.newEntity(TgAuthor.class);
        assertNull(entity.get("surname"));
        entity.setSurname("Stephenson");
        assertEquals("Stephenson", entity.get("surname"));
    }

    @Test
    public void value_of_inherited_property_can_be_accessed() {
        final var entity = factory.newEntity(EntityExt.class);
        assertNull(entity.get("number"));
        entity.setNumber(42);
        assertEquals(Integer.valueOf(42), entity.get("number"));
    }

    @Test
    public void value_of_property_key_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("key"));
        entity.setKey("Joe");
        assertEquals("Joe", entity.get("key"));
    }

    @Test
    public void value_of_property_id_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("id"));
        entity.setId(1L);
        assertEquals(Long.valueOf(1L), entity.get("id"));
    }

    @Test
    public void value_of_property_version_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertEquals(Long.valueOf(0L), entity.get("version"));
        entity.setVersion(1L);
        assertEquals(Long.valueOf(1L), entity.get("version"));
    }

    @Test
    public void value_of_declared_property_can_be_set() {
        final var entity = factory.newEntity(TgAuthor.class);
        assertNull(entity.get("surname"));
        entity.set("surname", "Stephenson");
        assertEquals("Stephenson", entity.get("surname"));
    }

    @Test
    public void value_of_inherited_property_can_be_set() {
        final var entity = factory.newEntity(EntityExt.class);
        assertNull(entity.get("number"));
        entity.set("number", 42);
        assertEquals(Integer.valueOf(42), entity.get("number"));
    }

    @Test
    public void value_of_property_id_can_be_set() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("id"));
        entity.set("id", 1L);
        assertEquals(Long.valueOf(1), entity.get("id"));
    }

    @Test
    public void value_of_property_version_can_be_set() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertEquals(Long.valueOf(0L), entity.get("version"));
        entity.set("version", 1L);
        assertEquals(Long.valueOf(1), entity.get("version"));
    }

}
