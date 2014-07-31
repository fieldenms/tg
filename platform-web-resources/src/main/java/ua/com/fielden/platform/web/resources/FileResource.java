package ua.com.fielden.platform.web.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class FileResource extends ServerResource {

    private final String fileName;
    private final MediaType mediaType;

    public FileResource(final String fileName, final MediaType mediaType, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.fileName = fileName;
        this.mediaType = mediaType;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            return new InputRepresentation(new FileInputStream(fileName), mediaType);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
