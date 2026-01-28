package ua.com.fielden.platform.security.provider;

import java.util.SortedSet;

import com.google.inject.ImplementedBy;

import java.util.SortedSet;

/// Represents a transformation of a security token node tree.
///
@FunctionalInterface
@ImplementedBy(SecurityTokenNodeIdentityTransformation.class)
public interface ISecurityTokenNodeTransformation {

    /// Accepts a security token node tree represented by a sorted set of top-level nodes, and returns a transformed tree.
    /// The provided tree should be assumed to be immutable, thus no attempt shall be made to modify it.
    ///
    SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> tree);

}
