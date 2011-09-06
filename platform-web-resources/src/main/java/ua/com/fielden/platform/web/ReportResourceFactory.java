package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.web.resources.ReportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Inject;

/**
 * Factory for producing {@link ReportResource}s.
 *
 * @author TG Team
 *
 */
public class ReportResourceFactory extends Restlet {

    private final RestServerUtil restServerUtil;

    private final IReportDaoFactory reportDaoFactory;

    @Inject
    public ReportResourceFactory(final IReportDaoFactory reportFactory, final RestServerUtil serverUtil) {
	this.restServerUtil = serverUtil;
	this.reportDaoFactory = reportFactory;
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	if (Method.POST.equals(request.getMethod())) {
	    new ReportResource(reportDaoFactory.createReportDao(), restServerUtil, getContext(), request, response).handlePost();
	}
    }

}
