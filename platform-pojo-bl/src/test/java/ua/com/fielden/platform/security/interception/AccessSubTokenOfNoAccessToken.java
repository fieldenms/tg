package ua.com.fielden.platform.security.interception;

/**
 * A security token used in testing of the authorisation mechanism.
 * <p>
 * Used specifically to test situations where top level authorisation (by declaration -- not inheritance) overrides any sub-calls also requiring authorisation.
 * 
 * @author 01es
 * 
 */
public class AccessSubTokenOfNoAccessToken extends NoAccessToken {
}
