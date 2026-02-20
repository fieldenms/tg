package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.AbstractAuthorisationModel;
import ua.com.fielden.platform.security.AuthorisationException;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens._CanRead_Token;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

/**
 * This is an authorisation model implementation, which is used for testing purposes. It restricts access to {@link NoAccessToken}.
 * <p>
 * The authorisation logic covers situations where top level authorisation (by declaration -- not inheritance) overrides any sub-calls also requiring authorisation, but otherwise
 * restricting the call.
 * 
 * @author TG Team
 * 
 */
public class AuthorisationModelForTests extends AbstractAuthorisationModel {

    @Override
    public Result authorise(final Class<? extends ISecurityToken> token) {
        if (NoAccessToken.class.equals(token)) {
            return failure(new AuthorisationException("Permission denied.", token));
        } else if (AccessToken.class.equals(token)) {
            return successful();
        } else if (_CanRead_Token.class.equals(token)) {
            return successful();
        }

        throw new AuthorisationException("This model cannot handle provided token.", token);
    }
}
