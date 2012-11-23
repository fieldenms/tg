package ua.com.fielden.platform.web.resources;

import java.util.ArrayList;
import java.util.List;

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
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A resource responsible for managing associations between users and roles.
 *
 * @author TG Team
 */
public class UserRoleAssociationResource extends Resource {

    private final IUserController controller;
    private final IUserRoleDao userRoleDao;
    private final RestServerUtil restUtil;

    private final Long userIdToHaveRolesUpdated;
    private final Long[] roleIds;

    ////////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    ////////////////////////////////////////////////////////////////////
    @Override
    public boolean allowGet() {
	return false;
    }

    @Override
    public boolean allowHead() {
	return false;
    }

    @Override
    public boolean allowPost() {
	return true;
    }

    /**
     * Principle constructor.
     */
    public UserRoleAssociationResource(final IUserController controller, final IUserRoleDao userRoleDao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.controller = controller;
	this.userRoleDao = userRoleDao;
	this.restUtil = restUtil;

	userIdToHaveRolesUpdated = Long.parseLong(request.getResourceRef().getQueryAsForm().getFirstValue("userId"));
	roleIds = parseRoles(request.getResourceRef().getQueryAsForm().getFirstValue("roles"));
    }

    /** Converts string representation of roles' ids to integer. */
    private Long[] parseRoles(final String firstValue) {
	if (StringUtils.isEmpty(firstValue)) {
	    return new Long[0];
	}

	final String[] roles = firstValue.split(",");
	final List<Long> ids = new ArrayList<Long>(roles.length);
	for (final String strId : roles) {
	    ids.add(Long.parseLong(strId));
	}
	return ids.toArray(new Long[]{});
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////
    /**
     * Handles POST request resulting from RAO call to method updateUser.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    // retrieve roles by ids and user with roles by key
	    final List<UserRole> roles = userRoleDao.findByIds(roleIds);
	    final User user = controller.findUserByIdWithRoles(userIdToHaveRolesUpdated);
	    // update user with new roles
	    controller.updateUser(user, roles);
	    // if there  was no exception then report a success back to client
	    getResponse().setEntity(restUtil.resultRepresentation(new Result("Roles updated successfully.")));
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
	    getResponse().setEntity(restUtil.errorRepresentation(msg));
	}
    }

}
