package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.file_reports.IReport;

/**
 * Resource for handling report requests.
 *
 * @author TG Team
 *
 */
public class ReportResource extends ServerResource {

    private final RestServerUtil restUtil;
    private final IReport dao;

    public ReportResource(final IReport dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(true);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.restUtil = restUtil;
    }

    /**
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of a list containing {@link EntityResultQueryModel}, a list of property
     * names and a list of corresponding titles.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
	try {
	    final List<?> list = restUtil.restoreList(envelope);
	    final String reportName = (String) list.get(0);
	    final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query = (QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>) list.get(1);
	    final Map<String, Object> params = (Map<String, Object>) list.get(2);

	    //getResponse().setEntity(new InputRepresentation(new ByteArrayInputStream(dao.getReport(reportName, query, params)), MediaType.APPLICATION_OCTET_STREAM));
	    return new InputRepresentation(new ByteArrayInputStream(dao.getReport(reportName, query, params)), MediaType.APPLICATION_OCTET_STREAM);
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    //getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	    return restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage());
	}
    }

}
