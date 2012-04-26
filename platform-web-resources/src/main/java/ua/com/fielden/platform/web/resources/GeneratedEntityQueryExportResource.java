package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * Represents a web resource mapped to URI /export/generated-type.
 *
 * @author TG Team
 */
public class GeneratedEntityQueryExportResource extends Resource {
    // the following properties are determined from request
    private final String username;

    private final DynamicEntityDao dao;
    private final RestServerUtil restUtil;

    /**
     * The main resource constructor accepting a DAO instance in addition to the standard {@link Resource} parameters.
     *
     * @param dao
     * @param context
     * @param request
     * @param response
     */
    public GeneratedEntityQueryExportResource(final DynamicEntityDao dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
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
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of a list containing {@link QueryExecutionModel}, a list of property
     * names and a list of corresponding titles.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final List<?> list = restUtil.restoreList(envelope);
	    final QueryExecutionModel<?, EntityResultQueryModel<?>> qem = (QueryExecutionModel<?, EntityResultQueryModel<?>>) ((DynamicallyTypedQueryContainer)list.get(0)).getQem();
	    dao.setEntityType(qem.getQueryModel().getResultType());
	    final String[] propertyNames = (String[]) list.get(1);
	    final String[] propertyTitles = (String[]) list.get(2);
	    getResponse().setEntity(new InputRepresentation(new ByteArrayInputStream(dao.export(qem, propertyNames, propertyTitles)), MediaType.APPLICATION_OCTET_STREAM));
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	}
    }
}
