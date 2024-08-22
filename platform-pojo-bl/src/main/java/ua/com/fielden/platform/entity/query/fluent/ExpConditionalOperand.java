package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;

abstract class ExpConditionalOperand<T, ET extends AbstractEntity<?>> //
        extends MultipleOperand<T, ET> //
        implements IComparisonOperand<T, ET> {

    protected ExpConditionalOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public EntityQueryProgressiveInterfaces.IExprOperand0<T, ET> beginExpr() {
        return createExprOperand0(builder.beginExpression());
    }

    private ExprOperand0<T, ET> createExprOperand0(final EqlSentenceBuilder builder) {
        return new ExprOperand0<T, ET>(builder) {

            @Override
            protected T nextForExprOperand0(final EqlSentenceBuilder builder) {
                return ExpConditionalOperand.this.nextForSingleOperand(builder);
            }

        };
    }

}
