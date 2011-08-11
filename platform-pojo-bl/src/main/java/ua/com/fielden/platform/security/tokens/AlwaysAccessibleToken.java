package ua.com.fielden.platform.security.tokens;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;

/**
 * A security token, which should be used for annotation on method that should always be accessible. This is relevant only in cases where some method invokes other methods that may
 * have security annotation, but this method should be executed regardless of the permission.
 * <p>
 * A concrete implementation of {@link ISecurityTokenController} should take this special token into account for it to make any effect in accordance with the logic handling nested
 * security tokens.
 */
public final class AlwaysAccessibleToken implements ISecurityToken {

}
