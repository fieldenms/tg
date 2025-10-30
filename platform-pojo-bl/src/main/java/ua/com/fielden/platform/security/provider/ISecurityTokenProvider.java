package ua.com.fielden.platform.security.provider;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedSet;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.security.ISecurityToken;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedSet;

/// A contract for providing convenient access to security tokens and their representation as [SecurityTokenNode]s.
///
/// One of the objectives for this contract is to provide a singleton implementation that could cache computationally intensive results for reading and transformation of tokens.
///
/// A contract for providing convenient access to security tokens and their representation as [SecurityTokenNode]s.
///
/// One of the objectives for this contract is to provide a singleton implementation that could cache computationally intensive results for reading and transformation of tokens.
///
/// The default implementation is {@link SecurityTokenProvider}, which should be subclassed if some custom logic is needed in an application.
///
/// @author TG Team
/// @see ISecurityTokenNodeTransformation
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

    /// A security token that is used in place of a non-existing token, when trying to reconstruct a security token class from a string representation,
    /// when reading data from a database.
    ///
    /// The placement of this class in [ISecurityTokenProvider] is deliberate to avoid its discovery during the process of automatic discovery of the application security tokens.
    ///
    class MissingSecurityTokenPlaceholder implements ISecurityToken {
        public final static String TITLE = "MISSING SECURITY TOKEN PLACEHOLDER";
        public final static String DESC = "Used to represent situations where no real security token class could be found.";
    }

}
