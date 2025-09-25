package ua.com.fielden.platform.security.provider;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedSet;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.security.ISecurityToken;

/// A contract for providing convenient access to security tokens and their representation as [SecurityTokenNode]s.
///
/// One of the objectives for this contract is to provide a singleton implementation that could cache computationally intensive results for reading and transformation of tokens.
///
@ImplementedBy(SecurityTokenProvider.class)
public interface ISecurityTokenProvider {

    String ERR_DUPLICATE_SECURITY_TOKENS = "Not all security tokens are unique in their simple class name. This is required.";

    /// Returns an optional token class by its name.
    /// The result is empty if no token was found.
    ///
    <T extends ISecurityToken> Optional<Class<T>> getTokenByName(final String tokenClassSimpleName);
    
    /// Returns top level of security token nodes, suitable for building of Security Matrix.
    ///
    SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes();

    /// Returns all security token nodes.
    /// The result is equal to flattening the result of [#getTopLevelSecurityTokenNodes()].
    ///
    Collection<Class<? extends ISecurityToken>> allSecurityTokens();

}
