package ua.com.fielden.platform.security.provider;

import jakarta.inject.Singleton;

import java.util.SortedSet;

/// The default implementation -- the identity function.
///
@Singleton
final class SecurityTokenNodeIdentityTransformation implements ISecurityTokenNodeTransformation {

    @Override
    public SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> tree) {
        return tree;
    }

}
