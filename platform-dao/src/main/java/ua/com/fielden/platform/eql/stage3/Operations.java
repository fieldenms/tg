package ua.com.fielden.platform.eql.stage3;

import jakarta.inject.Inject;

public class Operations {

    private final StructuralEquivalenceVisitor structEq;

    @Inject
    protected Operations(final StructuralEquivalenceVisitor structEq) {
        this.structEq = structEq;
    }

    public boolean structEq(final Object x, final Object y) {
        return structEq.visit(x, y);
    }

}
