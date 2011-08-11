package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.io.FileInputStream;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * A web resource for downloading a file representing an application dependency (e.g. a jar file, or a .properties file).
 * 
 * @author TG Team
 */
public class ReferenceDependencyDownloadResource extends Resource {
    // the following properties are determined from request
    private final String username;

    private final RestServerUtil restUtil;
    private final String location;
    private final String fileName;

    public ReferenceDependencyDownloadResource(final String location, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

	this.location = location;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	this.fileName = (String) request.getAttributes().get("file-name");
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
     * Handles GET requests for obtaining a file representing a dependency.
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}
	// process GET request
	try {
	    final File file = new File(location + "/" + fileName);
	    if (file.canRead()) {
		return new InputRepresentation(new FileInputStream(file), MediaType.APPLICATION_OCTET_STREAM);
	    } else {
		getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		return restUtil.errorRepresentation("Could not read file " + fileName);
	    }
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }
}
