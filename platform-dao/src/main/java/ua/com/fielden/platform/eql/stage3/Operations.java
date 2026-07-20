package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;

public class Operations {

    private final StructuralEquivalenceVisitor structEq;
    private final AlphaEquivalenceVisitor alphaEq;

    // TODO Make protected once EQL tests are refactored using IoC.
    @Inject
    public Operations(final StructuralEquivalenceVisitor structEq, final AlphaEquivalenceVisitor alphaEq) {
        this.structEq = structEq;
        this.alphaEq = alphaEq;
    }

    public boolean structEq(final Object x, final Object y) {
        return structEq.visit(x, y);
    }

    public boolean alphaEq(final Object x, final Object y) {
        return alphaEq.visit(x, y);
    }

}
