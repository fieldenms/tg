package ua.com.fielden.platform.web.resources;

import java.io.File;
import java.io.FileInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * A web resource for downloading a file representing an application dependency (e.g. a jar file, or a .properties file).
 * 
 * @author TG Team
 */
public class ReferenceDependencyDownloadResource extends ServerResource {
    // the following properties are determined from request
    private final String username;

    private final RestServerUtil restUtil;
    private final String location;
    private final String fileName;

    public ReferenceDependencyDownloadResource(final String location, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        setNegotiated(false);
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));

        this.location = location;
        this.restUtil = restUtil;
        this.username = (String) request.getAttributes().get("username");
        this.fileName = (String) request.getAttributes().get("file-name");
    }

    /**
     * Handles GET requests for obtaining a file representing a dependency.
     */
    @Get
    @Override
    public Representation get() {
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
