package ua.com.fielden.platform.security.provider;

import java.util.SortedSet;

/// The default implementation -- the identity function.
///
final class SecurityTokenNodeIdentityTransformation implements ISecurityTokenNodeTransformation {

    @Override
    public SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> tree) {
        return tree;
    }

}
