package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.fluent.*;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.operands.*;
import ua.com.fielden.platform.eql.stage1.operands.functions.*;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsBoolean.AS_BOOLEAN;
import static ua.com.fielden.platform.entity.query.fluent.ITypeCast.AsInteger.AS_INTEGER;
import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.stage1.operands.Value1.value;
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
            case ValToken tok -> value(preprocessValue(tok.value));
            case IValToken tok -> value(preprocessValue(tok.value), true);
            default -> unexpectedToken(ctx.token);
        };
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitParam(final ParamContext ctx) {
        return switch (ctx.token) {
            case ParamToken tok -> value(getParamValue(tok.paramName));
            case IParamToken tok -> value(getParamValue(tok.paramName), true);
            default -> unexpectedToken(ctx.token);
        };
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitSingleOperand_Now(final SingleOperand_NowContext ctx) {
        final QueryNowValue qnv = transformer.nowValue;
        return Value1.value(qnv != null ? qnv.get() : null);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitRound(final RoundContext ctx) {
        final int precision = ((ToToken) ctx.to).value;
        return new RoundTo1(ctx.singleOperand().accept(this), Value1.value(precision));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitExpr(final ExprContext ctx) {
        return visitExprBody(ctx.exprBody());
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitExprBody(final ExprBodyContext ctx) {
        final var first = ctx.first.accept(this);
        final List<CompoundSingleOperand1> rest = StreamUtils.zip(ctx.rest, ctx.operators, (rand, op) -> {
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

    @Override
    public SingleOperandFunction1<? extends ISingleOperand2<?>> visitUnaryFunction(final UnaryFunctionContext ctx) {
        return chooseSingleOperandFunction(ctx.funcName).apply(ctx.argument.accept(this));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitIfNull(final IfNullContext ctx) {
        return new IfNull1(ctx.nullable.accept(this), ctx.other.accept(this));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitDateAddInterval(final DateAddIntervalContext ctx) {
        return new AddDateInterval1(ctx.left.accept(this), toIntervalUnit(ctx.unit), ctx.right.accept(this));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitDateDiffInterval(final DateDiffIntervalContext ctx) {
        return new CountDateInterval1(toIntervalUnit(ctx.unit), ctx.endDate.accept(this), ctx.startDate.accept(this));
    }

    private static DateIntervalUnit toIntervalUnit(final DateIntervalUnitContext ctx) {
        return switch (ctx.token.getType()) {
            case SECONDS -> DateIntervalUnit.SECOND;
            case MINUTES -> DateIntervalUnit.MINUTE;
            case HOURS -> DateIntervalUnit.HOUR;
            case DAYS -> DateIntervalUnit.DAY;
            case MONTHS -> DateIntervalUnit.MONTH;
            case YEARS -> DateIntervalUnit.YEAR;
            default -> unexpectedToken(ctx.token);
        };
    }

    static Function<ISingleOperand1<? extends ISingleOperand2<?>>, SingleOperandFunction1<? extends ISingleOperand2<?>>>
    chooseSingleOperandFunction(final UnaryFunctionNameContext ctx)
    {
        return switch (ctx.token.getType()) {
            case UPPERCASE   -> UpperCaseOf1::new;
            case LOWERCASE   -> LowerCaseOf1::new;
            case SECONDOF    -> SecondOf1::new;
            case MINUTEOF    -> MinuteOf1::new;
            case HOUROF      -> HourOf1::new;
            case DAYOF       -> DayOf1::new;
            case MONTHOF     -> MonthOf1::new;
            case YEAROF      -> YearOf1::new;
            case DAYOFWEEKOF -> DayOfWeekOf1::new;
            case ABSOF       -> AbsOf1::new;
            case DATEOF      -> DateOf1::new;
            default -> unexpectedToken(ctx.token);
        };
    }

    static ITypeCast makeTypeCast(final EQLParser.CaseWhenEndContext ctx) {
        final Token token = ctx.token;
        return switch (token.getType()) {
            case EQLLexer.END -> null;
            case EQLLexer.ENDASBOOL -> AS_BOOLEAN;
            case EQLLexer.ENDASINT -> AS_INTEGER;
            case EQLLexer.ENDASSTR -> {
                final EndAsStrToken tok = (EndAsStrToken) token;
                yield ITypeCast.AsString.getInstance(tok.length);
            }
            case EQLLexer.ENDASDECIMAL -> {
                final EndAsDecimalToken tok = (EndAsDecimalToken) token;
                yield ITypeCast.AsDecimal.getInstance(tok.precision, tok.scale);
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
