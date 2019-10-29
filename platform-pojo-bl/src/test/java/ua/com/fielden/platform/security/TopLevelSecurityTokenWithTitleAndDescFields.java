package ua.com.fielden.platform.security;

/**
 * Represents a top level security token used for testing, which has static fields {@code TITLE} and {@code DESC} instead of annotation {@code KeyTitle}.
 * 
 * @author TG Team
 * 
 */
public class TopLevelSecurityTokenWithTitleAndDescFields implements ISecurityToken {
    public static final String TITLE = "Title value";
    public static final String DESC = "Description value";

}
