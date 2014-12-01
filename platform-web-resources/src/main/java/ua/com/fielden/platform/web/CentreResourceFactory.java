package ua.com.fielden.platform.web;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.CentreResource;

import com.google.inject.Injector;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {
    private final Map<String, EntityCentre> centres;
    private final Injector injector;
    private final ISerialiser serialiser;

    /**
     * Creates the {@link CentreResourceFactory} instance with map of available entity centres and {@link GlobalDomainTreeManager} instance (will be removed or enhanced later.)
     *
     * @param centres
     * @param injector
     */
    public CentreResourceFactory(final Map<String, EntityCentre> centres, final Injector injector) {
        this.centres = centres;
        this.injector = injector;
        this.serialiser = injector.getInstance(ISerialiser.class);
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));
        final IGlobalDomainTreeManager gdtm = injector.getInstance(IGlobalDomainTreeManager.class);

        if (Method.GET.equals(request.getMethod())) {
            new CentreResource(centres.get(request.getAttributes().get("centreName")), getContext(), request, response, gdtm, this.serialiser).handle();
        }
    }
}
