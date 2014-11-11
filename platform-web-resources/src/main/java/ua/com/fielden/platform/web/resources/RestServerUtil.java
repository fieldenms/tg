package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.engine.header.Header;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.snappy.SnappyQuery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;

/**
 * This is a convenience class providing some common routines used in the implementation of web-resources.
 *
 * @author TG Team
 *
 */
public class RestServerUtil {

    private final ISerialiser serialiser;
    /** An application wide public key */
    private String appWidePublicKey;
    /** An application wide private key */
    private String appWidePrivateKey;

    // TODO will move to seriliser.
    private final TgObjectMapper jsonSerialiser;

    @Inject
    public RestServerUtil(final ISerialiser serialiser, final TgObjectMapper jsonSerialiser) {
        this.serialiser = serialiser;
        this.jsonSerialiser = jsonSerialiser;
    }

    private final Logger logger = Logger.getLogger(RestServerUtil.class);

    private static final String HEADERS_KEY = "org.restlet.http.headers";

    private static Series<Header> getMessageHeaders(final Message message) {
        final ConcurrentMap<String, Object> attrs = message.getAttributes();
        Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
        if (headers == null) {
            headers = new Series<Header>(Header.class);
            final Series<Header> prev = (Series<Header>) attrs.putIfAbsent(HEADERS_KEY, headers);
            if (prev != null) {
                headers = prev;
            }
        }
        return headers;
    }

    /**
     * Creates a response header entry.
     *
     * @param response
     * @param headerEntry
     * @param value
     */
    public void setHeaderEntry(final Response response, final HttpHeaders headerEntry, final String value) {
        getMessageHeaders(response).add(headerEntry.value, value);
    }

    /**
     * Returns a header entry value if present. Otherwise, null.
     *
     * @param response
     * @param headerEntry
     * @return
     */
    public String getHeaderValue(final Request request, final HttpHeaders headerEntry) {
        final Series<Header> header = getMessageHeaders(request);
        return header != null && header.getFirst(headerEntry.value) != null ? header.getFirst(headerEntry.value).getValue() : null;
    }

