package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

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
public class ReportResource extends Resource {

    private final RestServerUtil restUtil;
    private final IReport dao;
    private final String username;

    public ReportResource(final IReport dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	dao.setUsername(username);
    }

    // //////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    // //////////////////////////////////////////////////////////////////
    @Override
    public boolean allowPost() {
	return true;
    }

    @Override
    public boolean allowGet() {
	return false;
    }

    /**
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of a list containing {@link EntityResultQueryModel}, a list of property
     * names and a list of corresponding titles.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final List<?> list = restUtil.restoreList(envelope);
	    final String reportName = (String) list.get(0);
	    final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query = (QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>) list.get(1);
	    final Map<String, Object> params = (Map<String, Object>) list.get(2);

	    getResponse().setEntity(new InputRepresentation(new ByteArrayInputStream(dao.getReport(reportName, query, params)), MediaType.APPLICATION_OCTET_STREAM));
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	}
    }

}
