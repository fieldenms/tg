package ua.com.fielden.platform.types;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithRichText;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import java.util.Date;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;

/**
 * Tests that cover serialisation / deserialisation of {@link RichText} with the JACKSON engine.
 *
 * @author TG Team
 */
public class RichTextSerialisationWithJacksonTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonTestEntityModuleWithPropertyFactory())
            .getInjector();
    private final FactoryForTestingEntities factory = new FactoryForTestingEntities(injector.getInstance(EntityFactory.class), new Date());
    private final ISerialiserEngine jacksonSerialiser = createSerialiserWithJackson(
            factory.getFactory(), createClassProvider(), createSerialisationTypeEncoder(), createIdOnlyProxiedEntityTypeCache())
            .getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = createSerialiserWithJackson(
            factory.getFactory(), createClassProvider(), createSerialisationTypeEncoder(), createIdOnlyProxiedEntityTypeCache())
            .getEngine(SerialiserEngines.JACKSON);

    private IIdOnlyProxiedEntityTypeCache createIdOnlyProxiedEntityTypeCache() {
        return new IdOnlyProxiedEntityTypeCacheForTests();
    }

    private ISerialisationTypeEncoder createSerialisationTypeEncoder() {
        return new SerialisationTypeEncoder();
    }

    private ProvidedSerialisationClassProvider createClassProvider() {
        return new ProvidedSerialisationClassProvider(EntityWithRichText.class);
    }

    @Test
    public void RichText_contents_are_preserved_after_serialisation_and_deserialisation() {
        final var richText = RichText.fromHtml("hello <b> world </b>");
        final var entity = factory.createEntityWithRichText(richText);
        final var restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithRichText.class);

        assertNotNull(restoredEntity);
        assertNotSame(entity, restoredEntity);
        assertTrue(entity.getText().iEquals(restoredEntity.getText()));
        assertFalse(restoredEntity.getProperty("text").isChangedFromOriginal());
        assertFalse(restoredEntity.getProperty("text").isDirty());
    }

    @Test
    public void persisted_RichText_contents_are_preserved_after_serialisation_and_deserialisation() {
        final var richText = RichText.fromHtml("hello <b> world </b>").asPersisted();
        final var entity = factory.createEntityWithRichText(richText);
        final var restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithRichText.class);

        assertNotNull(restoredEntity);
        assertNotSame(entity, restoredEntity);
        assertTrue(entity.getText().iEquals(restoredEntity.getText()));
        assertFalse(restoredEntity.getProperty("text").isChangedFromOriginal());
        assertFalse(restoredEntity.getProperty("text").isDirty());
    }

}
