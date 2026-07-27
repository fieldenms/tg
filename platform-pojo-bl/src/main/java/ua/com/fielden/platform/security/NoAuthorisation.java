package ua.com.fielden.platform.security;

import ua.com.fielden.platform.error.Result;

import static ua.com.fielden.platform.error.Result.successful;

/**
 * This is a convenient authorisation model implementation, which should be used when no authorisation is required.
 * 
 * @author TG Team
 * 
 */
public class NoAuthorisation extends AbstractAuthorisationModel {

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        return successful();
    }

}
