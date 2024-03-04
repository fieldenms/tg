package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.fluent.*;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.operands.*;
import ua.com.fielden.platform.eql.stage1.operands.functions.CaseWhen1;
import ua.com.fielden.platform.eql.stage1.operands.functions.Concat1;
import ua.com.fielden.platform.eql.stage1.operands.functions.RoundTo1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.utils.StreamUtils.zipDo;

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
            case ValToken tok -> new Value1(preprocessValue(tok.value));
            case IValToken tok -> new Value1(preprocessValue(tok.value), true);
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

    @Override
    public CaseWhen1 visitCaseWhen(final CaseWhenContext ctx) {
        final List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThens = new ArrayList<>();

        final var conditionVisitor = new ConditionVisitor(transformer);
        zipDo(ctx.whens, ctx.thens,
                (when, then) -> whenThens.add(T2.t2(when.accept(conditionVisitor), then.accept(this))));

        final var otherwise = ofNullable(ctx.otherwiseOperand).map(x -> x.accept(this)).orElse(null);
        return new CaseWhen1(whenThens, otherwise, makeTypeCast(ctx.caseWhenEnd()));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitSingleOperand_Model(final SingleOperand_ModelContext ctx) {
        final QueryModelToken<?> token = (QueryModelToken<?>) ctx.token;
        return transformer.generateAsSubQuery(token.model);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitSingleOperand_Expr(final SingleOperand_ExprContext ctx) {
        final ExprToken token = (ExprToken) ctx.token;
        return new EqlCompiler(transformer).compile(token.model.getTokenSource(), EqlCompilationResult.StandaloneExpression.class).model();
    }

    @Override
    public Concat1 visitConcat(final ConcatContext ctx) {
        return new Concat1(ctx.operands.stream().map(c -> c.accept(this)).toList());
    }

    static ITypeCast makeTypeCast(final EQLParser.CaseWhenEndContext ctx) {
        final Token token = ctx.token;
        return switch (token.getType()) {
            case EQLLexer.END -> null;
            case EQLLexer.ENDASBOOL -> TypeCastAsBoolean.INSTANCE;
            case EQLLexer.ENDASINT -> TypeCastAsInteger.INSTANCE;
            case EQLLexer.ENDASSTR -> {
                final EndAsStrToken tok = (EndAsStrToken) token;
                yield TypeCastAsString.getInstance(tok.length);
            }
            case EQLLexer.ENDASDECIMAL -> {
                final EndAsDecimalToken tok = (EndAsDecimalToken) token;
                yield TypeCastAsDecimal.getInstance(tok.precision, tok.scale);
            }
            default -> unexpectedToken(token);
        };
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
