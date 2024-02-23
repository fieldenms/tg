package fielden.platform.eql;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Terminal;
import fielden.platform.bnf.Variable;
import fielden.platform.bnf.util.BnfToG4;
import fielden.platform.bnf.util.BnfToHtml;
import fielden.platform.bnf.util.BnfToText;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static fielden.platform.bnf.FluentBNF.start;
import static fielden.platform.bnf.Notation.*;
import static fielden.platform.bnf.Terms.label;
import static fielden.platform.bnf.util.BnfVerifier.verifyBnf;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.values;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.*;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlVariable.*;

/**
 * Canonical representation of EQL's grammar.
 */
public final class CanonicalEqlGrammar {

    // Short names
    private static final Class<String> STR = String.class;
    private static final Class<Object> OBJ = Object.class;
    private static final Class<Enum> ENUM = Enum.class;
    private static final Class<IConvertableToPath> PROP_PATH = IConvertableToPath.class;

    /**
     * Canonical EQL grammar in Extended Backus-Naur form.
     * <p>
     * <b>NOTE</b>: Should <b>not</b> be used for fluent API generation but for <b>reference</b> only.
     */
    // @formatter:off
    public static final BNF canonical_bnf =
        start(Query).

        specialize(Query).
            into(Select, Expression).

        derive(Select).
            to(select.with(Class.class),
                    opt(as.with(STR)),
                    opt(Join),
                    opt(Where),
                    opt(GroupBy),
                    AnyYield).
            or(select.with(Class.class),
                    opt(as.with(STR)),
                    opt(Join),
                    opt(Where),
                    opt(GroupBy),
                    Model).

        derive(Where).
            to(where, Condition).

        // AND takes precedence over OR
        derive(Condition).
            to(Predicate).
            or(label("left", Condition), and, label("right", Condition)).
            or(label("left", Condition), or, label("right", Condition)).
            or(begin, Condition, end).

        specialize(Predicate).
            into(UnaryPredicate, ComparisonPredicate, QuantifiedComparisonPredicate, LikePredicate, MembershipPredicate, SingleConditionPredicate).

        derive(UnaryPredicate).
            to(label("left", ComparisonOperand), UnaryComparisonOperator).

        derive(ComparisonPredicate).
            to(label("left", ComparisonOperand), label("op", ComparisonOperator), label("right", ComparisonOperand)).

        derive(QuantifiedComparisonPredicate).
            to(label("left", ComparisonOperand), label("op", ComparisonOperator), QuantifiedOperand).

        derive(LikePredicate).
            to(label("left", ComparisonOperand), label("op", LikeOperator), label("right", ComparisonOperand)).

        derive(MembershipPredicate).
            to(label("left", ComparisonOperand), label("op", MembershipOperator), MembershipOperand).

        derive(UnaryComparisonOperator).
            to(isNull).or(isNotNull).

        derive(LikeOperator).
            to(like).or(iLike).or(likeWithCast).or(iLikeWithCast).
            or(notLike).or(notLikeWithCast).or(notILikeWithCast).or(notILike).

        specialize(ComparisonOperand).
            into(SingleOperand, Expr, MultiOperand).

        derive(ComparisonOperator).
            to(eq).or(gt).or(lt).or(ge).or(le).or(ne).

        derive(QuantifiedOperand).
            to(all.with(SingleResultQueryModel.class)).
            or(any.with(SingleResultQueryModel.class)).

        derive(Expr).
            to(beginExpr, ExprBody, endExpr).
        derive(ExprBody).
            to(SingleOperandOrExpr, repeat(ArithmeticalOperator, SingleOperandOrExpr)).
        derive(SingleOperandOrExpr).
            to(SingleOperand).or(Expr).
        derive(ArithmeticalOperator).
            to(add).or(sub).or(div).or(mult).or(mod).
        derive(SingleOperand).
            to(Prop).or(ExtProp).
            or(Val).or(Param).
            or(expr.with(ExpressionModel.class)).
            or(model.with(SingleResultQueryModel.class)).
            or(UnaryFunction).
            or(IfNull).
            or(now).
            or(DateDiffInterval).
            or(DateAddInterval).
            or(Round).
            or(Concat).
            or(CaseWhen).

        derive(UnaryFunction).
            to(label("funcName", UnaryFunctionName), label("argument", SingleOperandOrExpr)).

        derive(UnaryFunctionName).
            to(upperCase).or(lowerCase).
            or(secondOf).or(minuteOf).or(hourOf).or(dayOf).or(monthOf).or(yearOf).or(dayOfWeekOf).
            or(absOf).
            or(dateOf).

        derive(IfNull).
            to(ifNull, label("nullable", SingleOperandOrExpr), then, label("other", SingleOperandOrExpr)).

        derive(DateDiffInterval).
            to(count, label("unit", DateDiffIntervalUnit), between, label("startDate", SingleOperandOrExpr), and, label("endDate", SingleOperandOrExpr)).

