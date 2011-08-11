package ua.com.fielden.platform.security;

import ua.com.fielden.platform.error.Result;

/**
 * A contract defining method call authorisation model. There can be a number of possible implementations -- LDAP based, database based or event a simple class not requiring any
 * external information, which is very convenient for testing or prototyping.
 * <p>
 * All information required for authenticating a specific security token must be known by the model. For example, an actual authorisation model implementation may carry information
 * about currently logged in user and check the provided into method <code>authorise</code> token against that user.
 * <p>
 * It should also be possibly, but not required, for an actual implementation to cache authorisation results against security tokens, This should boost performance in case of a
 * complex or slow (think db and network operations) authorisation process.
 * 
 * @author TG Team
 * 
 */
public interface IAuthorisationModel {
    /**
     * Should provide an implementation for security token authorisation.
     * 
     * @param token
     *            -- security token to be authorised
     * @return
     */
    Result authorise(Class<? extends ISecurityToken> token);

    /** Should perform some action that would result in method isStarted() returning true. */
    void start();

    /** Should perform some action that would result in method isStarted() returning false. */
    void stop();

    /** Should return a current state of the model indicating the fact that authentication is in progress. */
    boolean isStarted();
}
