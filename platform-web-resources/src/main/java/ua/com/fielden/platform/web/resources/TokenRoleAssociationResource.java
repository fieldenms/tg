package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.security.provider.ISecurityTokenController;

/**
 * A resource responsible for managing associations between user roles and security tokens. Supported methods:
 * <ul>
 * <li>GET -- accepts token class name and returns a list of roles assoicted with it; expected URI is ../securitytokens/{token}/useroles.
 * <li>POST -- accepts a map between token class names and lists of role ids, which results in persisting such associations; expected URI is ../securitytokens.
 * </ul>
 * <p>
 *
 * @author TG Team
 */
public class TokenRoleAssociationResource extends ServerResource {
    private final String username; // TODO to be used for auditing purposes

    private final ISecurityTokenController controller;
    private final RestServerUtil restUtil;

    /**
     * Principle constructor.
     */
    public TokenRoleAssociationResource(final ISecurityTokenController controller, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.controller = controller;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Handles GET requests, which should return roles associated with the specified security token.
     */
    @Get
    @Override
    public Representation get() {
	// process GET request
	try {
	    return restUtil.mapRepresentation(controller.findAllAssociations());
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }
}
