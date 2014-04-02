package ua.com.fielden.platform.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * A REST implementation of {@link ISecurityTokenController} contract.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenControllerRao implements ISecurityTokenController {

    private final RestClientUtil restUtil;
    private final IUserRoleDao userRoleDao;

    @Inject
    public SecurityTokenControllerRao(final IUserRoleDao userRoleDao, final RestClientUtil restUtil) {
        this.restUtil = restUtil;
        this.userRoleDao = userRoleDao;
    }

    protected WebResourceType getDefaultWebResourceType() {
        return WebResourceType.VERSIONED;
    }

    @Override
    public List<UserRole> findUserRoles() {
        return userRoleDao.findAll();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {
        final Request request = restUtil.newRequest(Method.GET, restUtil.getBaseUri(getDefaultWebResourceType()) + "/tokenroleassociation");
        final Pair<Response, Result> result = restUtil.process(request);
        if (!result.getValue().isSuccessful()) {
            throw result.getValue();
        }
        return (Map<Class<? extends ISecurityToken>, Set<UserRole>>) result.getValue().getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserRole> findUserRolesFor(final Class<? extends ISecurityToken> securityTokenClass) {
        final Request request = restUtil.newRequest(Method.GET, restUtil.getBaseUri(getDefaultWebResourceType()) + "/securitytokens/" + securityTokenClass.getName() + "/useroles");
        final Pair<Response, Result> result = restUtil.process(request);
        if (!result.getValue().isSuccessful()) {
            throw result.getValue();
        }
        return (List<UserRole>) result.getValue().getInstance();
    }

    @Override
    public void saveSecurityToken(final Map<Class<? extends ISecurityToken>, Set<UserRole>> tokenToRoleAssocations) {
        // prepare an envelope
        final Map<String, Set<Long>> envelopeContent = convert(tokenToRoleAssocations);
        final Representation envelope = restUtil.represent(envelopeContent);
        // prepare a request
        final Request request = restUtil.newRequest(Method.POST, restUtil.getBaseUri(getDefaultWebResourceType()) + "/securitytokens");
        request.setEntity(envelope);
        // process request
        final Pair<Response, Result> result = restUtil.process(request);
        if (!result.getValue().isSuccessful()) {
            throw result.getValue();
        }
    }

    /** Converts map of entity instances into a map of IDs. */
    private Map<String, Set<Long>> convert(final Map<Class<? extends ISecurityToken>, Set<UserRole>> tokenToRoleAssocations) {
        final Map<String, Set<Long>> result = new HashMap<String, Set<Long>>();
        for (final Class<? extends ISecurityToken> token : tokenToRoleAssocations.keySet()) {
            final Set<Long> roleIds = new HashSet<Long>();
            for (final UserRole role : tokenToRoleAssocations.get(token)) {
                roleIds.add(role.getId());
            }
            result.put(token.getName(), roleIds);
        }
        return result;
    }

    @Override
    public boolean canAccess(final String username, final Class<? extends ISecurityToken> token) {
        if (StringUtils.isEmpty(restUtil.getUsername()) || !restUtil.getUsername().equals(username)) {
            throw new IllegalArgumentException("The passed in used " + username + " does not match application user " + restUtil.getUsername() + ".");
        }
        if (token == AlwaysAccessibleToken.class) {
            return true;
        }

        final Request request = restUtil.newRequest(Method.HEAD, restUtil.getBaseUri(getDefaultWebResourceType()) + "/securitytokens/" + token.getName());
        final Response response = restUtil.send(request);
        if (!Status.SUCCESS_OK.equals(response.getStatus())) {
            throw new IllegalStateException(response.getStatus().toString());
        } else if (!StringUtils.isEmpty(restUtil.getHeaderValue(response, HttpHeaders.ERROR))) {
            throw new IllegalStateException(restUtil.getHeaderValue(response, HttpHeaders.ERROR));
        }
        return "true".equalsIgnoreCase(restUtil.getHeaderValue(response, HttpHeaders.AUTHORIZED));
    }
}
