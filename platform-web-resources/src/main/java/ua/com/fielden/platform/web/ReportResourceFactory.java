package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.ReportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * Factory for producing {@link ReportResource}s.
 * @author yura
 *
 */
public class ReportResourceFactory extends Restlet {

    private final RestServerUtil restServerUtil;

    private final IReportDaoFactory reportDaoFactory;

    public ReportResourceFactory(final Injector injector) {
	this.restServerUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
	this.reportDaoFactory = injector.getInstance(IReportDaoFactory.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	if (Method.POST.equals(request.getMethod())) {
	    new ReportResource(reportDaoFactory.createReportDao(), restServerUtil, getContext(), request, response).handlePost();
	}
    }

}
