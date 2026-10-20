package ua.com.fielden.platform.web.resources;

import com.google.inject.Injector;
import org.junit.Test;
import org.restlet.representation.StringRepresentation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.web.resources.exceptions.InvalidRequestBodyException;

import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;
import static ua.com.fielden.platform.web.resources.RestServerUtil.ERR_INVALID_REQUEST_BODY;

/// Tests for the deserialisation of request bodies by [RestServerUtil].
///
public class RestServerUtilRequestBodyTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final RestServerUtil restUtil = new RestServerUtil(createSerialiserWithJackson(injector.getInstance(EntityFactory.class), new ProvidedSerialisationClassProvider(), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests()));

    private static StringRepresentation json(final String content) {
        return new StringRepresentation(content, APPLICATION_JSON);
    }

    @Test
    public void valid_request_body_is_deserialised() {
        assertEquals(Map.of("key", "value"), restUtil.restoreJsonMap(json("{\"key\": \"value\"}")));
    }

    @Test
    public void invalid_request_body_fails_with_a_generic_message_and_no_cause() {
        final var ex = assertThrows(InvalidRequestBodyException.class, () -> restUtil.restoreJsonMap(json("{\"key\": ")));
        assertEquals(ERR_INVALID_REQUEST_BODY, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void invalid_request_body_for_a_Result_fails_with_a_generic_message() {
        final var ex = assertThrows(InvalidRequestBodyException.class, () -> restUtil.restoreJsonResult(json("[1, 2]")));
        assertEquals(ERR_INVALID_REQUEST_BODY, ex.getMessage());
    }

    @Test
    public void error_response_for_an_invalid_request_body_discloses_no_details_of_the_failure() throws IOException {
        final var ex = assertThrows(InvalidRequestBodyException.class, () -> restUtil.restoreJsonMap(json("{\"key\": ")));
        // The representation is gzip-encoded.
        final String response = new String(new GZIPInputStream(restUtil.errorJSONRepresentation(ex).getStream()).readAllBytes(), UTF_8);
        assertTrue(response, response.contains(ERR_INVALID_REQUEST_BODY));
        assertFalse(response, response.contains("jackson"));
        assertFalse(response, response.contains("stackTrace"));
    }

}
