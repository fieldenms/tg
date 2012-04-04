package ua.com.fielden.platform.web.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.UserRole;

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
public class SecurityTokenResource extends Resource {
    private final String username; // TODO to be used for auditing purposes

    private final ISecurityTokenController controller;
    private final IUserRoleDao userRoleDao;
    private final RestServerUtil restUtil;

    private final Class<? extends ISecurityToken> token;

    ////////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    ////////////////////////////////////////////////////////////////////
    @Override
    public boolean allowGet() {
	return true;
    }

    @Override
    public boolean allowHead() {
	return true;
    }

    @Override
    public boolean allowPost() {
	return true;
    }

    /**
     * Principle constructor.
     */
    public SecurityTokenResource(final ISecurityTokenController controller, final IUserRoleDao userRoleDao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.controller = controller;
	this.userRoleDao = userRoleDao;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	userRoleDao.setUsername(username);

	final String tokenName = (String) request.getAttributes().get("token");
	try {
	    token = (Class<? extends ISecurityToken>) (StringUtils.isEmpty(tokenName) ? null : Class.forName(tokenName));
	} catch (final ClassNotFoundException e) {
	    throw new IllegalStateException("Token " + tokenName + " could not be found.");
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////
    /** Handles HEAD request, which corresponds to an authorisation request. */
    @Override
    public void handleHead() {
	super.handleHead();
	restUtil.setHeaderEntry(getResponse(), HttpHeaders.AUTHORIZED, controller.canAccess(username, token) ? "true" : "false");
    }

    /**
     * Handles GET requests, which should return roles associated with the specified security token.
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}
	// process GET request
	try {
	    return restUtil.listRepresentation(controller.findUserRolesFor(token));
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }

    /**
     * Handles POST request resulting making associations between security tokens and user roles.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final Map<String, List<Long>> map = restUtil.restoreMap(envelope);
	    final Map<Class<? extends ISecurityToken>, List<UserRole>> associations = convert(map);
	    controller.saveSecurityToken(associations);
	    // if there  was no exception then report a success back to client
	    getResponse().setEntity(restUtil.resultRepresentation(new Result("Security tokens updated successfully.")));
	} catch (final Exception ex) {
	    final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
	    getResponse().setEntity(restUtil.errorRepresentation(msg));
	}
    }

    /**
     * Converts a map of entity IDs into a map of entity instances.
     *
     * @throws Exception
     */
    private Map<Class<? extends ISecurityToken>, List<UserRole>> convert(final Map<String, List<Long>> tokenToRoleAssocations) throws Exception {
	final Map<Class<? extends ISecurityToken>, List<UserRole>> result = new HashMap<Class<? extends ISecurityToken>, List<UserRole>>();
	for (final String tokenName : tokenToRoleAssocations.keySet()) {
	    final Class<? extends ISecurityToken> token = (Class<? extends ISecurityToken>) Class.forName(tokenName);
	    final List<UserRole> roles = userRoleDao.findByIds(tokenToRoleAssocations.get(tokenName).toArray(new Long[] {}));
	    result.put(token, roles);
	}
	return result;
    }

}
