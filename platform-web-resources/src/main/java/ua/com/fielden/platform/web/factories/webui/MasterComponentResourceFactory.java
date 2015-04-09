package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The server resource factory for entity masters.
 *
 * @author TG Team
 *
 */
public class MasterComponentResourceFactory extends Restlet {
    private final IWebApp webApp;

    /**
     * Creates the {@link MasterComponentResourceFactory} instance with map of available entity masters.
     *
     * @param centres
     */
    public MasterComponentResourceFactory(final IWebApp webApp) {
        this.webApp = webApp;
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            final String entityTypeString = (String) request.getAttributes().get("entityType");
            final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
            final EntityMaster<? extends AbstractEntity<?>> master = this.webApp.getMasters().get(entityType);
            new MasterComponentResource(master, getContext(), request, response).handle();
        }
    }
}
