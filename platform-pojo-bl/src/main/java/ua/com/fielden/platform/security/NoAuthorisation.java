package ua.com.fielden.platform.security;

import ua.com.fielden.platform.error.Result;

/**
 * This is a convenient authorisation model implementation, which should be used when no authorisation is required.
 * 
 * @author TG Team
 * 
 */
public class NoAuthorisation extends AbstractAuthorisationModel {

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
	return new Result(token, "no authorisation required");
    }

}
