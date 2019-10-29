package ua.com.fielden.platform.security.provider;

import java.util.SortedSet;

import com.google.inject.ImplementedBy;

/**
 * An abstraction that capture the notion of security tree transformation.
 * It may reshuffle security nodes around as by a concrete application, which requires a different structure to that provided by default.    
 *
 * @author TG Air
 */
@FunctionalInterface
@ImplementedBy(SecurityTokenNodeIdentityTransformation.class)
public interface ISecurityTokenNodeTransformation {

    /**
     * Accepts a tree of nodes that are represented by a sorted set of top-level token nodes and returns a transformed tree, also represented by a sorted set of top-level token nodes.
     * The passed in structure might be immutable and thus no attempt shall be made to changes it.
     * Instead a new structure should be constructed as the result.
     * 
     * @param topLevelSecurityTokenNodes
     * @return
     */
    SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> topLevelSecurityTokenNodes);
}
