package ua.com.fielden.platform.web.utils;

import com.google.inject.Injector;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.web.app.ThreadLocalDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.GraphiQLResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static ua.com.fielden.platform.serialisation.api.impl.Serialiser.createSerialiserWithJackson;
import static ua.com.fielden.platform.web.resources.RestServerUtil.ERR_INVALID_REQUEST_BODY;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

/// Tests that a request body that cannot be deserialised is responded to with status `400 Bad Request`,
/// both by resources that use [WebUiResourceUtils#handleUndesiredExceptions] and by [GraphiQLResource].
///
public class InvalidRequestBodyResponseTest {
    private static final String INVALID_JSON = "{\"query\": ";

    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final RestServerUtil restUtil = new RestServerUtil(createSerialiserWithJackson(injector.getInstance(EntityFactory.class), new ProvidedSerialisationClassProvider(), new SerialisationTypeEncoder(), new IdOnlyProxiedEntityTypeCacheForTests()));

    private static StringRepresentation json(final String content) {
        return new StringRepresentation(content, APPLICATION_JSON);
    }

    /// Returns the content of `representation`, which is gzip-encoded.
    ///
    private static String content(final Representation representation) throws IOException {
        return new String(new GZIPInputStream(representation.getStream()).readAllBytes(), UTF_8);
    }

    private static Response newResponse() {
        return new Response(new Request(Method.POST, "https://tgdev.com/resource"));
    }

    @Test
    public void invalid_request_body_is_responded_to_with_400_and_the_generic_error() throws IOException {
        final Response response = newResponse();
        final Representation representation = handleUndesiredExceptions(response, () -> {
            restUtil.restoreJsonMap(json(INVALID_JSON));
            return null;
        }, restUtil);

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
        assertTrue(content(representation).contains(ERR_INVALID_REQUEST_BODY));
    }

    @Test
    public void other_exceptions_are_responded_to_with_500() {
        final Response response = newResponse();
        handleUndesiredExceptions(response, () -> { throw new IllegalStateException("Undesired."); }, restUtil);

        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    /// An authorisation model that authorises everything.
    ///
    private static final IAuthorisationModel AUTHORISED = new IAuthorisationModel() {
        @Override public Result authorise(final Class<? extends ISecurityToken> token) { return Result.successful(); }
        @Override public void start() {}
        @Override public void stop() {}
        @Override public boolean isStarted() { return true; }
    };

    /// Posts `body` to [GraphiQLResource], with a Web API that records its inputs in `inputs` and returns no errors.
    ///
    private Response postToGraphiQL(final String body, final List<Map<String, Object>> inputs, final List<Representation> representations) {
        final var request = new Request(Method.POST, "https://tgdev.com/graphiql", json(body));
        final var response = new Response(request);
        final var resource = new GraphiQLResource(
                input -> { inputs.add(input); return Map.of("data", Map.of()); },
                AUTHORISED, restUtil, new ThreadLocalDeviceProvider(), null, null, request, response);
        representations.add(resource.query(request.getEntity()));
        return response;
    }

    @Test
    public void invalid_request_body_for_GraphiQL_is_responded_to_with_400_and_a_GraphQL_error() throws IOException {
        final List<Map<String, Object>> inputs = new ArrayList<>();
        final List<Representation> representations = new ArrayList<>();
        final Response response = postToGraphiQL(INVALID_JSON, inputs, representations);

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
        assertEquals("{\"errors\":[{\"message\":\"%s\"}]}".formatted(ERR_INVALID_REQUEST_BODY), content(representations.getFirst()));
        assertTrue("The query should not be executed.", inputs.isEmpty());
    }

    @Test
    public void valid_request_body_for_GraphiQL_is_executed() {
        final List<Map<String, Object>> inputs = new ArrayList<>();
        final Response response = postToGraphiQL("{\"query\": \"{ __typename }\"}", inputs, new ArrayList<>());

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(List.of(Map.of("query", "{ __typename }")), inputs);
    }

}
