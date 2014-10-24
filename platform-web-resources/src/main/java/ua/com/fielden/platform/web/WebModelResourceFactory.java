package ua.com.fielden.platform.web;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.model.WebModel;
import ua.com.fielden.platform.web.resources.WebModelResource;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class WebModelResourceFactory extends Restlet {
    private final Map<String, WebModel> webModels;
    private final Map<String, String> generatedWebModels = new HashMap<>();

    /**
     * Creates the {@link WebModelResourceFactory} instance with map of available web models.
     *
     */
    public WebModelResourceFactory(final Map<String, WebModel> webModels) {
        this.webModels = webModels;
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            final String modelName = request.getAttributes().get("modelName").toString();
            if(!generatedWebModels.containsKey(modelName)) {
        	generatedWebModels.put(modelName, webModels.get(modelName).generate());
            }
            new WebModelResource(generatedWebModels.get(modelName), getContext(), request, response).handle();
        }
    }
}
