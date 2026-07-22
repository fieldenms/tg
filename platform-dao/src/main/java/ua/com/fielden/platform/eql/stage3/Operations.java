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

    public boolean structEq(final Object x, final Object y) {
        return structEq.visit(x, y);
    }

    public boolean alphaEq(final Object x, final Object y) {
        return alphaEq.visit(x, y);
    }

    public List<Object> collectNodes(final Object root, final Predicate<?> pred) {
        return nodeCollector.collect(root, pred);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> collectNodesOfType(final Object root, final Class<T> nodeType) {
        return (List<T>) collectNodes(root, nodeType::isInstance);
    }

}