    /**
     * Creates a JSON representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     * @throws JsonProcessingException
     */
    public Representation errorJSONRepresentation(final String string){
        logger.debug("Start building error JSON representation:" + new DateTime());
        //final byte[] bytes = serialiser.serialise(new Result(null, new Exception(string)), SerialiserEngine.JACKSON);
        byte[] bytes = new byte[0];
	try {
	    bytes = jsonSerialiser.writeValueAsBytes(new Result(null, new Exception(string)));
	} catch (final JsonProcessingException e) {
	    e.printStackTrace();
	}
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length */);
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final String string) {
        logger.debug("Start building error representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(new Result(null, new Exception(string)));
        logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final Exception ex) {
        logger.debug("Start building error representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(new Result(ex));
        logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Creates a JSON representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     * @throws JsonProcessingException
     */
    public Representation errorJSONRepresentation(final Exception ex) throws JsonProcessingException {
        logger.debug("Start building error JSON representation:" + new DateTime());
        //final byte[] bytes = serialiser.serialise(new Result(ex), SerialiserEngine.JACKSON);
        final byte[] bytes = jsonSerialiser.writeValueAsBytes(new Result(ex));
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/ );
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation resultRepresentation(final Result result) {
        logger.debug("Start building result representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result);
        logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Creates a JSON representation of {@link Result}.
     *
     * @param string
     * @return
     * @throws JsonProcessingException
     */
    public Representation resultJSONRepresentation(final Result result) throws JsonProcessingException {
        logger.debug("Start building result JSON representation:" + new DateTime());
        //final byte[] bytes = serialiser.serialise(result, SerialiserEngine.JACKSON);
        final byte[] bytes = jsonSerialiser.writeValueAsBytes(result);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM /* , bytes.length */);
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation snappyResultRepresentation(final List filteredEntities) {
        logger.debug("Start building snappy result representation:" + new DateTime());
        try {
            // create a Result enclosing entity list
            final byte[] bytes = serialiser.serialise(new Result(new ArrayList(filteredEntities), "Snappy pair is Ok"));
            logger.debug("SIZE: " + bytes.length);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes representation of a list of entities.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation listRepresentation(final List<T> entities) {
        logger.debug("Start building entities representation.");
        try {
            // create a Result enclosing entity list
            final Result result = new Result(new ArrayList<T>(entities), "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            logger.debug("SIZE: " + bytes.length);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes representation of a map.
     *
     * @return
     */
    public Representation mapRepresentation(final Map<?, ?> map) {
        logger.debug("Start building map representation.");
        try {
            // create a Result enclosing entity list
            final Result result = new Result(new HashMap<>(map), "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            logger.debug("SIZE: " + bytes.length);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes representation of a lifecycle data.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation lifecycleRepresentation(final LifecycleModel<T> lifecycleModel) {
        logger.debug("Start building lifecycle representation:" + new DateTime());
        try {
            // create a Result enclosing lifecycle data
            final Result result = new Result(lifecycleModel, "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            logger.debug("SIZE: " + bytes.length);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes KRYO representation of an entity.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation singleRepresentation(final T entity) {
        try {
            // create a Result enclosing entity list
            final Result result = entity != null ? new Result(entity, "OK") : new Result(null, new Exception("Could not find entity."));
            final byte[] bytes = serialiser.serialise(result);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes JACKSON representation of an entity.
     *
     * @return
     * @throws JsonProcessingException
     */
    public <T extends AbstractEntity> Representation singleJSONRepresentation(final T entity) throws JsonProcessingException {
        try {
            // create a Result enclosing entity list
            final Result result = entity != null ? new Result(entity, "OK") : new Result(null, new Exception("Could not find entity."));
            //final byte[] bytes = serialiser.serialise(result, SerialiserEngine.JACKSON);
            final byte[] bytes = jsonSerialiser.writeValueAsBytes(result);
            return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* TODO , bytes.length*/ );
        } catch (final Exception ex) {
            logger.error(ex);
            return errorJSONRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Converts representation of {@link QueryExecutionModel} to an actual instance.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public QueryExecutionModel<?, ?> restoreQueryExecutionModel(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), QueryExecutionModel.class);
    }

    /**
     * Converts representation of {@link DynamicallyTypedQueryContainer} to an instance of {@link QueryExecutionModel}.
     *
     * @param <T>
     * @param representation
     * @return
     * @throws Exception
     */
    public QueryExecutionModel<?, ?> restoreQueryExecutionModelForGeneratedType(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), DynamicallyTypedQueryContainer.class).getQem();
    }

    /**
     * Converts representation to an instance of {@link LifecycleQueryContainer}.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public LifecycleQueryContainer restoreLifecycleQueryContainer(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), LifecycleQueryContainer.class);
    }

    public SnappyQuery restoreSnappyQuery(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), SnappyQuery.class);
    }

    /**
     * Converts representation of the export request representation in to a list.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public List<?> restoreList(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), List.class);
    }

    /**
     * Converts representation of an entity to an actual instance.
     *
     * @param <T>
     * @param <K>
     * @param representation
     * @param type
     * @return
     * @throws Exception
     */
    public <T extends AbstractEntity> T restoreEntity(final Representation representation, final Class<T> type) throws Exception {
        return serialiser.deserialise(representation.getStream(), type);
    }

    /**
     * Converts JSON representation of an entity to an actual instance.
     *
     * @param <T>
     * @param <K>
     * @param representation
     * @param type
     * @return
     * @throws Exception
     */
    public <T extends AbstractEntity> T restoreJSONEntity(final Representation representation, final Class<T> type) throws Exception {
	return jsonSerialiser.readValue(representation.getStream(), type);
    }

    /**
     * Deserialises representation into a special map.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public Map<?, ?> restoreMap(final Representation representation) throws Exception {
        return serialiser.deserialise(representation.getStream(), Map.class);
    }

    public String getAppWidePublicKey() {
        return appWidePublicKey;
    }

    public void setAppWidePublicKey(final String publicKey) {
        this.appWidePublicKey = publicKey;
    }

    public String getAppWidePrivateKey() {
        return appWidePrivateKey;
    }

    public void setAppWidePrivateKey(final String privateKey) {
        this.appWidePrivateKey = privateKey;
    }

    public ISerialiser getSerialiser() {
        return serialiser;
    }

    /**
     * Creates representation (encoded by GZIP) for some input stream with particular media type.
     *
     * @param stream
     * @param mediaType
     * @return
     */
    public static Representation encodedRepresentation(final InputStream stream, final MediaType mediaType) {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(stream, mediaType));
    }

}
