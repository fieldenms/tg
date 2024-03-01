package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.antlr.EqlCompilationResult.StandaloneExpression;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLParser.StandaloneExpressionContext;
import static ua.com.fielden.platform.eql.antlr.SingleOperandVisitor.toArithmeticalOperator;

final class StandaloneExpressionVisitor extends AbstractEqlVisitor<StandaloneExpression> {

    StandaloneExpressionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public StandaloneExpression visitStandaloneExpression(final StandaloneExpressionContext ctx) {
        final var yieldOperandVisitor = new YieldOperandVisitor(transformer);

        final var first = ctx.first.accept(yieldOperandVisitor);
        final List<CompoundSingleOperand1> rest = StreamUtils.zip(ctx.rest, ctx.operators, (rand, op) -> {
            return new CompoundSingleOperand1(rand.accept(yieldOperandVisitor), toArithmeticalOperator(op));
        }).toList();

        return new StandaloneExpression(new Expression1(first, rest));
    }

}
