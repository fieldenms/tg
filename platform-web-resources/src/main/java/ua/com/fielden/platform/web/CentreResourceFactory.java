package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.web.resources.CentreResource;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {

    private final IEntityCentreConfigController eccc;
    private final ISerialiser serialiser;
    private final String username;

    public CentreResourceFactory(final IEntityCentreConfigController eccc, final ISerialiser serialiser, final String username) {
        this.eccc = eccc;
        this.serialiser = serialiser;
        this.username = username;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreResource(eccc, serialiser, getContext(), request, response, username).handle();
        }
    }
}
