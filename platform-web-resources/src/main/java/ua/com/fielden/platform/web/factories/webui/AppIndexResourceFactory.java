package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AppIndexResource;

/**
 * The resource factory for main application 'html' resource (similar to 'index.html' in its classical meaning).
 *
 * @author TG Team
 *
 */
public class AppIndexResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final ICriteriaGenerator criteriaGenerator;
    
    public AppIndexResourceFactory(
            final IWebResourceLoader webResourceLoader, 
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICriteriaGenerator criteriaGenerator) {
        this.webResourceLoader = webResourceLoader;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
        this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            new AppIndexResource(webResourceLoader, webUiConfig, userProvider, deviceProvider, dates, criteriaGenerator, getContext(), request, response).handle();
        }
    }

}