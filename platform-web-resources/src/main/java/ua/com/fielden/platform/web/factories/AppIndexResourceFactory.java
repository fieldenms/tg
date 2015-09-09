package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.AppIndexResource;

public class AppIndexResourceFactory extends Restlet {

    private final IWebUiConfig app;

    public AppIndexResourceFactory(final IWebUiConfig webApp) {
        this.app = webApp;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            // browsers from mobile phones also send word Mobile as part of the agent info
            // however, both Android tablets and mobiles send work Android.
            // therefore, in case there would be a need to distinguish between tablets and mobiles the following condition would need to be enhanced
            // also, there was no testing done for iOS devices... Chrom on iOS would include word CriOS, but that is different for Safari...
            //            if (request.getClientInfo().getAgent().contains("Android") || request.getClientInfo().getAgent().contains("CriOS")) {
            //                new MobileAppIndexResource(app, getContext(), request, response).handle();
            //            } else {
            new AppIndexResource(app, getContext(), request, response).handle();
            //            }
        }
    }
}
