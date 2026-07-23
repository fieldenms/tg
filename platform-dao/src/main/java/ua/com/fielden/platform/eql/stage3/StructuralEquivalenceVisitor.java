package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;

/// Ordinary structural equivalence of two stage-3 AST trees: two nodes are equivalent iff they have the same type and
/// their corresponding fields are equivalent -- child nodes recursively, everything else by value.
/// Generated identifiers are compared as-is (this is *not* alpha-equivalence).
///
public class StructuralEquivalenceVisitor extends AbstractStructuralEquivalenceVisitor<Void> {

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public StructuralEquivalenceVisitor() {}

    public boolean visit(final INode3 x, final INode3 y) {
        return visit(x, y, null);
    }

}
