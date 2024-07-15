package ua.com.fielden.platform.security.tokens.web_api;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token to control access to the GraphiQL IDE.
 * 
 * @author TG Team
 */
public class GraphiQL_CanExecute_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.EXECUTE.forTitle(), "GraphiQL IDE");
    public final static String DESC = "Authorises execution of GraphiQL IDE, which provides an interractive environment for exploring and running a domain-driven Web API.";
}