        derive(DateDiffIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(DateAddInterval).
            to(addTimeIntervalOf, label("left", SingleOperandOrExpr), label("unit", DateAddIntervalUnit), to, label("right", SingleOperandOrExpr)).

        derive(DateAddIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(Round).
            to(round, SingleOperandOrExpr, to.with(Integer.class)).

        derive(Concat).
            to(concat, SingleOperandOrExpr, (repeat(with, SingleOperandOrExpr)), end).

        derive(CaseWhen).
            to(caseWhen, Condition, then, SingleOperandOrExpr,
                    repeat(when, Condition, then, SingleOperandOrExpr),
                    opt(otherwise, label("otherwiseOperand", SingleOperandOrExpr)),
                    CaseWhenEnd).

        derive(CaseWhenEnd).
            to(end).or(endAsInt).or(endAsBool).
            or(endAsStr.with(Integer.class)).
            or(endAsDecimal.with(Integer.class, Integer.class)).

        derive(Prop).
            to(prop.with(STR)).or(prop.with(PROP_PATH)).or(prop.with(ENUM)).

        derive(ExtProp).
            to(extProp.with(STR)).or(extProp.with(PROP_PATH)).or(extProp.with(ENUM)).

        derive(Val).
            to(val.with(OBJ)).or(iVal.with(OBJ)).

        derive(Param).
            to(param.with(STR)).or(param.with(ENUM)).
            or(iParam.with(STR)).or(iParam.with(ENUM)).

        derive(MultiOperand).
            to(anyOfProps.rest(STR)).or(anyOfProps.rest(PROP_PATH)).
            or(allOfProps.rest(STR)).or(allOfProps.rest(PROP_PATH)).
            or(anyOfValues.rest(OBJ)).or(allOfValues.rest(OBJ)).
            or(anyOfParams.rest(STR)).or(anyOfIParams.rest(STR)).
            or(allOfParams.rest(STR)).or(allOfIParams.rest(STR)).
            or(anyOfModels.rest(PrimitiveResultQueryModel.class)).
            or(allOfModels.rest(PrimitiveResultQueryModel.class)).
            or(anyOfExpressions.rest(ExpressionModel.class)).
            or(allOfExpressions.rest(ExpressionModel.class)).

        derive(MembershipOperator).
            to(in).or(notIn).

        derive(MembershipOperand).
            to(values.rest(OBJ)).
            or(props.rest(STR)).or(props.rest(PROP_PATH)).
            or(params.rest(STR)).or(iParams.rest(STR)).
            or(model.with(SingleResultQueryModel.class)).

        derive(SingleConditionPredicate).
            to(exists.with(QueryModel.class)).
            or(notExists.with(QueryModel.class)).
            or(existsAnyOf.rest(QueryModel.class)).
            or(notExistsAnyOf.rest(QueryModel.class)).
            or(existsAllOf.rest(QueryModel.class)).
            or(notExistsAllOf.rest(QueryModel.class)).
            or(critCondition.with(STR, STR)).
            or(critCondition.with(PROP_PATH, PROP_PATH)).
            or(critCondition.with(ICompoundCondition0.class, STR, STR)).
            or(critCondition.with(ICompoundCondition0.class, STR, STR, OBJ)).
            or(condition.with(ConditionModel.class)).
            or(negatedCondition.with(ConditionModel.class)).

        derive(Join).
            to(JoinOperator, opt(label("alias", as).with(STR)), JoinCondition, opt(Join)).

        derive(JoinOperator).
            to(join.with(Class.class)).
            or(join.with(EntityResultQueryModel.class)).
            or(join.with(AggregatedResultQueryModel.class)).
            or(leftJoin.with(Class.class)).
            or(leftJoin.with(EntityResultQueryModel.class)).
            or(leftJoin.with(AggregatedResultQueryModel.class)).

        derive(JoinCondition).
            to(on, Condition).

        derive(GroupBy).
            to(groupBy, label("operand", SingleOperandOrExpr), opt(GroupBy)).

        specialize(AnyYield).
            into(Yield1, YieldMany).

        derive(Yield1).
            to(yield, label("operand", YieldOperand), label("model_", Yield1Model)).

        derive(YieldMany).
            to(opt(yieldAll), repeat1(AliasedYield), label("model_", YieldManyModel)).

        derive(AliasedYield).
            to(yield, label("operand", YieldOperand), label("alias", YieldAlias)).

        derive(YieldOperand).
            to(SingleOperandOrExpr).
            or(countAll).
            or(YieldOperandFunction).

        derive(YieldOperandFunction).
            to(label("funcName", YieldOperandFunctionName), label("argument", SingleOperandOrExpr)).

        derive(YieldOperandFunctionName).
            to(maxOf).or(minOf).or(sumOf).or(countOf).or(avgOf).
            or(sumOfDistinct).or(countOfDistinct).or(avgOfDistinct).

        derive(YieldAlias).
            to(as.with(STR)).or(as.with(ENUM)).or(as.with(PROP_PATH)).
            or(asRequired.with(STR)).or(asRequired.with(ENUM)).or(asRequired.with(PROP_PATH)).

        derive(Yield1Model).
            to(modelAsEntity.with(Class.class)).
            or(modelAsPrimitive).

        derive(YieldManyModel).
            to(modelAsEntity.with(Class.class)).
            or(modelAsAggregate).

        derive(Model).
            to(model).
            or(modelAsEntity.with(Class.class)).
            or(modelAsAggregate).

        derive(Expression).
            to(expr, model).

        build();
    // @formatter:on

    public enum EqlVariable implements Variable {
        Query,
        Select,
        Expression,
        Where,
        Condition, Predicate,
        SingleOperand, MultiOperand,
        ExtProp, Prop,
        UnaryComparisonOperator, Val, Param,
        ArithmeticalOperator, SingleOperandOrExpr, ExprBody, Expr,
        UnaryFunction, UnaryFunctionName, IfNull, DateDiffInterval, DateDiffIntervalUnit, DateAddInterval, DateAddIntervalUnit, Round, Concat, CaseWhen, CaseWhenEnd,
        MembershipOperator,
        MembershipOperand, ComparisonOperator, ComparisonOperand, QuantifiedOperand, SingleConditionPredicate, Join, JoinOperator,
        JoinCondition,
        Model, GroupBy,
        AnyYield, YieldOperand, YieldOperandFunction, YieldOperandFunctionName, YieldAlias, LikeOperator, SubsequentYield,
        UnaryPredicate,
        ComparisonPredicate, QuantifiedComparisonPredicate, LikePredicate, AliasedYield, YieldManyModel, Yield1Model, Yield1, YieldMany, MembershipPredicate
    }

    public enum EqlTerminal implements Terminal {
        select, where,
        eq, gt, lt, ge, le, ne,
        like, iLike, notLike, likeWithCast, iLikeWithCast, notLikeWithCast, notILikeWithCast, notILike,
        in, notIn,
        isNull, isNotNull,
        and, or,
        expr,
        begin, notBegin, end,
        prop, extProp,
        val, iVal,
        param, iParam,
        now,
        count,
        upperCase, lowerCase,
        secondOf, minuteOf, hourOf, dayOf, monthOf, yearOf, dayOfWeekOf,
        ifNull,
        addTimeIntervalOf,
        caseWhen,
        round,
        concat,
        absOf,
        dateOf,
        anyOfProps, allOfProps,
        anyOfValues, allOfValues,
        anyOfParams, allOfParams, anyOfIParams, allOfIParams,
        anyOfModels, allOfModels,
        anyOfExpressions, allOfExpressions,
        exists, notExists, existsAnyOf, notExistsAnyOf, existsAllOf, notExistsAllOf,
        critCondition, condition, negatedCondition,
        all, any,
        values,
        props,
        params, iParams,
        maxOf, minOf, sumOf, countOf, avgOf, countAll, sumOfDistinct, countOfDistinct, avgOfDistinct,
        between,
        seconds, minutes, hours, days, months, years,
        to,
        when, then, otherwise,
        endAsInt, endAsBool, endAsStr, endAsDecimal,
        with,
        as, asRequired,
        model, modelAsEntity, modelAsPrimitive, modelAsAggregate,
        add, sub, mult, div, mod,
        beginExpr, endExpr,
        join, leftJoin, on,
        yield, yieldAll,
        groupBy, asc, desc, order,
    }

    private CanonicalEqlGrammar() {}

    // print-bnf html FILE -- creates an HTML document with the BNF
    // generate antlr4 DIR -- creates an ANTLR4 grammar for the BNF in the given directory
    // print-bnf text FILE -- prints the BNF to stdout in human-readable format
    // verify -- verifies the BNF for correctness
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("usage: %s command".formatted(CanonicalEqlGrammar.class.getCanonicalName()));
            System.exit(1);
        }

        final String command = args[0];
        if ("generate".equals(command)) {
            if (args.length > 1) {
                String format = args[1];

                if ("antlr4".equals(format)) {
                    var result = new BnfToG4(canonical_bnf, "EQL").bnfToG4();
                    final String dir = args[2];
                    BnfToG4.writeResult(result, Path.of(dir));
                    System.out.println("ANTLR4 files generated in %s".formatted(dir));
                } else {
                    final PrintStream out;
                    final String outName;
                    if (args.length > 2) {
                        out = new PrintStream(new FileOutputStream(args[2]));
                        outName = args[2];
                    } else {
                        out = System.out;
                        outName = "stdout";
                    }

                    if ("html".equals(format)) {
                        out.println(new BnfToHtml().bnfToHtml(canonical_bnf));
                    } else {
                        out.println(new BnfToText().bnfToText(canonical_bnf));
                    }

                    System.out.println("Output written to %s".formatted(outName));
                }
            }
        } else if ("verify".equals(command))  {
            verifyBnf(canonical_bnf);
        } else {
            System.err.println("Unrecognised command: %s".formatted(command));
            System.exit(1);
        }
    }

}
