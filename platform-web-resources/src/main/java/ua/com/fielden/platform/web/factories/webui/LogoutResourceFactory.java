package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.resources.webui.LogoutResource;

/**
 * A factory for a logout web resource.
 *
 * @author TG Team
 *
 */
public class LogoutResourceFactory extends Restlet {

    private final String domainName;
    private final String path;
    private final Injector injector;

    public LogoutResourceFactory(final String domainName, final String path, final Injector injector) {
        this.domainName = domainName;
        this.path = path;
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            
            final ICompanionObjectFinder coFinder = injector.getInstance(ICompanionObjectFinder.class);
            final IUser coUser = coFinder.find(User.class, true);
            
            new LogoutResource(
                    injector.getInstance(IWebResourceLoader.class),
                    injector.getInstance(IUserProvider.class),
                    coUser,
                    injector.getInstance(IUserSession.class),
                    domainName,
                    path,
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
