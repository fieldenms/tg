package ua.com.fielden.platform.security.provider;

import java.util.Optional;
import java.util.SortedSet;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A contract for providing convenient access to security tokens and their representation as {@link SecurityTokenNode}s.
 * <p>
 * One of the objectives for this contract is to provide a singleton implementation that could cache computationally intensive results for reading and transformation of tokens.
 *
 * @author TG Team
 *
 */
@ImplementedBy(SecurityTokenProvider.class)
public interface ISecurityTokenProvider {
    
    /**
     * Returns a optional token class by its name. The result is empty if no token was found.
     *
     * @return
     */
    <T extends ISecurityToken> Optional<Class<T>> getTokenByName(final String tokenClassName);
    
    /**
     * Returns top level of security token nodes, suitable for building of Security Matrix.
     *
     * @return
     */
    SortedSet<SecurityTokenNode> getTopLevelSecurityTokenNodes();
}
