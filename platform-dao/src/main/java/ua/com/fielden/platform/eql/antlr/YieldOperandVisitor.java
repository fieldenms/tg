package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.entity.query.exceptions.EqlValidationException;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlCompilationException;
import ua.com.fielden.platform.eql.antlr.tokens.ParamToken;
import ua.com.fielden.platform.eql.antlr.tokens.ValToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage1.operands.functions.*;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.antlr.SingleOperandVisitor.toArithmeticalOperator;
import static ua.com.fielden.platform.eql.stage1.operands.Value1.value;

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

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitYieldOperandConcatOf(final YieldOperandConcatOfContext ctx) {
        final var expr = ctx.expr.accept(new SingleOperandVisitor(transformer));
        final Value1 separator = switch (ctx.separator.token) {
            case ValToken tok -> {
                if (!(tok.value instanceof CharSequence cs)) {
                    throw new EqlValidationException(format(
                            "Invalid separator for `concatOf` using `val`. Must be a subtype of [%s], but was [%s].",
                            CharSequence.class.getSimpleName(),
                            tok.value == null ? "null" : tok.value.getClass().getTypeName()));
                }
                yield value(preprocessValue(cs));
            }
            case ParamToken tok -> {
                final var paramValue = requireParamValue(tok.paramName);
                if (!(paramValue instanceof CharSequence cs)) {
                    throw new EqlValidationException(format(
                            "Invalid separator for `concatOf` using `param(%s)`. Must be a subtype of [%s], but was [%s].",
                            tok.paramName,
                            CharSequence.class.getSimpleName(),
                            paramValue == null ? "null" : paramValue.getClass().getTypeName()));
                }
                yield value(cs);
            }
            default -> unexpectedToken(ctx.separator.token);
        };
        return new ConcatOf1(expr, separator);
    }

}
