package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.Pair;

/**
 * A web resource representing a list of dependencies.
 *
 * @author TG Team
 */
public class ReferenceDependencyListResource extends ServerResource {
    // the following properties are determined from request
    private final String username;

    private final RestServerUtil restUtil;
    private final File location;

    public ReferenceDependencyListResource(final String location, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

	this.location = new File(location);
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
    }

    /**
     * Handles GET requests for obtaining a list of dependencies.
     */
    @Get
    @Override
    public Representation get() {
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
