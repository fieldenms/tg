package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.*;
import ua.com.fielden.platform.eql.stage1.operands.functions.RoundTo1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

final class SingleOperandVisitor extends AbstractEqlVisitor<ISingleOperand1<? extends ISingleOperand2<?>>> {

    SingleOperandVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public Prop1 visitProp(final PropContext ctx) {
        final PropToken token = (PropToken) ctx.token;
        return new Prop1(token.propPath, false);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitExtProp(final ExtPropContext ctx) {
        final ExtPropToken token = (ExtPropToken) ctx.token;
        return new Prop1(token.propPath, true);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitVal(final ValContext ctx) {
        return switch (ctx.token) {
            case ValToken tok -> new Value1(tok.value);
            case IValToken tok -> new Value1(tok.value, true);
            default -> unexpectedToken(ctx.token);
        };
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitParam(final ParamContext ctx) {
        return switch (ctx.token) {
            case ParamToken tok -> new Value1(getParamValue(tok.paramName));
            case IParamToken tok -> new Value1(getParamValue(tok.paramName), true);
            default -> unexpectedToken(ctx.token);
        };
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitSingleOperand_Now(final SingleOperand_NowContext ctx) {
        final QueryNowValue qnv = transformer.nowValue;
        return new Value1(qnv != null ? qnv.get() : null);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitRound(final RoundContext ctx) {
        final int precision = ((ToToken) ctx.to).value;
        return new RoundTo1(ctx.singleOperand().accept(this), new Value1(precision));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitExpr(final ExprContext ctx) {
        return visitExprBody(ctx.exprBody());
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitExprBody(final ExprBodyContext ctx) {
        final var first = ctx.first.accept(this);
        final List<CompoundSingleOperand1> rest = StreamUtils.zip(ctx.rest.stream(), ctx.operators.stream(), (rand, op) -> {
            return new CompoundSingleOperand1(rand.accept(this), toArithmeticalOperator(op));
        }).toList();
        return new Expression1(first, rest);
    }

    static ArithmeticalOperator toArithmeticalOperator(final ArithmeticalOperatorContext ctx) {
        return switch (ctx.token.getType()) {
            case EQLParser.ADD -> ArithmeticalOperator.ADD;
            case EQLParser.SUB -> ArithmeticalOperator.SUB;
            case EQLParser.DIV -> ArithmeticalOperator.DIV;
            case EQLParser.MULT -> ArithmeticalOperator.MULT;
            case EQLParser.MOD -> ArithmeticalOperator.MOD;
            default -> unexpectedToken(ctx.token);
        };
    }

}
