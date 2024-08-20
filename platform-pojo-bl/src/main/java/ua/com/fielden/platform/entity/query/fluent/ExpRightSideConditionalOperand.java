package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ExpRightSideConditionalOperand<T, ET extends AbstractEntity<?>> //
        extends RightSideOperand<T, ET> //
        implements IComparisonQuantifiedOperand<T, ET> {

    protected ExpRightSideConditionalOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IExprOperand0<T, ET> beginExpr() {
        return createExprOperand0(builder.beginExpression());
    }

    private ExprOperand0<T, ET> createExprOperand0(final EqlSentenceBuilder builder) {
        return new ExprOperand0<T, ET>(builder) {

            @Override
            protected T nextForExprOperand0(final EqlSentenceBuilder builder) {
                return ExpRightSideConditionalOperand.this.nextForSingleOperand(builder);
            }

        };
    }

}
