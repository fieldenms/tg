package ua.com.fielden.platform.web.resources;

import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Resource;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Provides information about the application.
 *
 * @author TG Team
 */
public class InfoResource extends ServerResource {
    // the following properties are determined from request
    private final String username;
    private final String applicationInfo;

    /**
     * The main resource constructor accepting a DAO instance and an entity factory in addition to the standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is required for enhancement of entities provided in request envelopes.
     *
     * @param dao
     * @param factory
     * @param context
     * @param request
     * @param response
     */
    public InfoResource(final String applicationInfo, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.applicationInfo = applicationInfo;
	this.username = (String) request.getAttributes().get("username");
    }

    /**
     * Handles GET requests resulting from RAO call to {@link IEntityDao#findById(Long)}.
     */
    @Get
    @Override
    public Representation get() {
	final StringBuffer buff = new StringBuffer();
	buff.append("<html>");
	buff.append(applicationInfo);
	buff.append("<br/></br><small>The information has been requested by ");
	buff.append(username);
	buff.append(" at ");
	buff.append(new DateTime() + ":<br/>");
	buff.append("<ul>");
	buff.append("<li>Address " + getRequest().getClientInfo().getAddress());
	buff.append("<li>Agent " + getRequest().getClientInfo().getAgentName() + " " + getRequest().getClientInfo().getAgentVersion());
	buff.append("</ul>");
	buff.append("</small>");
	buff.append("<html>");
	return new StringRepresentation(buff, MediaType.TEXT_HTML, Language.ALL, CharacterSet.UTF_8);
    }
}
