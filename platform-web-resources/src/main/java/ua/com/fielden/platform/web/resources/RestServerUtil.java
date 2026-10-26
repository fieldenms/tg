package ua.com.fielden.platform.web.resources;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.continuation.NeedMoreDataException;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.utils.StreamCouldNotBeResolvedException;
import ua.com.fielden.platform.web.resources.exceptions.InvalidRequestBodyException;
import ua.com.fielden.platform.web_api.IWebApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;

/// Utilities used in the implementation of web-resources.
///
public class RestServerUtil {

    public static final String ERR_INVALID_REQUEST_BODY = "The request could not be processed, as its content is invalid.";

    private static final String ERR_COULD_NOT_FIND_ENTITY = "Could not find entity.";

    private final Logger logger = LogManager.getLogger(RestServerUtil.class);
    private final ISerialiser serialiser;

    @Inject
    public RestServerUtil(final ISerialiser serialiser) {
        this.serialiser = serialiser;
    }

    /// Creates a JSON representation of a [Result] whose cause will have the message `string`.
    ///
    public Representation errorJsonRepresentation(final String string) {
        // logger.debug("Start building error JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(failure(new Exception(string)), SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length */);
    }

    /// Creates a representation of a [Result] whose cause will have the message `string`.
    ///
    public Representation errorRepresentation(final String string) {
        // logger.debug("Start building error representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(failure(new Exception(string)));
        // logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /// Creates a JSON representation of a [Result] reporting the error designated by `ex`.
    ///
    public Representation errorJSONRepresentation(final Exception ex) {
        // logger.debug("Start building error JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(ex instanceof Result ? ex : failure(ex), SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /*, bytes.length*/);
    }

    /// Creates a representation of a [Result].
    ///
    public Representation resultRepresentation(final Result result) {
        // logger.debug("Start building result representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result);
        // logger.debug("SIZE: " + bytes.length);
        return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /// Creates a JSON representation of a [Result].
    ///
    public Representation resultJSONRepresentation(final Result result) {
        // logger.debug("Start building result JSON representation:" + new DateTime());
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON /* , bytes.length */);
    }

    /// Creates a JSON representation of a list of entities.
    ///
    public <T extends AbstractEntity<?>> Representation listJsonRepresentation(final List<T> entities) {
        // logger.debug("Start building JSON entities representation.");
        if (entities == null) {
            throw new IllegalArgumentException("The provided list of entities is null.");
        }
        // create a Result enclosing entity list
        final Result result = successful(new ArrayList<>(entities));
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /// Creates a representation of a list of entities with `customObject`, serialising them without properties `id` and `version`.
    ///
    /// @param customObject  a map of custom properties to be included in the result
    ///
    public <T extends AbstractEntity<?>> Representation listJsonRepresentationWithoutIdAndVersion(
            final List<T> entities,
            final Map<String, Object> customObject)
    {
        if (entities == null) {
            throw new IllegalArgumentException("The provided list of entities is null.");
        }
        // create a Result enclosing entity list and customObject
        final ArrayList<Object> resultantList = new ArrayList<>(entities);
        resultantList.add(customObject);
        final Result result = successful(resultantList);
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

    /// Creates a representation of a list of entities.
    ///
    public Representation rawListJsonRepresentation(final Object... objects) {
        // logger.debug("Start building JSON list representation.");
        if (objects.length <= 0) {
            throw new IllegalArgumentException("Empty objects.");
        }
        // create a Result enclosing entity list
        final Result result = successful(new ArrayList<>(Arrays.asList(objects)));
        final byte[] bytes = serialiser.serialise(result, SerialiserEngines.JACKSON);
        // logger.debug("SIZE: " + bytes.length);
        return encodedRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_JSON);
    }

    /// Creates a representation of an [IWebApi] execution result.
    ///
    public Representation webApiResultRepresentation(final Map<String, Object> result) {
        return encodedRepresentation(new ByteArrayInputStream(serialiser.serialise(result, JACKSON)), APPLICATION_JSON);
    }

    /// Creates a [Result] from `entity`.
    ///
    public <T extends AbstractEntity<?>> Result singleEntityResult(final T entity) {
        // create a Result enclosing entity list
        final Result result;
        if (entity != null) {
            // valid and invalid entities: both kinds are represented using successful result. Use client-side isValid() method
            //   in 'tg-reflector' to differentiate them
            result = successful(entity);
        } else {
            result = failure(new Exception(ERR_COULD_NOT_FIND_ENTITY));
        }
        return result;
    }

    /// Creates a [SerialiserEngines#JACKSON] representation of an entity.
    ///
    public <T extends AbstractEntity<?>> Representation singleJsonRepresentation(final T entity) {
        final byte[] bytes = serialiser.serialise(singleEntityResult(entity), JACKSON);
        return encodedRepresentation(new ByteArrayInputStream(bytes), APPLICATION_JSON);
    }

    /// Creates a [Result] from an entity with an exception.
    ///
    public <T extends AbstractEntity<?>> Result singleEntityResult(final T entity, final Optional<Exception> savingException) {
        final Result result;
        if (entity != null && savingException.isPresent()) {
            final Exception ex = savingException.get();
            if (ex instanceof Result) {
                final Result thrownResult = (Result) ex;
                if (thrownResult.isSuccessful()) {
                    throw failure("The successful result [%s] was thrown during unsuccessful saving of entity with id [%s] of type [%s]. This is most likely a programming error.".formatted(thrownResult, entity.getId(), entity.getClass().getSimpleName()));
                }

                // we don't want continuation related exceptions to pollute the log with errors and stack traces – simply log an informative message
                if (ex instanceof NeedMoreData continuationEx) {
                    if (continuationEx.getEx() instanceof NeedMoreDataException nmdEx) {
                        logger.debug(() -> "NMD Continuation: %s, %s, %s".formatted(nmdEx.getMessage(), nmdEx.continuationTypeStr, nmdEx.continuationProperty));
                    } else { // just in case...
                        logger.debug(() -> "NMD Continuation: %s".formatted(continuationEx.getMessage()));
                    }
                } else {
                    // iterate over properties in search of the first invalid one (without required checks)
                    final Optional<Result> firstFailure = entity.nonProxiedProperties().filter(mp -> mp.getFirstFailure() != null).findFirst().map(MetaProperty::getFirstFailure);

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
                }
                result = thrownResult.copyWith(entity);
                logger.debug(() -> """
                                   The unsuccessful result [%s] was thrown during unsuccesful saving of entity with id [%s] of type [%s]. \
                                   Its instance will be overridden by entity with id [%s] to be able to bind the entity to respective master."""
                                .formatted(thrownResult, entity.getId(), entity.getClass().getSimpleName(), entity.getId()));
            } else {
                logger.error(ex.getMessage(), ex);
                result = failure(entity, ex);
            }
        } else {
            result = singleEntityResult(entity);
        }
        return result;
    }

    /// Converts representation of [QueryExecutionModel] to an actual instance.
    ///
    public QueryExecutionModel<?, ?> restoreQueryExecutionModel(final Representation representation) {
        return serialiser.deserialise(getStream(representation), QueryExecutionModel.class);
    }

    /// Converts representation to an instance of [LifecycleQueryContainer].
    ///
    public LifecycleQueryContainer restoreLifecycleQueryContainer(final Representation representation) {
        return serialiser.deserialise(getStream(representation), LifecycleQueryContainer.class);
    }

    /// Converts a [List] representation into a [List] instance.
    ///
    public List<?> restoreList(final Representation representation) {
        return serialiser.deserialise(getStream(representation), List.class);
    }

    /// Converts a [Result] JSON representation into a [Result] instance.
    ///
    public Result restoreJsonResult(final Representation representation) {
        return restoreRequestBody(representation, stream -> serialiser.deserialise(stream, Result.class, SerialiserEngines.JACKSON));
    }

    /// Converts an entity representation into an entity instance.
    ///
    /// @param type  the type of the represented entity
    ///
    public <T extends AbstractEntity<?>> T restoreEntity(final Representation representation, final Class<T> type) {
        return serialiser.deserialise(getStream(representation), type);
    }

    /// Converts an entity JSON representation into an entity instance.
    ///
    /// @param type  the type of the represented entity
    ///
    public <T extends AbstractEntity<?>> T restoreJsonEntity(final Representation representation, final Class<T> type) {
        return restoreRequestBody(representation, stream -> serialiser.deserialise(stream, type, SerialiserEngines.JACKSON));
    }

    /// Deserialises representation into a special map.
    ///
    public Map<?, ?> restoreMap(final Representation representation) {
        return restoreRequestBody(representation, stream -> serialiser.deserialise(stream, Map.class));
    }

    /// Deserialises JSON representation of JS object into Java map.
    ///
    public Map<?, ?> restoreJsonMap(final Representation representation) {
        return restoreRequestBody(representation, stream -> serialiser.deserialise(stream, Map.class, SerialiserEngines.JACKSON));
    }

    /// Deserialises the body of a request with `deserialiser`.
    ///
    /// If the body cannot be deserialised, the failure is logged, and [InvalidRequestBodyException] with a generic message is thrown.
    /// The details of the failure, such as the types and properties involved, stay on the server, as the exception is serialised to the client.
    ///
    private <T> T restoreRequestBody(final Representation representation, final Function<InputStream, T> deserialiser) {
        try {
            return deserialiser.apply(getStream(representation));
        } catch (final RuntimeException ex) {
            logger.warn("The request body could not be deserialised.", ex);
            throw new InvalidRequestBodyException(ERR_INVALID_REQUEST_BODY);
        }
    }

    private static InputStream getStream(final Representation representation) {
        try {
            return representation.getStream();
        } catch (final IOException ioException) {
            throw new StreamCouldNotBeResolvedException(String.format("The stream could not be resolved from representation [%s].", representation), ioException);
        }
    }

    public ISerialiser getSerialiser() {
        return serialiser;
    }

    /// Creates a representation (GZIP-encoded) for `stream` with `mediaType`.
    ///
    public static Representation encodedRepresentation(final InputStream stream, final MediaType mediaType) {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(stream, mediaType));
    }

}
