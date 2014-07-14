package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.FileResource;

/**
 * The server resource that returns the content of the file;
 *
 * @author TG Team
 *
 */
public class FileResourceFactory extends Restlet {

    private final String fileName;
    private final MediaType mediaType;

    public FileResourceFactory(final String fileName, final MediaType mediaType) {
        this.fileName = fileName;
        this.mediaType = mediaType;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new FileResource(fileName, mediaType, getContext(), request, response).handle();
        }
    }
}
