package ua.com.fielden.platform.security.tokens.web_api;

/**
 * Various standard templates for titles and descriptions of security tokens for Web API.
 *
 * @author TG Team
 *
 */
public enum WebApiTemplate {
    QUERY ("%s_WebApi_CanQuery_Token", "%s Can Query", "Authorises Query root field execution.");
    
    private final String forClassName;
    private final String forTitle;
    private final String forDesc;
    
    public final String forClassName() {
        return forClassName;
    }
    
    public final String forTitle() {
        return forTitle;
    }
    
    public final String forDesc() {
        return forDesc;
    }
    
    private WebApiTemplate(final String forClassName, final String forTitle, final String forDesc) {
        this.forClassName = forClassName;
        this.forTitle = forTitle;
        this.forDesc = forDesc;
    }
    
}