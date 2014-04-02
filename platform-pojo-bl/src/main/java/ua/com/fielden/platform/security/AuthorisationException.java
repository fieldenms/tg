package ua.com.fielden.platform.security;

/**
 * An exception class, which should be used in situations where some security token was violated.
 * 
 * @author 01es
 * 
 */
public class AuthorisationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Class<? extends ISecurityToken> violatedToken;

    public AuthorisationException(final Class<? extends ISecurityToken> violatedToken) {
        this.violatedToken = violatedToken;
    }

    public AuthorisationException(String message, final Class<? extends ISecurityToken> violatedToken) {
        super(message);
        this.violatedToken = violatedToken;
    }

    public final Class<? extends ISecurityToken> getViolatedToken() {
        return violatedToken;
    }
}
