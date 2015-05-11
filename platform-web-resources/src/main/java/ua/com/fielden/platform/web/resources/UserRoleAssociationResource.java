package ua.com.fielden.platform.web.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A resource responsible for managing associations between users and roles.
 * 
 * @author TG Team
 */
public class UserRoleAssociationResource extends ServerResource {

    private final IUserEx controller;
    private final IUserRoleDao userRoleDao;
    private final RestServerUtil restUtil;

    /**
     * Principle constructor.
     */
    public UserRoleAssociationResource(final IUserEx controller, final IUserRoleDao userRoleDao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        setNegotiated(false);
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        this.controller = controller;
        this.userRoleDao = userRoleDao;
        this.restUtil = restUtil;
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////
    /**
     * Handles POST request resulting from RAO call to method updateUser.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        try {
            final Map<Long, List<Long>> map = (Map<Long, List<Long>>) restUtil.restoreMap(envelope);
            final Map<User, Set<UserRole>> userRoleMap = convert(map);
            // update user with new roles
            controller.updateUsers(userRoleMap);
            // if there  was no exception then report a success back to client
            //getResponse().setEntity(restUtil.resultRepresentation(new Result("Roles updated successfully.")));
            return restUtil.resultRepresentation(new Result("Roles updated successfully."));
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
            //getResponse().setEntity(restUtil.errorRepresentation(msg));
            return restUtil.errorRepresentation(msg);
        }
    }

    private Map<User, Set<UserRole>> convert(final Map<Long, List<Long>> map) {
        final Map<Long, User> users = getUsersFor(map.keySet());
        final Map<Long, UserRole> userRoles = getUserRolesFor(map.values());
        final Map<User, Set<UserRole>> userRoleMap = new HashMap<>();
        for (final Map.Entry<Long, List<Long>> mapEntry : map.entrySet()) {
            final Set<UserRole> newRoles = getRolesFor(mapEntry.getValue(), userRoles);
            final User user = getUserFor(mapEntry.getKey(), users);
            userRoleMap.put(user, newRoles);
        }
        return userRoleMap;
    }

    private User getUserFor(final Long key, final Map<Long, User> users) {
        final User user = users.get(key);
        if (user == null) {
            throw new IllegalStateException("The user with id: " + key + " doesn't exists!");
        }
        return user;
    }

    private Set<UserRole> getRolesFor(final List<Long> value, final Map<Long, UserRole> userRoles) {
        final Set<UserRole> roles = new HashSet<>();
        for (final Long key : value) {
            final UserRole role = userRoles.get(key);
            if (role == null) {
                throw new IllegalStateException("The user role with id: " + key + " doesn't exists!");
            }
            roles.add(role);
        }
        return roles;
    }

    private Map<Long, UserRole> getUserRolesFor(final Collection<List<Long>> values) {
        final Set<Long> ids = new HashSet<>();
        for (final List<Long> restoredIds : values) {
            ids.addAll(restoredIds);
        }
        final List<UserRole> roles = userRoleDao.findByIds(ids.toArray(new Long[] {}));
        final Map<Long, UserRole> roleMap = new HashMap<>();
        for (final UserRole role : roles) {
            roleMap.put(role.getId(), role);
        }
        return roleMap;
    }

    private Map<Long, User> getUsersFor(final Set<Long> keySet) {
        final Map<Long, User> userMap = new HashMap<>();
        for (final Long id : keySet) {
            userMap.put(id, controller.findUserByIdWithRoles(id));
        }
        return userMap;
    }

}
