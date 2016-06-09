package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.snappy.SnappyQuery;
import ua.com.fielden.platform.utils.StreamCouldNotBeResolvedException;

/**
 * This is a convenience class providing some common routines used in the implementation of web-resources.
 *
 * @author TG Team
 *
 */
public class RestServerUtil {

    private final ISerialiser serialiser;

    @Inject
    public RestServerUtil(final ISerialiser serialiser) {
        this.serialiser = serialiser;
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
    public Representation errorJSONRepresentation(final String string) {
        logger.debug("Start building error JSON representation:" + new DateTime());
        byte[] bytes = new byte[0];
        bytes = serialiser.serialise(new Result(null, new Exception(string)), SerialiserEngines.JACKSON);
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
    public Representation errorJSONRepresentation(final Exception ex) {
        logger.debug("Start building error JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(ex instanceof Result ? ex : new Result(ex), SerialiserEngines.JACKSON);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
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
    public Representation resultJSONRepresentation(final Result result) {
        logger.debug("Start building result JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* , bytes.length */);
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
     * Composes representation of a list of entities.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation listJSONRepresentation(final List<T> entities) {
        logger.debug("Start building JSON entities representation.");
        if (entities == null) {
            throw new IllegalArgumentException("The provided list of entities is null.");
        }
        // create a Result enclosing entity list
        final Result result = new Result(new ArrayList<T>(entities), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
    }

    /**
     * Composes representation of a list of entities.
     *
     * @return
     */
    public Representation rawListJSONRepresentation(final Object... objects) {
        logger.debug("Start building JSON list representation.");
        if (objects.length <= 0) {
            throw new IllegalArgumentException("Empty objects.");
        }
        // create a Result enclosing entity list
        final Result result = new Result(new ArrayList<>(Arrays.asList(objects)), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
    }

    /**
     * Composes representation of a map.
     *
     * @return
     */
    public Representation mapJSONRepresentation(final Map<?, ?> map) {
        logger.debug("Start building JSON map representation.");
        // create a Result enclosing map
        final Result result = new Result(new LinkedHashMap<>(map), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
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
    public <T extends AbstractEntity<?>> Representation singleJSONRepresentation(final T entity) {
        // create a Result enclosing entity list
        final Result result;
        if (entity != null) {
            // valid and invalid entities: both kinds are represented using successful result. Use client-side isValid() method
            //   in 'tg-reflector' to differentiate them
            result = new Result(entity, "OK");
        } else {
            result = new Result(null, new Exception("Could not find entity."));
        }
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* TODO , bytes.length*/);
    }
    
    /**
     * Composes JACKSON representation of an entity with exception.
     *
     * @return
     * @throws JsonProcessingException
     */
    public <T extends AbstractEntity<?>> Representation singleJSONRepresentation(final T entity, final Optional<Exception> savingException) {
        if (entity != null && savingException.isPresent()) {
            final Exception ex = savingException.get();
            final Result result;
            if (ex instanceof Result) {
                final Result thrownResult = (Result) ex;
                if (thrownResult.isSuccessful()) {
                    throw Result.failure(String.format("The successful result [%s] was thrown during unsuccesful saving of entity [%s]. This is most likely programming error.", thrownResult, entity));
                }
                if (ex != entity.isValid()) {
                    // Log the server side error only in case where exception, that was thrown, does not equal to validation result of the entity (by reference).
                    // Please, note that the Results, that are thrown in companion objects, often represents validation results of some complimentary entities during saving.
                    // For example, see ServiceRepairSubmitActionDao save method, which internally invokes saveWorkOrder(serviceRepair) method of ServiceRepairDao, where during saving of workOrder
                    //  some validation result is thrown.
                    // In these cases -- server error log will appear about saving error.
                    logger.error(ex.getMessage(), ex);
                }
                result = thrownResult.copyWith(entity);
                logger.warn(String.format("The unsuccessful result [%s] was thrown during unsuccesful saving of entity [%s]. Its instance [%s] will be overridden with the [%s] entity to be able to bind the entity to respective master.", thrownResult, entity, thrownResult.getInstance(), entity));
            } else {
                logger.error(ex.getMessage(), ex);
                result = new Result(entity, ex);
            }
            final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
            return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* TODO , bytes.length*/);
        } else {
            return singleJSONRepresentation(entity);
        }
    }

    /**
     * Converts representation of {@link QueryExecutionModel} to an actual instance.
     *
     * @param representation
     * @return
     */
    public QueryExecutionModel<?, ?> restoreQueryExecutionModel(final Representation representation) {
        return serialiser.deserialise(getStream(representation), QueryExecutionModel.class);
    }

    /**
     * Converts representation of {@link DynamicallyTypedQueryContainer} to an instance of {@link QueryExecutionModel}.
     *
     * @param <T>
     * @param representation
     * @return
     */
    public QueryExecutionModel<?, ?> restoreQueryExecutionModelForGeneratedType(final Representation representation) {
        return serialiser.deserialise(getStream(representation), DynamicallyTypedQueryContainer.class).getQem();
    }

    /**
     * Converts representation to an instance of {@link LifecycleQueryContainer}.
     *
     * @param representation
     * @return
     */
    public LifecycleQueryContainer restoreLifecycleQueryContainer(final Representation representation) {
        return serialiser.deserialise(getStream(representation), LifecycleQueryContainer.class);
    }

    public SnappyQuery restoreSnappyQuery(final Representation representation) {
        return serialiser.deserialise(getStream(representation), SnappyQuery.class);
    }

    /**
     * Converts representation of the export request representation in to a list.
     *
     * @param representation
     * @return
     */
    public List<?> restoreList(final Representation representation) {
        return serialiser.deserialise(getStream(representation), List.class);
    }

    /**
     * Converts representation of the export request representation in to result.
     *
     * @param representation
     * @return
     */
    public Result restoreJSONResult(final Representation representation) {
        return serialiser.deserialise(getStream(representation), Result.class, SerialiserEngines.JACKSON);
    }

    /**
     * Converts representation of an entity to an actual instance.
     *
     * @param <T>
     * @param <K>
     * @param representation
     * @param type
     * @return
     */
    public <T extends AbstractEntity> T restoreEntity(final Representation representation, final Class<T> type) {
        return serialiser.deserialise(getStream(representation), type);
    }

    /**
     * Converts JSON representation of an entity to an actual instance.
     *
     * @param <T>
     * @param <K>
     * @param representation
     * @param type
     * @return
     */
    public <T extends AbstractEntity> T restoreJSONEntity(final Representation representation, final Class<T> type) {
        return serialiser.deserialise(getStream(representation), type, SerialiserEngines.JACKSON);
    }

    /**
     * Deserialises representation into a special map.
     *
     * @param representation
     * @return
     */
    public Map<?, ?> restoreMap(final Representation representation) {
        return serialiser.deserialise(getStream(representation), Map.class);
    }

    /**
     * Deserialises JSON representation of JS object into Java map.
     *
     * @param representation
     * @return
     */
    public Map<?, ?> restoreJSONMap(final Representation representation) {
        return serialiser.deserialise(getStream(representation), Map.class, SerialiserEngines.JACKSON);
    }
    
    private final static InputStream getStream(final Representation representation) {
        try {
            return representation.getStream();
        } catch (final IOException ioException) {
            throw new StreamCouldNotBeResolvedException(String.format("The stream could not be resolved from representation [%s].", representation), ioException);
        }
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
