package ua.com.fielden.platform.web.resources.webui;

import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.resources.AttachmentTypeResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This resource should be used for uploading files.
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

    public FileProcessingResource(
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
        this.companion = companion;
        this.factory = factory;
        this.entityCreator = entityCreator;
        this.restUtil = restUtil;
        this.sizeLimit = fileSizeLimit;
        this.types = types;
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
        final T entity = entityCreator.apply(factory);
        entity.setInputStream(stream);
        final T applied = companion.save(entity);
        return restUtil.singleJSONRepresentation(applied);
    }

}
