package ua.com.fielden.platform.security.provider;

import java.util.SortedSet;

import com.google.inject.ImplementedBy;

/// An abstraction that captures the notion of security token tree transformation.
/// It is intended to be used in applications which require a custom structure of a token tree.
///
/// @author TG Team
@FunctionalInterface
@ImplementedBy(SecurityTokenNodeIdentityTransformation.class)
public interface ISecurityTokenNodeTransformation {

    /// Accepts a tree of nodes that are represented by a sorted set of top-level token nodes and returns a transformed tree, also represented by a sorted set of top-level token nodes.
    /// The passed in structure might be immutable and thus no attempt shall be made modify it.
    /// Instead, a new structure should be constructed as the result.
    ///
    /// The result should not contain tokens that were not present in the original structure.
    /// If application-specific tokens need to be provided, a custom {@link ISecurityTokenProvider} should be installed by the application.
    SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> topLevelSecurityTokenNodes);
}
