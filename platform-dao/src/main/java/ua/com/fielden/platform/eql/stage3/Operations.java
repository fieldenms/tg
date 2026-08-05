package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/// A facade over the stage-3 AST operations.
///
/// It exists so that callers can reach these operations through a single injectable entry point.
///
public class Operations {

    private final StructuralEquivalenceVisitor structEq;
    private final AlphaEquivalenceVisitor alphaEq;
    private final NodeCollector nodeCollector;
    private final UpdateVisitor updateVisitor;

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public Operations(
            final StructuralEquivalenceVisitor structEq,
            final AlphaEquivalenceVisitor alphaEq,
            final NodeCollector nodeCollector,
            final UpdateVisitor updateVisitor)
    {
        this.structEq = structEq;
        this.alphaEq = alphaEq;
        this.nodeCollector = nodeCollector;
        this.updateVisitor = updateVisitor;
    }

    /// True iff `x` and `y` are structurally equivalent.
    ///
    /// @see StructuralEquivalenceVisitor
    ///
    public boolean structEq(final INode3 x, final INode3 y) {
        return structEq.visit(x, y);
    }

    /// True iff `x` and `y` are alpha-equivalent, i.e., structurally equivalent modulo a consistent renaming of source
    /// identifiers.
    ///
    /// @see AlphaEquivalenceVisitor
    ///
    public boolean alphaEq(final INode3 x, final INode3 y) {
        return alphaEq.visit(x, y);
    }

    /// All nodes reachable from `root` (inclusive) that satisfy `pred`.
    ///
    public List<INode3> collectNodes(final INode3 root, final Predicate<? super INode3> pred) {
        return nodeCollector.collect(root, pred);
    }

    /// All nodes reachable from `root` (inclusive) that are instances of `nodeType`.
    ///
    @SuppressWarnings("unchecked")
    public <T extends INode3> List<T> collectNodesOfType(final INode3 root, final Class<T> nodeType) {
        return (List<T>) collectNodes(root, nodeType::isInstance);
    }

    /// Updates the AST rooted at `root` according to `fn`.
    ///
    /// @see UpdateVisitor
    ///
    public INode3 update(final INode3 root, final BiFunction<? super INode3, UpdateVisitor.Action, INode3> fn) {
        final UpdateVisitor.State state = fn::apply;
        return updateVisitor.visit(root, state);
    }

}
