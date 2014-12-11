package ua.com.fielden.platform.web.factories.webui;

import java.util.Date;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.SerialisationTestResource;

import com.google.inject.Injector;

/**
 * Resource factory for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 */
public class SerialisationTestResourceFactory extends Restlet {
    private final EntityFactory factory;
    private final RestServerUtil restUtil;
    private final Date testingDate;

    public SerialisationTestResourceFactory(final Injector injector) {
        this.factory = injector.getInstance(EntityFactory.class);
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.testingDate = new Date(); // this 'testing date' should be the same across all resources! (two resources will be created during test lifecycle -- one for GET request and one for POST)
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        // final String username = (String) request.getAttributes().get("username");
        // injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

        if (Method.POST == request.getMethod() || Method.GET == request.getMethod()) {
            final SerialisationTestResource resource = new SerialisationTestResource(factory, restUtil, getContext(), request, response, testingDate);
            resource.handle();
        }
    }
}
