package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;

import java.util.List;
import java.util.function.Predicate;

public class Operations {

    private final StructuralEquivalenceVisitor structEq;
    private final AlphaEquivalenceVisitor alphaEq;
    private final NodeCollector nodeCollector;

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public Operations(
            final StructuralEquivalenceVisitor structEq,
            final AlphaEquivalenceVisitor alphaEq,
            final NodeCollector nodeCollector)
    {
        this.structEq = structEq;
        this.alphaEq = alphaEq;
        this.nodeCollector = nodeCollector;
    }

    public boolean structEq(final INode3 x, final INode3 y) {
        return structEq.visit(x, y);
    }

    public boolean alphaEq(final INode3 x, final INode3 y) {
        return alphaEq.visit(x, y);
    }

    public List<INode3> collectNodes(final INode3 root, final Predicate<? super INode3> pred) {
        return nodeCollector.collect(root, pred);
    }

    @SuppressWarnings("unchecked")
    public <T extends INode3> List<T> collectNodesOfType(final INode3 root, final Class<T> nodeType) {
        return (List<T>) collectNodes(root, nodeType::isInstance);
    }

}
