package ua.com.fielden.platform.web.factories.webui;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class MasterComponentResourceFactory extends Restlet {
    private final Map<String, String> masters;

    /**
     * Creates the {@link MasterComponentResourceFactory} instance with map of available entity centres and {@link GlobalDomainTreeManager} instance (will be removed or enhanced
     * later.)
     *
     * @param centres
     * @param injector
     */
    public MasterComponentResourceFactory(final Map<String, String> masters) {
        this.masters = masters;
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterComponentResource(masters.get(request.getAttributes().get("masterName")), getContext(), request, response).handle();
        }
    }
}
