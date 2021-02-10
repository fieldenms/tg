package ua.com.fielden.platform.security.tokens.web_api;

import ua.com.fielden.platform.security.ISecurityToken;


/**
 * Top level security token for all security tokens that belong to Web API.
 */
public class WebApiToken
    implements ISecurityToken
{
    public static final String TITLE = "Web API";
    public static final String DESC = "Web API tokens.";
}
