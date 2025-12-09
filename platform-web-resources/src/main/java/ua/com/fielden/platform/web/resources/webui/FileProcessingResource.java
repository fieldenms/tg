package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.rx.observables.ProcessingProgressSubject;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.rx.eventsources.ProcessingProgressEventSource;
import ua.com.fielden.platform.web.sse.IEventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

/// This is a multipurpose file-processing resource that can be used for uploading files to be processed with the specified functional entity.
///
public class FileProcessingResource<T extends AbstractEntityWithInputStream<?>> extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger(FileProcessingResource.class);
    private static final String WARN_CLIENT_NOT_REGISTERED = "The client should have been registered for SSE communication.";

    protected final IEntityDao<T> companion;
    private final EntityFactory factory;
    private final Function<EntityFactory, T> entityCreator;
    private final RestServerUtil restUtil;
    private final ISerialiser serialiser;
    private final long sizeLimitBytes;
    private final Set<MediaType> types;
    private final IEventSourceEmitterRegister eseRegister;
    private final String jobUid;
    private final String sseUid;
    private final String origFileName;
    private final Date fileLastModified;
    private final String mimeAsProvided;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final User user;

    public FileProcessingResource(
            final IEventSourceEmitterRegister eseRegister,
            final IUserProvider userProvider,
            final IEntityDao<T> companion,
            final EntityFactory factory,
            final Function<EntityFactory, T> entityCreator,
            final RestServerUtil restUtil,
            final long fileSizeLimitBytes,
            final Set<MediaType> types,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ISerialiser serialiser,
            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);
        this.eseRegister = eseRegister;
        this.companion = companion;
        this.factory = factory;
        this.entityCreator = entityCreator;
        this.restUtil = restUtil;
        this.serialiser = serialiser;
        this.sizeLimitBytes = fileSizeLimitBytes;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
        this.types = types;

        try {
            this.jobUid = request.getHeaders().getFirstValue("jobUid", /*ignore case*/ true);
            this.sseUid = request.getHeaders().getFirstValue("sseUid", /*ignore case*/ true);
            this.user = userProvider.getUser();
            this.origFileName = URLDecoder.decode(request.getHeaders().getFirstValue("origFileName", /*ignore case*/ true), StandardCharsets.UTF_8);
            this.mimeAsProvided = request.getHeaders().getFirstValue("mime", /*ignore case*/ true);

            final long lastModified = Long.parseLong(request.getHeaders().getFirstValue("lastModified", /*ignore case*/ true));
            this.fileLastModified = new Date(lastModified);
        } catch (final Exception ex) {
            exhaustInputStream(request.getEntity());
            throw ex;
        }
    }

    /// Receives a file from a client.
    ///
    @Put
    public Representation receiveFile(final Representation input) throws IOException {
        String msg = "Successful processing.";
        try {
            final Representation rep;

            if (isEmpty(jobUid)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep =  restUtil.errorJsonRepresentation(msg = "jobUid is required.");
            } else if (isEmpty(origFileName)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep =  restUtil.errorJsonRepresentation(msg = "origFileName is missing, but is required.");
            } else  if (input == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep = restUtil.errorJsonRepresentation(msg = "The file content is empty, which is prohibited.");
            } else if (input.getMediaType() != null && !isMediaTypeSupported(input.getMediaType())) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep = restUtil.errorJsonRepresentation(msg = "Unexpected media type [%s].".formatted(input.getMediaType()));
            } else if (input.getSize() == -1) {
                getResponse().setStatus(Status.CLIENT_ERROR_LENGTH_REQUIRED);
                rep = restUtil.errorJsonRepresentation(msg = "File size is required.");
            } else if (input.getSize() > sizeLimitBytes) {
                getResponse().setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                rep = restUtil.errorJsonRepresentation(msg = "File is too large.");
            } else {
                final InputStream stream = input.getStream();
                rep = handleUndesiredExceptions(getResponse(), () -> tryToProcess(stream, getMime(input.getMediaType())), restUtil);
            }
            return rep;
        } finally {
            final String logMsg = "Processed file: %s, result: %s".formatted(this.origFileName, msg);
            LOGGER.debug(logMsg);
            exhaustInputStream(input);
        }
    }

    /// Need to exhaust the input stream representing a file being uploaded for processing.
    /// Not fully consumed streams could happen due to various errors (including validation) before or during their processing.
    /// Without consuming the input stream entirely, the client may get blocked waiting for the connection to be terminated by the server (e.g. by HAProxy).
    ///
    private void exhaustInputStream(final Representation input) {
        try {
            input.exhaust();
        } catch (final Exception ex) {
            LOGGER.error("Error while exhausting the input stream.", ex);
        }
    }

    private String getMime(final MediaType mediaType) {
        return mediaType != null ? mediaType.getName() : this.mimeAsProvided;
    }

    private boolean isMediaTypeSupported(final MediaType mediaType) {
        // simply checking types.contains(mediaType) may not be sufficient as there could be something like IMAGE/*
        final String mime = getMime(mediaType);
        return types.stream().anyMatch(mt -> matchMimeType(mt.getName(), mime));
    }

    private boolean matchMimeType(final String mime1, final String mime2) {
        final String[] mime1Parts = mime1.split("/");
        final String[] mime2Parts = mime2.split("/");
        final boolean anySubtype = "*".equals(mime1Parts[1]);
        return equalsEx(mime1Parts[0], mime2Parts[0]) && (equalsEx(mime1Parts[1], mime2Parts[1]) || anySubtype);
    }

    /// Creates an event source and connects it to an SSE emitter, which is associated with a client making the current request as identified by `sseUid`, to report file processing progress back to that client.
    /// Instantiates an entity that is responsible for file processing and executes it.
    ///
    /// @param stream -- a stream that represents a file to be processed.
    ///
    private Representation tryToProcess(final InputStream stream, final String mime) {

        final Optional<IEventSourceEmitter> maybeEmitter = getEmitterWithRetry();
        final ProcessingProgressSubject subject = new ProcessingProgressSubject();
        final ProcessingProgressEventSource eventSource = new ProcessingProgressEventSource(subject, jobUid, serialiser);

        try {
            maybeEmitter.ifPresent(eventSource::connect);
            final T entity = entityCreator.apply(factory);
            entity.setOrigFileName(origFileName);
            entity.setLastModified(fileLastModified);
            entity.setInputStream(stream);
            maybeEmitter.ifPresent(_ -> entity.setEventSourceSubject(subject));
            entity.setMime(mime);

            final T applied = saveRaw(entity);
            return restUtil.singleJsonRepresentation(applied);
        } finally {
            eventSource.disconnect();
        }
    }

    /// Attempts to retrieve an [IEventSourceEmitter] from the emitter registry with retries.
    ///
    /// If no emitter is found after retries, a warning is logged.
    ///
    /// @return an [Optional] containing the emitter if available; otherwise empty.
    ///
    private Optional<IEventSourceEmitter> getEmitterWithRetry() {
        try {
            int count = 1;
            IEventSourceEmitter emitter = eseRegister.getEmitter(user, sseUid);
            while (emitter == null && count <= 3) {
                count++;
                LOGGER.info("Retrying to get an SSE emitter [{}].", count);
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    LOGGER.warn("Thread sleep interrupted.", e);
                }
                emitter = eseRegister.getEmitter(user, sseUid);

            }
            if (emitter == null) {
                LOGGER.warn(WARN_CLIENT_NOT_REGISTERED);
            }
            return Optional.ofNullable(emitter);
        } catch (Exception ex) {
            LOGGER.error("Error while obtaining an SSE event source emitter.", ex);
            return Optional.empty();
        }
    }

    protected T saveRaw(final T entity) {
        return companion.save(entity);
    }

}