package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token used in testing of the authorisation mechanism.
 * <p>
 * As name implies it is used to restrict any calls to methods that require autherisation.
 * 
 * @author TG Team
 * 
 */
public class NoAccessToken implements ISecurityToken {
}
