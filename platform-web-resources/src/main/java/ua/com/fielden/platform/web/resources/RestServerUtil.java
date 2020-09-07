package ua.com.fielden.platform.web.resources;

import static java.lang.String.format;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;

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
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.snappy.SnappyQuery;
import ua.com.fielden.platform.utils.StreamCouldNotBeResolvedException;

/**
 * This is a convenience class providing some common routines used in the implementation of web-resources.
 *
 * @author TG Team
 *
 */
public class RestServerUtil {
    private static final String HEADERS_KEY = "org.restlet.http.headers";

    private final ISerialiser serialiser;
    private final Logger logger = Logger.getLogger(RestServerUtil.class);

    @Inject
    public RestServerUtil(final ISerialiser serialiser) {
        this.serialiser = serialiser;
    }

    private static Series<Header> getMessageHeaders(final Message message) {
        final ConcurrentMap<String, Object> attrs = message.getAttributes();
        Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
        if (headers == null) {
            headers = new Series<>(Header.class);
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
    public Representation errorJsonRepresentation(final String string) {
        // logger.debug("Start building error JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(new Result(null, new Exception(string)), SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length */);
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final String string) {
        // logger.debug("Start building error representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(new Result(null, new Exception(string)));
        // logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final Exception ex) {
        // logger.debug("Start building error representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(new Result(ex));
        // logger.debug("SIZE: " + bytes.length);
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
        // logger.debug("Start building error JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(ex instanceof Result ? ex : new Result(ex), SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation resultRepresentation(final Result result) {
        // logger.debug("Start building result representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result);
        // logger.debug("SIZE: " + bytes.length);
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
        // logger.debug("Start building result JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* , bytes.length */);
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation snappyResultRepresentation(final List filteredEntities) {
        // logger.debug("Start building snappy result representation:" + new DateTime());
        try {
            // create a Result enclosing entity list
            final byte[] bytes = serialiser.serialise(new Result(new ArrayList(filteredEntities), "Snappy pair is Ok"));
            // logger.debug("SIZE: " + bytes.length);
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
        // logger.debug("Start building entities representation.");
        try {
            // create a Result enclosing entity list
            final Result result = new Result(new ArrayList<>(entities), "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            // logger.debug("SIZE: " + bytes.length);
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
    public <T extends AbstractEntity<?>> Representation listJsonRepresentation(final List<T> entities) {
        // logger.debug("Start building JSON entities representation.");
        if (entities == null) {
            throw new IllegalArgumentException("The provided list of entities is null.");
        }
        // create a Result enclosing entity list
        final Result result = new Result(new ArrayList<>(entities), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /**
     * Composes representation of a list of entities, serialising them without id / version properties.
     *
     * @return
     */
    public <T extends AbstractEntity<?>> Representation listJsonRepresentationWithoutIdAndVersion(final List<T> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("The provided list of entities is null.");
        }
        // create a Result enclosing entity list
        final Result result = new Result(new ArrayList<>(entities), "All is cool");
        EntitySerialiser.getContext().setExcludeIdAndVersion(true);
        try {
            final byte[] bytes = serialiser.serialise(result, JACKSON);
            EntitySerialiser.getContext().setExcludeIdAndVersion(false);
            return encodedRepresentation(new ByteArrayInputStream(bytes), APPLICATION_JSON);
        } catch (final Throwable t) {
            EntitySerialiser.getContext().setExcludeIdAndVersion(false);
            throw t;
        }
    }

    /**
     * Composes representation of a list of entities.
     *
     * @return
     */
    public Representation rawListJsonRepresentation(final Object... objects) {
        // logger.debug("Start building JSON list representation.");
        if (objects.length <= 0) {
            throw new IllegalArgumentException("Empty objects.");
        }
        // create a Result enclosing entity list
        final Result result = new Result(new ArrayList<>(Arrays.asList(objects)), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /**
     * Composes representation of a map.
     *
     * @return
     */
    public Representation mapJsonRepresentation(final Map<?, ?> map) {
        // logger.debug("Start building JSON map representation.");
        // create a Result enclosing map
        final Result result = new Result(new LinkedHashMap<>(map), "All is cool");
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /**
     * Composes representation of a map.
     *
     * @return
     */
    public Representation mapRepresentation(final Map<?, ?> map) {
        // logger.debug("Start building map representation.");
        try {
            // create a Result enclosing entity list
            final Result result = new Result(new HashMap<>(map), "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            // logger.debug("SIZE: " + bytes.length);
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
    public <T extends AbstractEntity<?>> Representation lifecycleRepresentation(final LifecycleModel<T> lifecycleModel) {
        // logger.debug("Start building lifecycle representation:" + new DateTime());
        try {
            // create a Result enclosing lifecycle data
            final Result result = new Result(lifecycleModel, "All is cool");
            final byte[] bytes = serialiser.serialise(result);
            // logger.debug("SIZE: " + bytes.length);
            return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
        } catch (final Exception ex) {
            logger.error(ex);
            return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
        }
    }

    /**
     * Composes serialised representation of the <code>entity</code>.
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
    public <T extends AbstractEntity<?>> Representation singleJsonRepresentation(final T entity) {
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
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /**
     * Composes JACKSON representation of an master information entity.
     *
     * @return
     * @throws JsonProcessingException
     */
    public <T extends AbstractEntity<?>> Representation singleJsonMasterRepresentation(final T entity, final String entityType) {
        // create a Result enclosing entity list
        final Result result;
        if (entity != null) {
            // valid and invalid entities: both kinds are represented using successful result. Use client-side isValid() method
            //   in 'tg-reflector' to differentiate them
            result = successful(entity);
        } else {
            result = failuref("Could not find master for entity type: %s.", entityType);
        }
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /**
     * Composes JACKSON representation of an entity with exception.
     *
     * @return
     * @throws JsonProcessingException
     */
    public <T extends AbstractEntity<?>> Representation singleJsonRepresentation(final T entity, final Optional<Exception> savingException) {
        if (entity != null && savingException.isPresent()) {
            final Exception ex = savingException.get();
            final Result result;
            if (ex instanceof Result) {
                final Result thrownResult = (Result) ex;
                if (thrownResult.isSuccessful()) {
                    throw failure(format("The successful result [%s] was thrown during unsuccesful saving of entity with id [%s] of type [%s]. This is most likely programming error.", thrownResult, entity.getId(), entity.getClass().getSimpleName()));
                }

                // iterate over properties in search of the first invalid one (without required checks)
                final Optional<Result> firstFailure = entity.nonProxiedProperties()
                .filter(mp -> mp.getFirstFailure() != null)
                .findFirst().map(mp -> mp.getFirstFailure());

                // returns first failure if exists or successful result if there was no failure.
                final Result isValid = firstFailure.orElse(successful(entity));
                if (ex != isValid) {
                    // Log the server side error only in case where exception, that was thrown, does not equal to validation result of the entity (by reference).
                    // Please, note that Results, that are thrown in companion objects, often represents validation results of some complimentary entities during saving.
                    // For example, see ServiceRepairSubmitActionDao save method, which internally invokes saveWorkOrder(serviceRepair) method of ServiceRepairDao, where during saving of workOrder
                    //  some validation result is thrown.
                    // In these cases -- server error log should report the saving error.
                    logger.error(ex.getMessage(), ex);
                }
                result = thrownResult.copyWith(entity);
                logger.warn(format("The unsuccessful result [%s] was thrown during unsuccesful saving of entity with id [%s] of type [%s]. Its instance will be overridden by the entity with id [%s] to be able to bind the entity to respective master.", thrownResult, entity.getId(), entity.getClass().getSimpleName(), entity.getId()));
            } else {
                logger.error(ex.getMessage(), ex);
                result = failure(entity, ex);
            }
            final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
            return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* TODO , bytes.length*/);
        } else {
            return singleJsonRepresentation(entity);
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
    public Result restoreJsonResult(final Representation representation) {
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
    public <T extends AbstractEntity<?>> T restoreEntity(final Representation representation, final Class<T> type) {
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
    public <T extends AbstractEntity<?>> T restoreJsonEntity(final Representation representation, final Class<T> type) {
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
    public Map<?, ?> restoreJsonMap(final Representation representation) {
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
