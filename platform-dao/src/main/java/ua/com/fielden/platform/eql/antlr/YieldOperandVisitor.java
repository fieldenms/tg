package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.functions.*;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;
import java.util.function.Function;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.antlr.SingleOperandVisitor.toArithmeticalOperator;

final class YieldOperandVisitor extends AbstractEqlVisitor<ISingleOperand1<? extends ISingleOperand2<?>>> {

    YieldOperandVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitYieldOperand_SingleOperand(final YieldOperand_SingleOperandContext ctx) {
        return ctx.singleOperand().accept(new SingleOperandVisitor(transformer));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitYieldOperand_CountAll(final YieldOperand_CountAllContext ctx) {
        return CountAll1.INSTANCE;
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitYieldOperandFunction(final YieldOperandFunctionContext ctx) {
        final var argument = ctx.singleOperand().accept(new SingleOperandVisitor(transformer));
        return chooseFunction(ctx.yieldOperandFunctionName()).apply(argument);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitYieldOperandExpr(final YieldOperandExprContext ctx) {
        final var first = ctx.first.accept(this);
        final List<CompoundSingleOperand1> rest = StreamUtils.zip(ctx.rest, ctx.operators, (rand, op) -> {
            return new CompoundSingleOperand1(rand.accept(this), toArithmeticalOperator(op));
        }).toList();
        return new Expression1(first, rest);
    }

    private static Function<ISingleOperand1<? extends ISingleOperand2<?>>, SingleOperandFunction1<?>>
    chooseFunction(final YieldOperandFunctionNameContext ctx) {
        return switch (ctx.token.getType()) {
            case EQLLexer.MAXOF -> MaxOf1::new;
            case EQLLexer.MINOF -> MinOf1::new;
            case EQLLexer.SUMOF -> op -> new SumOf1(op, false);
            case EQLLexer.SUMOFDISTINCT -> op -> new SumOf1(op, true);
            case EQLLexer.COUNTOF -> op -> new CountOf1(op, false);
            case EQLLexer.COUNTOFDISTINCT -> op -> new CountOf1(op, true);
            case EQLLexer.AVGOF -> op -> new AverageOf1(op, false);
            case EQLLexer.AVGOFDISTINCT -> op -> new AverageOf1(op, true);
            default -> unexpectedToken(ctx.token);
        };
    }

}
