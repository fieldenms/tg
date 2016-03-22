package ua.com.fielden.platform.web.resources.webui;

import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.rx.observables.ProcessingProgressSubject;
import ua.com.fielden.platform.web.resources.AttachmentTypeResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.rx.eventsources.ProcessingProgressEventSrouce;
import ua.com.fielden.platform.web.sse.resources.EventSourcingResourceFactory;

/**
 * This resource should be used for uploading files to be processed with the specified functional entity.
 * 
 * Unlike {@link AttachmentTypeResource} it does not save or associated the uploaded file with any entity. Instead it passes that file into
 * 
 * @author TG Team
 * 
 */
public class FileProcessingResource<T extends AbstractEntityWithInputStream<?>> extends ServerResource {

    private final IEntityDao<T> companion;
    private final EntityFactory factory;
    private final Function<EntityFactory, T> entityCreator;
    private final RestServerUtil restUtil;
    private final long sizeLimit;
    private final Set<MediaType> types;
    private final Router router;
    private final String jobUid;

    public FileProcessingResource(
            final Router router, 
            final IEntityDao<T> companion, 
            final EntityFactory factory, 
            final Function<EntityFactory, T> entityCreator, 
            final RestServerUtil restUtil, 
            final long fileSizeLimit, 
            final Set<MediaType> types, 
            final Context context, 
            final Request request, 
            final Response response) {
        init(context, request, response);
        this.router = router;
        this.companion = companion;
        this.factory = factory;
        this.entityCreator = entityCreator;
        this.restUtil = restUtil;
        this.sizeLimit = fileSizeLimit;
        this.types = types;
        
        this.jobUid = request.getHeaders().getFirstValue("jobUid");
        if (StringUtils.isEmpty(jobUid)) {
            throw new IllegalArgumentException("jobUid is required");
        }
        
    }

    /**
     * Receives a file
     * 
     */
    @Put
    public Representation receiveFile(final Representation entity) throws Exception {
        final Representation response;
        if (entity == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorJSONRepresentation("There is nothing to process");
        } else if (!types.contains(entity.getMediaType())) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorJSONRepresentation("Unexpected media type.");
        } else if (entity.getSize() == -1) {
            getResponse().setStatus(Status.CLIENT_ERROR_LENGTH_REQUIRED);
            return restUtil.errorJSONRepresentation("File size is required.");
        } else if (entity.getSize() > sizeLimit) {
            getResponse().setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
            return restUtil.errorJSONRepresentation("File is too large.");
        } else {
            final InputStream stream = entity.getStream();
            response = EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> tryToProcess(stream), restUtil);
        }

        return response;
    }

    private Representation tryToProcess(final InputStream stream) {
        final ProcessingProgressSubject subject = new ProcessingProgressSubject();
        final EventSourcingResourceFactory eventSource = new EventSourcingResourceFactory(new ProcessingProgressEventSrouce(subject));
        final String baseUri = getRequest().getResourceRef().getPath(true);
        router.attach(baseUri + "/" + jobUid, eventSource);
        
        try {
            final T entity = entityCreator.apply(factory);
            entity.setInputStream(stream);
            entity.setEventSourceSubject(subject);

            final T applied = companion.save(entity);
            return restUtil.singleJSONRepresentation(applied);
        } finally {
            router.detach(eventSource);
        }
    }

}
