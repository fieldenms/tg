package fielden.platform.eql;

import fielden.platform.eql.fling.BnfToHtml;
import fielden.platform.eql.fling.BnfToText;
import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.Terminal;
import il.ac.technion.cs.fling.internal.grammar.rules.Variable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.values;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.*;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlVariable.*;
import static fielden.platform.eql.fling.BnfVerifier.verifyBnf;
import static il.ac.technion.cs.fling.grammars.api.BNFAPI.bnf;
import static il.ac.technion.cs.fling.internal.grammar.rules.Quantifiers.noneOrMore;
import static il.ac.technion.cs.fling.internal.grammar.rules.Quantifiers.optional;

// Use a forked version of Fling which has additional features:
// https://github.com/homedirectory/fling

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
    public static final EBNF canonical_bnf = bnf().
        start(Query).

        specialize(Query).
            into(Select, Expression).

        derive(Select).
            to(select.with(Class.class),
                    optional(as.with(STR)),
                    optional(Join),
                    optional(Where),
                    optional(GroupBy),
                    FirstYield).
            or(select.with(Class.class),
                    optional(as.with(STR)),
                    optional(Join),
                    optional(Where),
                    optional(GroupBy),
                    Model).

        derive(Where).
            to(where, Condition).

        // AND takes precedence over OR
        derive(Condition).
            to(Predicate).or(Condition, and, Condition).or(Condition, or, Condition).or(begin, Condition, end).

        derive(Predicate).
            to(ComparisonOperand, UnaryComparisonOperator).
            or(ComparisonOperand, ComparisonOperator, ComparisonOperand).
            or(ComparisonOperand, QuantifiedComparisonOperator, QuantifiedOperand).
            or(ComparisonOperand, MembershipOperator, MembershipOperand).
            or(SingleConditionPredicate).

        derive(UnaryComparisonOperator).
            to(isNull).or(isNotNull).

        derive(ComparisonOperator).
            to(like).or(iLike).or(likeWithCast).or(iLikeWithCast).
            or(notLike).or(notLikeWithCast).or(notILikeWithCast).or(notILike).

        specialize(ComparisonOperand).
            into(SingleOperand, Expr, MultiOperand).

        derive(QuantifiedComparisonOperator).
            to(eq).or(gt).or(lt).or(ge).or(le).or(ne).

        derive(QuantifiedOperand).
            to(all.with(SingleResultQueryModel.class)).
            or(any.with(SingleResultQueryModel.class)).
            or(ComparisonOperand).

        derive(Expr).
            to(beginExpr, ExprBody, endExpr).
        derive(ExprBody).
            to(SingleOperandOrExpr, noneOrMore(ArithmeticalOperator, SingleOperandOrExpr)).
        derive(SingleOperandOrExpr).
            to(SingleOperand).or(Expr).
        derive(ArithmeticalOperator).
            to(add).or(sub).or(div).or(mult).or(mod).
        derive(SingleOperand).
            to(AnyProp).or(Val).or(Param).
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
            to(UnaryFunctionName, SingleOperandOrExpr).

        derive(UnaryFunctionName).
            to(upperCase).or(lowerCase).
            or(secondOf).or(minuteOf).or(hourOf).or(dayOf).or(monthOf).or(yearOf).or(dayOfWeekOf).
            or(absOf).
            or(dateOf).

        derive(IfNull).
            to(ifNull, SingleOperandOrExpr, then, SingleOperandOrExpr).

        derive(DateDiffInterval).
            to(count, DateDiffIntervalUnit, between, SingleOperandOrExpr, and, SingleOperandOrExpr).

        derive(DateDiffIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(DateAddInterval).
            to(addTimeIntervalOf, SingleOperandOrExpr, DateAddIntervalUnit, to, SingleOperandOrExpr).

        derive(DateAddIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(Round).
            to(round, SingleOperandOrExpr, to.with(Integer.class)).

        derive(Concat).
            to(concat, SingleOperandOrExpr, (noneOrMore(with, SingleOperandOrExpr)), end).

        derive(CaseWhen).
            to(caseWhen, Condition, then, SingleOperandOrExpr,
                    noneOrMore(when, Condition, then, SingleOperandOrExpr),
                    optional(otherwise, SingleOperandOrExpr),
                    CaseWhenEnd).

        derive(CaseWhenEnd).
            to(end).or(endAsInt).or(endAsBool).
            or(endAsStr.with(Integer.class)).
            or(endAsDecimal.with(Integer.class, Integer.class)).

        specialize(AnyProp).
            into(Prop, ExtProp).

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
            to(anyOfProps.many(STR)).or(anyOfProps.many(PROP_PATH)).
            or(allOfProps.many(STR)).or(allOfProps.many(PROP_PATH)).
            or(anyOfValues.many(OBJ)).or(allOfValues.many(OBJ)).
            or(anyOfParams.many(STR)).or(anyOfIParams.many(STR)).
            or(allOfParams.many(STR)).or(allOfIParams.many(STR)).
            or(anyOfModels.many(PrimitiveResultQueryModel.class)).
            or(allOfModels.many(PrimitiveResultQueryModel.class)).
            or(anyOfExpressions.many(ExpressionModel.class)).
            or(allOfExpressions.many(ExpressionModel.class)).

        derive(MembershipOperator).
            to(in).or(notIn).

        derive(MembershipOperand).
            to(values.many(OBJ)).
            or(props.many(STR)).or(props.many(PROP_PATH)).
            or(params.many(STR)).or(iParams.many(STR)).
            or(model.with(SingleResultQueryModel.class)).

        derive(SingleConditionPredicate).
            to(exists.with(QueryModel.class)).
            or(notExists.with(QueryModel.class)).
            or(existsAnyOf.many(QueryModel.class)).
            or(notExistsAnyOf.many(QueryModel.class)).
            or(existsAllOf.many(QueryModel.class)).
            or(notExistsAllOf.many(QueryModel.class)).
            or(critCondition.with(STR, STR)).
            or(critCondition.with(PROP_PATH, PROP_PATH)).
            or(critCondition.with(ICompoundCondition0.class, STR, STR)).
            or(critCondition.with(ICompoundCondition0.class, STR, STR, OBJ)).
            or(condition.with(ConditionModel.class)).
            or(negatedCondition.with(ConditionModel.class)).

        derive(Join).
            to(JoinOperator, optional(as.with(STR)), JoinCondition, optional(Join)).

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
            to(groupBy, SingleOperandOrExpr, optional(GroupBy)).

        derive(FirstYield).
            to(yield, YieldOperand, modelAsEntity.with(Class.class)).
            or(yield, YieldOperand, modelAsPrimitive).
            or(yieldAll, SubsequentYield).
            or(yield, YieldOperand, YieldAlias, SubsequentYield).

        derive(YieldOperand).
            to(SingleOperandOrExpr).
            or(countAll).
            or(YieldOperandFunction).

        derive(YieldOperandFunction).
            to(YieldOperandFunctionName, SingleOperandOrExpr).

        derive(YieldOperandFunctionName).
            to(maxOf).or(minOf).or(sumOf).or(countOf).or(avgOf).
            or(sumOfDistinct).or(countOfDistinct).or(avgOfDistinct).

        derive(YieldAlias).
            to(as.with(STR)).or(as.with(ENUM)).or(as.with(PROP_PATH)).
            or(asRequired.with(STR)).or(asRequired.with(ENUM)).or(asRequired.with(PROP_PATH)).

        derive(SubsequentYield).
            to(yield, YieldOperand, YieldAlias, SubsequentYield).
            or(modelAsEntity.with(Class.class)).
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
        AnyProp, ExtProp, Prop,
        UnaryComparisonOperator, Val, Param,
        ArithmeticalOperator, SingleOperandOrExpr, ExprBody, Expr,
        UnaryFunction, UnaryFunctionName, IfNull, DateDiffInterval, DateDiffIntervalUnit, DateAddInterval, DateAddIntervalUnit, Round, Concat, CaseWhen, CaseWhenEnd,
        MembershipOperator,
        MembershipOperand, ComparisonOperator, ComparisonOperand, QuantifiedComparisonOperator, QuantifiedOperand, SingleConditionPredicate, Join, JoinOperator,
        JoinCondition,
        Model, GroupBy,
        FirstYield, YieldOperand, YieldOperandFunction, YieldOperandFunctionName, YieldAlias, SubsequentYield
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
    // print-bnf -- prints the BNF to stdout in human-readable format
    // verify -- verifies the BNF for correctness
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("usage: %s command".formatted(CanonicalEqlGrammar.class.getCanonicalName()));
            System.exit(1);
        }

        final String command = args[0];
        if ("print-bnf".equals(command)) {
            if (args.length > 1 && "html".equals(args[1])) {
                String html = new BnfToHtml().bnfToHtml(canonical_bnf);
                final PrintStream out;
                if (args.length > 2) {
                    out = new PrintStream(new FileOutputStream(args[2]));
                } else {
                    out = System.out;
                }
                out.println(html);
            } else {
                System.out.println(new BnfToText().bnfToText(canonical_bnf));
            }
        } else if ("verify".equals(command))  {
            verifyBnf(canonical_bnf);
        } else {
            System.err.println("Unrecognised command: %s".formatted(command));
            System.exit(1);
        }
    }

}
