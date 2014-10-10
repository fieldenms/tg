package ua.com.fielden.platform.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.FileResource;

/**
 * The server resource factory for {@link FileResource} that returns file to the client.
 *
 * @author TG Team
 *
 */
public class FileResourceFactory extends Restlet {

    private final List<String> resourcePaths;

    /**
     * Creates new {@link FileResourceFactory} instance with specified paths of file resources.
     *
     * @param resourcePaths
     */
    public FileResourceFactory(final Set<String> resourcePaths) {
	this.resourcePaths = new ArrayList<String>(resourcePaths);
	Collections.reverse(this.resourcePaths);
    }

    /**
     * Invoked on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new FileResource(Collections.unmodifiableList(resourcePaths), getContext(), request, response).handle();
        }
    }


}
