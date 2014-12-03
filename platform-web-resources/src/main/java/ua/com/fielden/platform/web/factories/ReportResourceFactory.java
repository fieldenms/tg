package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.ReportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Factory for producing {@link ReportResource}s.
 * 
 * @author TG Team
 * 
 */
public class ReportResourceFactory extends Restlet {

    private final RestServerUtil restServerUtil;

    private final IReportDaoFactory reportDaoFactory;

    private final Injector injector;

    @Inject
    public ReportResourceFactory(final IReportDaoFactory reportFactory, final RestServerUtil serverUtil, final Injector injector) {
        this.restServerUtil = serverUtil;
        this.reportDaoFactory = reportFactory;
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST.equals(request.getMethod())) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            new ReportResource(reportDaoFactory.createReportDao(), restServerUtil, getContext(), request, response).handle();
        }
    }

}
