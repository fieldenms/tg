package ua.com.fielden.platform.security;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;

import com.google.inject.Inject;

/**
 * REST oriented authorisation model.
 * 
 * TODO If required from the performance perspective this implementation can be enhanced to cache authorisation results in a form of map between tokens and results.
 * 
 * @author TG Team
 * 
 */
public class RestAuthorisationModel extends AbstractAuthorisationModel {

    protected final ISecurityTokenController controller;
    protected final RestClientUtil restUtil;

    @Inject
    public RestAuthorisationModel(final ISecurityTokenController controller, final RestClientUtil restUtil) {
        this.controller = controller;
        this.restUtil = restUtil;
    }

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        return controller.canAccess(restUtil.getUsername(), token) ? //
        new Result("Authorised")
                : //
                new Result(new IllegalStateException("Permission denied: " + token.getAnnotation(KeyTitle.class).value()));
    }

}
