package ua.com.fielden.platform.serialisation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.serialisation.api.impl.TgJackson.ERR_RESTRICTED_TYPE_SERIALISATION;
import static ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser.ERR_RESTRICTED_TYPE_SERIALISATION_DUE_TO_PROP_TYPE;

import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithUserSecret;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntityDeserialisationException;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntitySerialisationException;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

/**
 * Unit tests to ensure correct serialisation of {@link UserSecret} instances or instances that have properties of ths type is not permitted using the Jackson engine.
 *
 * @author TG Team
 *
 */
public class RestrictedEntitySerialisationWithJacksonTest {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    
    @Test
    public void instances_of_UserSecret_cannot_be_serialised() {
        final UserSecret us = factory.newEntity(UserSecret.class);
        try {
            final ISerialiserEngine jacksonSerialiser = Serialiser.createSerialiserWithKryoAndJackson(factory, new ProvidedSerialisationClassProvider(UserSecret.class), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests()).getEngine(SerialiserEngines.JACKSON);
            jacksonSerialiser.serialise(us);
            fail();
        } catch (final EntitySerialisationException ex) {
            assertEquals(format(ERR_RESTRICTED_TYPE_SERIALISATION, us.getClass().getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void instances_of_UserSecret_cannot_be_deserialised() {
        try {
            final ISerialiserEngine jacksonDeserialiser = Serialiser.createSerialiserWithKryoAndJackson(factory, new ProvidedSerialisationClassProvider(UserSecret.class), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests()).getEngine(SerialiserEngines.JACKSON);
            jacksonDeserialiser.deserialise(new byte[] {}, UserSecret.class);
            fail();
        } catch (final EntityDeserialisationException ex) {
            assertEquals(format(TgJackson.ERR_RESTRICTED_TYPE_DESERIALISATION, UserSecret.class.getSimpleName()), ex.getMessage());
        }
    }

    @Test
    public void creating_serialiser_for_types_that_reference_UserSecret_fail() {
        final UserSecret us = EntityFactory.newPlainEntity(UserSecret.class, 1L);
        final EntityWithUserSecret entWithSecret = EntityFactory.newPlainEntity(EntityWithUserSecret.class, 2L);
        entWithSecret.setSecret(us);
        try {
            Serialiser.createSerialiserWithKryoAndJackson(factory, new ProvidedSerialisationClassProvider(UserSecret.class, EntityWithUserSecret.class), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests()).getEngine(SerialiserEngines.JACKSON);
            fail();
        } catch (final EntitySerialisationException ex) {
            assertEquals(format(ERR_RESTRICTED_TYPE_SERIALISATION_DUE_TO_PROP_TYPE, EntityWithUserSecret.class.getSimpleName(), "secret"), ex.getMessage());
        }
    }
}
