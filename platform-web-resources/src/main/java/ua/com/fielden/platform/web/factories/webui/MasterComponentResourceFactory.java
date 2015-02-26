package ua.com.fielden.platform.web.factories.webui;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class MasterComponentResourceFactory extends Restlet {
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> masters;

    /**
     * Creates the {@link MasterComponentResourceFactory} instance with map of available entity centres and {@link GlobalDomainTreeManager} instance (will be removed or enhanced
     * later.)
     *
     * @param centres
     * @param injector
     */
    public MasterComponentResourceFactory(final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> masters) {
        this.masters = masters;
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
            final EntityMaster<? extends AbstractEntity<?>> master = this.masters.get(entityType);
            new MasterComponentResource(master, getContext(), request, response).handle();
        }
    }
}
