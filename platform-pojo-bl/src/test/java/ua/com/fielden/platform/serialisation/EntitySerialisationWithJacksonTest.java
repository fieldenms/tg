package ua.com.fielden.platform.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.Interval;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ClassWithMap;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.EntityWithByteArray;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.entity.BaseEntity;
import ua.com.fielden.platform.serialisation.entity.EntityWithPolymorphicProperty;
import ua.com.fielden.platform.serialisation.entity.EntityWithQueryProperty;
import ua.com.fielden.platform.serialisation.entity.SubBaseEntity1;
import ua.com.fielden.platform.serialisation.entity.SubBaseEntity2;
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
    private final ISerialiserEngine jacksonSerialiser = new Serialiser(factory, new ProvidedSerialisationClassProvider(Entity.class, ClassWithMap.class, EntityWithPolymorphicProperty.class, BaseEntity.class, SubBaseEntity1.class, SubBaseEntity2.class, EntityWithByteArray.class, EntityWithQueryProperty.class)).getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = new Serialiser(factory, new ProvidedSerialisationClassProvider(Entity.class, ClassWithMap.class, EntityWithPolymorphicProperty.class, BaseEntity.class, SubBaseEntity1.class, SubBaseEntity2.class, EntityWithByteArray.class, EntityWithQueryProperty.class)).getEngine(SerialiserEngines.JACKSON);

    @Test
    @Ignore
    public void test_joda_interval_serialisation() throws Exception {
        final Interval interval = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(new Interval(0, 100)), Interval.class);

        assertNotNull("Interval has not been deserialised successfully.", interval);
        assertEquals("Incorrect start.", 0L, interval.getStartMillis());
        assertEquals("Incorrect end.", 100L, interval.getEndMillis());
    }
}
