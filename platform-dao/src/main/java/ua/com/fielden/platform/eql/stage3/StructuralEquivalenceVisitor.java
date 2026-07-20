package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;

import java.util.Objects;

/// Ordinary structural equivalence of two stage-3 AST trees: two nodes are equivalent iff they have the same type and
/// their corresponding fields are equivalent -- child nodes recursively, everything else by value.
/// Generated identifiers are compared as-is (this is *not* alpha-equivalence).
///
/// Each `visit` reproduces the `equals` of the corresponding node, but every reference to a child node is compared
/// through [AbstractSameShapeVisitor#visit(Object, Object, Object)] rather than through `Object.equals`; only
/// genuine leaves
/// (types, names, flags, parameters, enums, `Class`) are compared by value with [Objects#equals].
///
/// Two fields are deliberately excluded, matching the current `equals`: [Yield3]'s derived `column`, and a source's
/// `columns` (derived from its table or models).
///
public class StructuralEquivalenceVisitor extends AbstractStructuralEquivalenceVisitor<Void> {

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public StructuralEquivalenceVisitor() {}

    public boolean visit(final Object x, final Object y) {
        return visit(x, y, null);
    }

}
