package ua.com.fielden.platform.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Unit tests to ensure correct {@link AbstractEntity} descendants serialisation / deserialisation using JACKSON engine.
 *
 * @author TG Team
 *
 */
public class EntitySerialisationWithJacksonTest {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final ISerialiserEngine jacksonSerialiser = new Serialiser(factory, new ProvidedSerialisationClassProvider(EmptyEntity.class)).getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = new Serialiser(factory, new ProvidedSerialisationClassProvider(EmptyEntity.class)).getEngine(SerialiserEngines.JACKSON);

    @Test
    public void null_entity_should_be_restored() throws Exception {
        final EmptyEntity entity = null;
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNull("Entity has not been deserialised successfully.", restoredEntity);
    }

    @Test
    public void empty_entity_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, "key", "description");
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertTrue("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());
        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertTrue("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_id_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, null, "key", "description");
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect id.", null, restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertTrue("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());
        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertTrue("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_key_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, null, "description");
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", null, restoredEntity.getKey());
        assertFalse("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());
        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertTrue("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_desc_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, "key", null);
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertTrue("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());
        assertEquals("Incorrect desc.", null, restoredEntity.getDesc());
        assertFalse("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }
}
