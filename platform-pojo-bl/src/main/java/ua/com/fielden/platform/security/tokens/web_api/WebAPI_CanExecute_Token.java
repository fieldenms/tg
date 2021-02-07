package ua.com.fielden.platform.security.tokens.web_api;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.tokens.Template.EXECUTE;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * {@link IWebApi} execution token.
 * 
 * @author TG Team
 * 
 */
public class WebAPI_CanExecute_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = "Web API";
    public final static String TITLE = format(EXECUTE.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(EXECUTE.forDesc(), ENTITY_TITLE);
}