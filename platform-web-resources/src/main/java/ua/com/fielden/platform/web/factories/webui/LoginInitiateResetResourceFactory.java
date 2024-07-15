package ua.com.fielden.platform.web.factories.webui;

import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

import com.google.inject.Injector;
import com.google.inject.Key;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;

/**
 * A factory for a web resource that initiates the password recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginInitiateResetResourceFactory extends Restlet {
    
    private final Injector injector;
    
    public LoginInitiateResetResourceFactory(final Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        if (GET.equals(request.getMethod()) || POST.equals(request.getMethod())) {
            new LoginInitiateResetResource(
                    injector.getInstance(IWebResourceLoader.class),
                    injector.getInstance(Key.get(String.class, AppUri.class)),
                    injector.getInstance(IUniversalConstants.class),
                    injector.getInstance(ICompanionObjectFinder.class),
                    injector.getInstance(IUserProvider.class),
                    injector.getInstance(IDeviceProvider.class),
                    injector.getInstance(IDates.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
    
}