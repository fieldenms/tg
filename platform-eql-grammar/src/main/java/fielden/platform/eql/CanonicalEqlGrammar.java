package fielden.platform.eql;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Terminal;
import fielden.platform.bnf.Variable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import static fielden.platform.bnf.FluentBNF.start;
import static fielden.platform.bnf.Notation.*;
import static fielden.platform.bnf.Terms.label;
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
            into(Select, StandaloneExpression, StandaloneCondExpr, OrderBy).

        specialize(Select).
            into(SelectFrom, SourcelessSelect).

        derive(SelectFrom).
            to(SelectSource,
               opt(label("alias", as).with(STR)),
               opt(Join),
               opt(Where),
               opt(GroupBy),
               SelectEnd).

        derive(SelectSource).
            to(select.with(Class.class)).
            or(select.rest(EntityResultQueryModel.class)).
            or(select.rest(AggregatedResultQueryModel.class)).

        derive(SourcelessSelect).
            to(select, opt(GroupBy), SelectEnd).

        specialize(SelectEnd).
            into(AnyYield, Model).

        derive(Where).
            to(where, Condition).

        // AND takes precedence over OR
        derive(Condition).
            to(Predicate).
            or(label("left", Condition), and, label("right", Condition)).
            or(label("left", Condition), or, label("right", Condition)).
            or(begin, Condition, end).
            or(notBegin, Condition, end).

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
            to(SingleOperand, repeat(ArithmeticalOperator, SingleOperand)).
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
            or(Expr).

        derive(UnaryFunction).
            to(label("funcName", UnaryFunctionName), label("argument", SingleOperand)).

        derive(UnaryFunctionName).
            to(upperCase).or(lowerCase).
            or(secondOf).or(minuteOf).or(hourOf).or(dayOf).or(monthOf).or(yearOf).or(dayOfWeekOf).
            or(absOf).
            or(dateOf).

        derive(IfNull).
            to(ifNull, label("nullable", SingleOperand), then, label("other", SingleOperand)).

        derive(DateDiffInterval).
            to(count, label("unit", DateIntervalUnit), between, label("startDate", SingleOperand), and, label("endDate", SingleOperand)).

        derive(DateIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(DateAddInterval).
            to(addTimeIntervalOf, label("left", SingleOperand), label("unit", DateIntervalUnit), to, label("right", SingleOperand)).

        derive(Round).
            to(round, SingleOperand, to.with(Integer.class)).

        derive(Concat).
            to(concat, SingleOperand, (repeat(with, SingleOperand)), end).

        derive(CaseWhen).
            to(caseWhen, Condition, then, SingleOperand,
                    repeat(when, Condition, then, SingleOperand),
                    opt(otherwise, label("otherwiseOperand", SingleOperand)),
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
            to(repeat1(groupBy, label("operand", SingleOperand))).

        specialize(AnyYield).
            into(Yield1, YieldMany).

        derive(Yield1).
            to(yield, label("operand", YieldOperand), label("model_", Yield1Model)).

        derive(YieldMany).
            to(opt(yieldAll), repeat(AliasedYield), label("model_", YieldManyModel)).

        derive(AliasedYield).
            to(yield, label("operand", YieldOperand), label("alias", YieldAlias)).

        derive(YieldOperand).
            to(SingleOperand).
            or(beginExpr, YieldOperand, repeat(ArithmeticalOperator, YieldOperand), endExpr).
            or(countAll).
            or(YieldOperandFunction).

        derive(YieldOperandFunction).
            to(label("funcName", YieldOperandFunctionName), label("argument", SingleOperand)).

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

        derive(StandaloneExpression).
            to(expr, label("operand", YieldOperand), repeat(ArithmeticalOperator, YieldOperand), model).

        derive(StandaloneCondExpr).
            to(cond, StandaloneCondition, model).

        derive(StandaloneCondition).
            to(Predicate).
            or(label("left", StandaloneCondition), and, label("right", StandaloneCondition)).
            or(label("left", StandaloneCondition), or, label("right", StandaloneCondition)).

        derive(OrderBy).
            to(orderBy, repeat1(OrderByOperand), model).

        derive(OrderByOperand).
            to(SingleOperand, Order).
            or(yield.with(STR), Order).
            or(order.with(OrderingModel.class)).

        derive(Order).
            to(asc).or(desc).

        build();
    // @formatter:on

    public enum EqlVariable implements Variable {
        Query,
        Select,
        StandaloneExpression,
        Where,
        Condition, Predicate,
        SingleOperand, MultiOperand,
        ExtProp, Prop,
        UnaryComparisonOperator, Val, Param,
        ArithmeticalOperator, ExprBody, Expr,
        UnaryFunction, UnaryFunctionName, IfNull, DateDiffInterval, DateAddInterval, Round, Concat, CaseWhen, CaseWhenEnd,
        MembershipOperator,
        MembershipOperand, ComparisonOperator, ComparisonOperand, QuantifiedOperand, SingleConditionPredicate, Join, JoinOperator,
        JoinCondition,
        Model, GroupBy,
        AnyYield, YieldOperand, YieldOperandFunction, YieldOperandFunctionName, YieldAlias, LikeOperator, SubsequentYield,
        UnaryPredicate,
        ComparisonPredicate, QuantifiedComparisonPredicate, LikePredicate, AliasedYield, YieldManyModel, Yield1Model, Yield1, YieldMany, StandaloneCondExpr,
        StandaloneCondition,
        OrderBy, Order, OrderByOperand, SelectFrom, SelectSource, SelectEnd, SourcelessSelect, DateIntervalUnit, MembershipPredicate
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
        groupBy, asc, desc, order, cond, orderBy,
    }

    private CanonicalEqlGrammar() {}

}