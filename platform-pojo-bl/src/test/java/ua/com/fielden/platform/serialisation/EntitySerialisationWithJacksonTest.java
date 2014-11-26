package ua.com.fielden.platform.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBigDecimal;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBoolean;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithDate;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMoney;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSameEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithString;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

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
    private final ISerialiserEngine jacksonSerialiser = new Serialiser(factory, createClassProvider()).getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = new Serialiser(factory, createClassProvider()).getEngine(SerialiserEngines.JACKSON);

    private ProvidedSerialisationClassProvider createClassProvider() {
        return new ProvidedSerialisationClassProvider(
                EmptyEntity.class,
                EntityWithBigDecimal.class,
                EntityWithInteger.class,
                EntityWithString.class,
                EntityWithBoolean.class,
                EntityWithDate.class,
                EntityWithOtherEntity.class,
                EntityWithSameEntity.class,
                OtherEntity.class,
                EntityWithMoney.class //
        );
    }

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

    @Test
    public void entity_with_big_decimal_prop_should_be_restored() throws Exception {
        final EntityWithBigDecimal entity = factory.newEntity(EntityWithBigDecimal.class, 1L, "key", "description");
        entity.setProp(BigDecimal.TEN);
        final EntityWithBigDecimal restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBigDecimal.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", BigDecimal.TEN, restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_integer_prop_should_be_restored() throws Exception {
        final EntityWithInteger entity = factory.newEntity(EntityWithInteger.class, 1L, "key", "description");
        entity.setProp(new Integer(23));
        final EntityWithInteger restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithInteger.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", new Integer(23), restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_string_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_boolean_prop_should_be_restored() throws Exception {
        final EntityWithBoolean entity = factory.newEntity(EntityWithBoolean.class, 1L, "key", "description");
        entity.setProp(true);
        final EntityWithBoolean restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBoolean.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", true, restoredEntity.isProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_date_prop_should_be_restored() throws Exception {
        final EntityWithDate entity = factory.newEntity(EntityWithDate.class, 1L, "key", "description");
        final Date date = new Date();
        entity.setProp(date);
        final EntityWithDate restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithDate.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", date, restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_money_prop_should_be_restored() throws Exception {
        final EntityWithMoney entity = factory.newEntity(EntityWithMoney.class, 1L, "key", "description");
        entity.setProp(new Money("23.00", 20, Currency.getInstance("AUD")));
        final EntityWithMoney restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithMoney.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", new Money("23.00", 20, Currency.getInstance("AUD")), restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_other_entity_prop_should_be_restored() throws Exception {
        final EntityWithOtherEntity entity = factory.newEntity(EntityWithOtherEntity.class, 1L, "key", "description");
        entity.setProp(factory.newEntity(OtherEntity.class, 1L, "other_key", "description"));
        final EntityWithOtherEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithOtherEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", factory.newEntity(OtherEntity.class, 1L, "other_key", "description"), restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_the_same_entity_prop_should_be_restored() throws Exception {
        final EntityWithSameEntity entity = factory.newEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(factory.newEntity(EntityWithSameEntity.class, 2L, "key2", "description"));
        final EntityWithSameEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSameEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", factory.newEntity(EntityWithSameEntity.class, 2L, "key2", "description"), restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    @Ignore
    public void entity_with_the_same_entity_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithSameEntity entity = factory.newEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(entity);
        final EntityWithSameEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSameEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertEquals("Incorrect prop.", restoredEntity, restoredEntity.getProp());
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }
}
