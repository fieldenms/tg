package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;

/**
 * A web resource representing a list of dependencies.
 * 
 * @author TG Team
 */
public class ReferenceDependencyListResource extends Resource {
    // the following properties are determined from request
    private final String username;

    private final RestServerUtil restUtil;
    private final File location;

    public ReferenceDependencyListResource(final String location, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

	this.location = new File(location);
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
    }

    // //////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    // //////////////////////////////////////////////////////////////////
    @Override
    public boolean allowPost() {
	return false;
    }

    @Override
    public boolean allowGet() {
	return true;
    }

    /**
     * Handles GET requests for obtaining a list of dependencies.
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}
	// process GET request
	try {
	    final Map<String, Pair<String, Long>> map = Checksum.sha1(location);
	    final Result result = Result.successful(map);
	    return restUtil.resultRepresentation(result);
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }
}
