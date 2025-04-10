package ua.com.fielden.platform.security.tokens;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;

import java.util.Optional;

/// Generates security token types at runtime.
///
/// This facility should be used only within the initialisation code of {@link ISecurityTokenProvider}.
/// Otherwise, the generated tokens will not be registered with {@link ISecurityTokenProvider}.
@ImplementedBy(SecurityTokenGenerator.class)
public interface ISecurityTokenGenerator {

    /**
     * Generates a security token type.
     * The generated token's package is equal to that of the specified entity type.
     * <p>
     * If the generated token type needs to be found by its name, use {@link ISecurityTokenProvider} or {@link ClassesRetriever}
     * if the former is not accessible.
     *
     * @param entityType  entity type for which the token will be generated
     * @param template  kind of the generated token
     * @param maybeParentType  security token type that will be extended by the generated token
     */
    Class<? extends ISecurityToken> generateToken(
            Class<? extends AbstractEntity<?>> entityType,
            Template template,
            Optional<Class<? extends ISecurityToken>> maybeParentType);

}
