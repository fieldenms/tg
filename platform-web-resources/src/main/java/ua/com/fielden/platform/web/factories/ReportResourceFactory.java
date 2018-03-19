package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Inject;
import com.google.inject.Injector;

import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.ReportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

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
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;

    @Inject
    public ReportResourceFactory(final IReportDaoFactory reportFactory, final RestServerUtil serverUtil, final Injector injector) {
        this.restServerUtil = serverUtil;
        this.reportDaoFactory = reportFactory;
        this.injector = injector;
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.POST.equals(request.getMethod())) {
            final String username = (String) request.getAttributes().get("username");
            userProvider.setUsername(username, injector.getInstance(IUser.class));
            
            new ReportResource(reportDaoFactory.createReportDao(), restServerUtil, deviceProvider, getContext(), request, response).handle();
        }
    }

}
