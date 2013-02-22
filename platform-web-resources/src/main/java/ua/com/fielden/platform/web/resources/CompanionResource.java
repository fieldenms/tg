package ua.com.fielden.platform.web.resources;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;

public class CompanionResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final IComputationMonitor resource;

    public CompanionResource(final IComputationMonitor resource, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.restUtil = restUtil;
	this.resource = resource;
    }

    /**
     * Handles GET requests resulting from RAO call to {@link IEntityDao#findById(Long)}
     */
    @Get
    @Override
    public Representation get() {
	try {
	    final Integer progress = resource.progress();
	    return restUtil.resultRepresentation(Result.successful(progress)); //new StringRepresentation(progress == null ? "indefinite" : progress.toString());
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
	try {
	    resource.stop();
	    return restUtil.resultRepresentation(Result.successful("stopped"));
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	    final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
	    return restUtil.errorRepresentation(msg);
	}
    }

    @Delete
    @Override
    public Representation delete() {
	try {
	    getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
	    restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
	}

	return new StringRepresentation("delete");
    }

}
