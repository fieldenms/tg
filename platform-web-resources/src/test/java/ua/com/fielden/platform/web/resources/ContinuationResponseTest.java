package ua.com.fielden.platform.web.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.continuation.NeedMoreDataException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.MoreDataForDeleteEntity;
import ua.com.fielden.platform.sample.domain.TgDeletionTestEntity;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.continuation.NeedMoreDataException.CONTINUATION_PROPERTY;
import static ua.com.fielden.platform.continuation.NeedMoreDataException.CONTINUATION_TYPE_STR;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;

/// Tests for the response to saving an entity whose companion requests more data with [NeedMoreData], as the web client receives it.
///
/// [NeedMoreData] is not an error: the companion throws it to interrupt `save` and request additional information from the user, such as the acknowledgement of warnings.
/// It is transported as the exception of a [Result], as [RestServerUtil#singleEntityResult] composes it, with the entity being saved as the instance, so that the master stays bound to it.
/// The web client recognises the request by `continuationTypeStr` of that exception, opens the master of the continuation type,
/// and saves the entity again with the provided continuation, which the companion then accesses under the key in `continuationProperty`.
///
/// The scenario is that of `TgDeletionTestEntityDao`, which requests a number from 1 to 10 for saving a persisted entity.
///
public class ContinuationResponseTest {
    private static final String
            REQUEST_MESSAGE = "Need to specify number from 1 to 10",
            CONTINUATION_KEY = "newDeleteEntity";

    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    // The types of the entity being saved and of the continuation are registered, as the entity types of an application are.
    private final RestServerUtil restUtil = new RestServerUtil(createSerialiserWithJackson(
            factory,
            new ProvidedSerialisationClassProvider(TgDeletionTestEntity.class, MoreDataForDeleteEntity.class),
            new SerialisationTypeEncoder(),
            new IdOnlyProxiedEntityTypeCacheForTests()));

    /// Returns the response to saving `entity`, during which its companion threw `request`, as the web client receives it.
    ///
    private JsonNode responseToSave(final TgDeletionTestEntity entity, final NeedMoreData request) {
        final Result response = restUtil.singleEntityResult(entity, Optional.of(request));
        try {
            return new ObjectMapper().readTree(restUtil.getSerialiser().serialise(response, JACKSON));
        } catch (final IOException ex) {
            throw new AssertionError(ex);
        }
    }

    private TgDeletionTestEntity entityBeingSaved() {
        return factory.newByKey(TgDeletionTestEntity.class, "ENTITY1");
    }

    @Test
    public void request_for_more_data_identifies_the_continuation_by_its_type_and_the_key_for_the_companion() {
        final JsonNode response = responseToSave(entityBeingSaved(), new NeedMoreData(REQUEST_MESSAGE, MoreDataForDeleteEntity.class, CONTINUATION_KEY));

        final JsonNode request = response.get(EX);
        assertEquals(NeedMoreDataException.class.getName(), request.get("@resultType").asText());
        assertEquals(REQUEST_MESSAGE, request.get(MESSAGE).asText());
        assertEquals("The client opens the master of this type to obtain the data.", MoreDataForDeleteEntity.class.getName(), request.get(CONTINUATION_TYPE_STR).asText());
        assertEquals("The client provides the continuation under this key when saving again.", CONTINUATION_KEY, request.get(CONTINUATION_PROPERTY).asText());
        assertFalse("A continuation identified by its type is created when its master is opened.", request.has(INSTANCE));
    }

    @Test
    public void request_for_more_data_is_responded_to_with_the_entity_being_saved() {
        final JsonNode response = responseToSave(entityBeingSaved(), new NeedMoreData(REQUEST_MESSAGE, MoreDataForDeleteEntity.class, CONTINUATION_KEY));

        assertEquals(REQUEST_MESSAGE, response.get(MESSAGE).asText());
        assertEquals("The master stays bound to the entity being saved.", TgDeletionTestEntity.class.getName(), response.get("@instanceType").asText());
        assertTrue(response.get(INSTANCE).isObject());
    }

    @Test
    public void request_for_more_data_with_an_initialised_continuation_carries_that_continuation_for_the_client_to_open() {
        final var continuation = factory.newEntity(MoreDataForDeleteEntity.class);
        final JsonNode response = responseToSave(entityBeingSaved(), new NeedMoreData(REQUEST_MESSAGE, continuation, CONTINUATION_KEY));

        final JsonNode request = response.get(EX);
        assertEquals(MoreDataForDeleteEntity.class.getName(), request.get(CONTINUATION_TYPE_STR).asText());
        assertEquals(CONTINUATION_KEY, request.get(CONTINUATION_PROPERTY).asText());
        assertEquals(MoreDataForDeleteEntity.class.getName(), request.get("@instanceType").asText());
        assertTrue(request.get(INSTANCE).isObject());
    }

}
