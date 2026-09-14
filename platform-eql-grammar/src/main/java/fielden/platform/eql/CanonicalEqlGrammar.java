package fielden.platform.eql;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Terminal;
import fielden.platform.bnf.Variable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;

import static fielden.platform.bnf.FluentBNF.start;
import static fielden.platform.bnf.Metadata.inline;
import static fielden.platform.bnf.Notation.*;
import static fielden.platform.bnf.Terms.*;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.*;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlTerminal.values;
import static fielden.platform.eql.CanonicalEqlGrammar.EqlVariable.*;

/**
 * Canonical representation of EQL's grammar.
 */
public final class CanonicalEqlGrammar {

    // Short names
    private static final Class<String> STR = String.class;
    private static final Class<Object> OBJ = Object.class;
    private static final Class<Enum> ENUM = Enum.class;
    private static final Class<CharSequence> CS = CharSequence.class;

    /**
     * Canonical EQL grammar in Extended Backus-Naur form.
     * <p>
     * <b>NOTE</b>: Should <b>not</b> be used for fluent API generation but for <b>reference</b> only.
     */
    // @formatter:off
    public static final BNF canonical_bnf =
        start(Query).

        specialize(Query).
            into(Select, StandaloneExpression, StandaloneCondExpr, StandaloneOrderBy).

        specialize(Select).
            into(SelectFrom, SourcelessSelect).

        derive(SelectFrom).
            to(SelectSource,
               opt(label("alias", as).with(CS)),
               opt(Join),
               opt(Where),
               opt(GroupBy),
               opt(OrderBy),
               SelectEnd).

        derive(SelectSource).
            to(label("select", select.with(Class.class))).
            or(label("select", select.rest(EntityResultQueryModel.class))).
            or(label("select", select.rest(AggregatedResultQueryModel.class))).

        derive(SourcelessSelect).
            to(select, opt(GroupBy), SelectEnd).

        specialize(SelectEnd).
            into(altLabel("SelectEnd_Model", Model),
                 altLabel("SelectEnd_AnyYield", AnyYield)).

        derive(Where).
            to(where, Condition).

        // AND takes precedence over OR
        specialize(Condition).
            into(altLabel("PredicateCondition", Predicate),
                 AndCondition, OrCondition, CompoundCondition, NegatedCompoundCondition).

        derive(AndCondition).
            to(label("left", Condition), and, label("right", Condition)).

        derive(OrCondition).
            to(label("left", Condition), or, label("right", Condition)).

        derive(CompoundCondition).
            to(begin, Condition, end).

        derive(NegatedCompoundCondition).
            to(notBegin, Condition, end).

        specialize(Predicate).
            into(UnaryPredicate, ComparisonPredicate, QuantifiedComparisonPredicate, LikePredicate, MembershipPredicate, SingleConditionPredicate).

        derive(UnaryPredicate).
            to(label("left", ComparisonOperand), UnaryComparisonOperator).

        derive(ComparisonPredicate).
            to(label("left", ComparisonOperand), ComparisonOperator, label("right", ComparisonOperand)).

        derive(QuantifiedComparisonPredicate).
            to(label("left", ComparisonOperand), ComparisonOperator, QuantifiedOperand).

        derive(LikePredicate).
            to(label("left", ComparisonOperand), LikeOperator, label("right", ComparisonOperand)).

        derive(MembershipPredicate).
            to(label("left", ComparisonOperand), MembershipOperator, MembershipOperand).

        derive(UnaryComparisonOperator).
            to(isNull).or(isNotNull).

        derive(LikeOperator).
            to(like).or(iLike).or(likeWithCast).or(iLikeWithCast).
            or(notLike).or(notLikeWithCast).or(notILikeWithCast).or(notILike).

        specialize(ComparisonOperand).
            into(altLabel("ComparisonOperand_Expr", Expr),
                 altLabel("ComparisonOperand_Multi", MultiOperand)).

        derive(ComparisonOperator).
            to(eq).or(gt).or(lt).or(ge).or(le).or(ne).

        derive(QuantifiedOperand).
            to(all.with(SingleResultQueryModel.class)).
            or(any.with(SingleResultQueryModel.class)).

        specialize(Expr).
            into(ExprCompound,
                 altLabel("Expr_SingleOperand", SingleOperand)).

        derive(ExprCompound).
            to(beginExpr, label("first", Expr), repeat(listLabel("operators", ArithmeticalOperator), listLabel("rest", Expr)), endExpr).

        derive(ArithmeticalOperator).
            to(add).or(sub).or(div).or(mult).or(mod).
        derive(SingleOperand).
            to(Prop).or(ExtProp).
            or(Val).or(Param).
            or(altLabel("SingleOperand_Expr", label("token", expr.with(ExpressionModel.class)))).
            or(altLabel("SingleOperand_Model", label("token", model.with(SingleResultQueryModel.class)))).
            or(UnaryFunction).
            or(IfNull).
            or(altLabel("SingleOperand_Now", now)).
            or(DateDiffInterval).
            or(DateAddInterval).
            or(Round).
            or(Ceil).
            or(Floor).
            or(Concat).
            or(CaseWhen).

        derive(UnaryFunction).
            to(label("funcName", UnaryFunctionName), label("argument", Expr)).

        derive(UnaryFunctionName).
            to(upperCase).or(lowerCase).
            or(secondOf).or(minuteOf).or(hourOf).or(dayOf).or(monthOf).or(yearOf).or(dayOfWeekOf).
            or(absOf).
            or(dateOf).

        derive(IfNull).
            to(ifNull, label("nullable", Expr), then, label("other", Expr)).

        derive(DateDiffInterval).
            to(count, label("unit", DateIntervalUnit), between, label("endDate", Expr), and, label("startDate", Expr)).

        derive(DateIntervalUnit).
            to(seconds).or(minutes).or(hours).or(days).or(months).or(years).

        derive(DateAddInterval).
            to(addTimeIntervalOf, label("left", Expr), label("unit", DateIntervalUnit), to, label("right", Expr)).

        derive(Round).
            to(round, Expr, label("to", to.with(Integer.class))).

        derive(Ceil).
            to(ceil, Expr).

        derive(Floor).
            to(floor, Expr).

        derive(Concat).
            to(concat, listLabel("operands", Expr), (repeat(with, listLabel("operands", Expr))), end).

        derive(CaseWhen).
            to(caseWhen, listLabel("whens", Condition), then, listLabel("thens", Expr),
                    repeat(when, listLabel("whens", Condition), then, listLabel("thens", Expr)),
                    opt(otherwise, label("otherwiseOperand", Expr)),
                    CaseWhenEnd).

        derive(CaseWhenEnd).
            to(end).or(endAsInt).or(endAsBool).
            or(endAsStr.with(Integer.class)).
            or(endAsDecimal.with(Integer.class, Integer.class)).

        derive(Prop).
            to(prop.with(CS)).or(prop.with(ENUM)).

        derive(ExtProp).
            to(extProp.with(CS)).or(extProp.with(ENUM)).

        derive(Val).
            to(val.with(OBJ)).or(iVal.with(OBJ)).

        derive(Param).
            to(param.with(CS)).or(param.with(ENUM)).
            or(iParam.with(CS)).or(iParam.with(ENUM)).

        derive(MultiOperand).
            to(anyOfProps.rest(CS)).
            or(allOfProps.rest(CS)).
            or(anyOfValues.rest(OBJ)).or(allOfValues.rest(OBJ)).
            or(anyOfParams.rest(CS)).or(anyOfIParams.rest(CS)).
            or(allOfParams.rest(CS)).or(allOfIParams.rest(CS)).
            or(anyOfModels.rest(PrimitiveResultQueryModel.class)).
            or(allOfModels.rest(PrimitiveResultQueryModel.class)).
            or(anyOfExpressions.rest(ExpressionModel.class)).
            or(allOfExpressions.rest(ExpressionModel.class)).

        derive(MembershipOperator).
            to(in).or(notIn).

        derive(MembershipOperand).
            to(values.rest(OBJ)).
            or(props.rest(CS)).
            or(params.rest(CS)).or(iParams.rest(CS)).
            or(model.with(SingleResultQueryModel.class)).

        derive(SingleConditionPredicate).
            to(exists.with(QueryModel.class)).
            or(notExists.with(QueryModel.class)).
            or(existsAnyOf.rest(QueryModel.class)).
            or(notExistsAnyOf.rest(QueryModel.class)).
            or(existsAllOf.rest(QueryModel.class)).
            or(notExistsAllOf.rest(QueryModel.class)).
            or(critCondition.with(CS, CS)).
            or(critCondition.with(ICompoundCondition0.class, CS, CS)).
            or(critCondition.with(ICompoundCondition0.class, CS, CS, OBJ)).
            or(condition.with(ConditionModel.class)).
            or(negatedCondition.with(ConditionModel.class)).

        derive(Join).
            to(JoinOperator, opt(label("alias", as).with(CS)), JoinCondition, opt(Join)).

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
            to(repeat1(groupBy, listLabel("operands", Expr))).

        specialize(AnyYield).
            into(YieldAll, YieldSome).

        derive(YieldAll).
            to(yieldAll, repeat(AliasedYield), YieldManyModel).

        derive(YieldSome).
            to(yield, label("firstYield", YieldOperand), YieldTail).

        specialize(YieldTail).
            into(Yield1Tail, YieldManyTail).

        derive(Yield1Tail).
            to(Yield1Model).

        derive(YieldManyTail).
            to(label("firstAlias", YieldAlias), repeat(listLabel("restYields", AliasedYield)), YieldManyModel).

        derive(AliasedYield).
            to(yield, YieldOperand, YieldAlias).

        derive(YieldOperand).
            to(altLabel("YieldOperand_SingleOperand", SingleOperand)).
            or(YieldOperandExpr).
            or(altLabel("YieldOperand_CountAll", countAll)).
            or(YieldOperandFunction).
            or(YieldOperandConcatOf).

        derive(YieldOperandExpr).
            to(beginExpr, label("first", YieldOperand), repeat(listLabel("operators", ArithmeticalOperator), listLabel("rest", YieldOperand)), endExpr).

        derive(YieldOperandFunction).
            to(label("funcName", YieldOperandFunctionName), label("argument", Expr)).

        derive(YieldOperandFunctionName).
            to(maxOf).or(minOf).or(sumOf).or(countOf).or(avgOf).
            or(sumOfDistinct).or(countOfDistinct).or(avgOfDistinct).

        derive(YieldOperandConcatOf).
            to(concatOf, label("argExpr", Expr), opt(YieldOperandConcatOfOrderBy), separator, label("separator", YieldOperandConcatOfSeparator)).

        derive(YieldOperandConcatOfSeparator).
            to(val.with(CS)).
            or(param.with(CS)).or(param.with(ENUM)).

        derive(YieldOperandConcatOfOrderBy).
            to(orderBy, repeat1(listLabel("operands", YieldOperandConcatOfOrderByOperand))).

        specialize(YieldOperandConcatOfOrderByOperand).
            into(YieldOperandConcatOfOrderByOperand_Expr, YieldOperandConcatOfOrderByOperand_OrderingModel).

        derive(YieldOperandConcatOfOrderByOperand_Expr).
            to(Expr, Order).

        derive(YieldOperandConcatOfOrderByOperand_OrderingModel).
            to(order.with(OrderingModel.class)).

        derive(YieldAlias).
            to(as.with(CS)).or(as.with(ENUM)).
            or(asRequired.with(CS)).or(asRequired.with(ENUM)).

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
            to(expr, label("first", YieldOperand), repeat(listLabel("operators", ArithmeticalOperator), listLabel("rest", YieldOperand)), model).

        derive(StandaloneCondExpr).
            to(cond, StandaloneCondition, model).

        specialize(StandaloneCondition).
            into(altLabel("StandaloneCondition_Predicate", Predicate),
                 AndStandaloneCondition, OrStandaloneCondition).

        derive(AndStandaloneCondition).
            to(label("left", StandaloneCondition), and, label("right", StandaloneCondition)).

        derive(OrStandaloneCondition).
            to(label("left", StandaloneCondition), or, label("right", StandaloneCondition)).

        derive(StandaloneOrderBy).
            to(orderBy, repeat1(listLabel("operands", OrderByOperand)), opt(Limit), opt(Offset), model).

        derive(OrderBy).
            to(orderBy, repeat1(listLabel("operands", OrderByOperand)), opt(Limit), opt(Offset)).

        specialize(OrderByOperand).
            into(OrderByOperand_Expr, OrderByOperand_Yield, OrderByOperand_OrderingModel).

        derive(OrderByOperand_Expr).
            to(Expr, Order).

        derive(OrderByOperand_Yield).
            to(label("yield", yield.with(CS)), Order).

        derive(OrderByOperand_OrderingModel).
            to(order.with(OrderingModel.class)).

        derive(Order).
            to(asc).or(desc).

        derive(Limit).
            to(label("limit", limit.with(long.class))).
            or(label("limit", limit.with(ua.com.fielden.platform.entity.query.fluent.Limit.class))).

        derive(Offset).
            to(label("offset", offset.with(long.class))).

        annotate(Select, inline()).
        annotate(SelectFrom, inline()).
        annotate(SelectSource, inline()).
        annotate(StandaloneExpression, inline()).
        annotate(StandaloneCondExpr, inline()).
        annotate(StandaloneOrderBy, inline()).

        annotate(AndCondition, inline()).
        annotate(OrCondition, inline()).
        annotate(CompoundCondition, inline()).
        annotate(NegatedCompoundCondition, inline()).

        annotate(UnaryPredicate, inline()).
        annotate(ComparisonPredicate, inline()).
        annotate(QuantifiedComparisonPredicate, inline()).
        annotate(LikePredicate, inline()).
        annotate(MembershipPredicate, inline()).
        annotate(SingleConditionPredicate, inline()).

        annotate(Prop, inline()).
        annotate(ExtProp, inline()).
        annotate(Val, inline()).
        annotate(Param, inline()).
        annotate(UnaryFunction, inline()).
        annotate(IfNull, inline()).
        annotate(DateDiffInterval, inline()).
        annotate(DateAddInterval, inline()).
        annotate(Round, inline()).
        annotate(Ceil, inline()).
        annotate(Floor, inline()).
        annotate(Concat, inline()).
        annotate(CaseWhen, inline()).
        annotate(ExprCompound, inline()).

        annotate(YieldAll, inline()).
        annotate(YieldSome, inline()).
        annotate(Yield1Tail, inline()).
        annotate(YieldManyTail, inline()).
        annotate(YieldOperandFunction, inline()).
        annotate(YieldOperandExpr, inline()).
        annotate(YieldOperandConcatOf, inline()).

        annotate(AndStandaloneCondition, inline()).
        annotate(OrStandaloneCondition, inline()).

        annotate(OrderByOperand_Expr, inline()).
        annotate(OrderByOperand_Yield, inline()).
        annotate(OrderByOperand_OrderingModel, inline()).
        annotate(YieldOperandConcatOfOrderByOperand_Expr, inline()).
        annotate(YieldOperandConcatOfOrderByOperand_OrderingModel, inline()).
        annotate(Limit, inline()).
        annotate(Offset, inline()).

        build();
    // @formatter:on

    public enum EqlVariable implements Variable {
        Query,
        Select,
        StandaloneExpression,
        Where,
        Condition, Predicate, OrCondition, AndCondition, CompoundCondition, NegatedCompoundCondition,
        SingleOperand, MultiOperand,
        ExtProp, Prop,
        UnaryComparisonOperator, Val, Param,
        ArithmeticalOperator, ExprCompound, Expr,
        UnaryFunction, UnaryFunctionName, IfNull, DateDiffInterval, DateAddInterval, Round, Ceil, Floor, Concat, CaseWhen, CaseWhenEnd,
        MembershipOperator,
        MembershipOperand, ComparisonOperator, ComparisonOperand, QuantifiedOperand, SingleConditionPredicate, Join, JoinOperator,
        JoinCondition,
        Model, GroupBy,
        AnyYield, YieldOperand, YieldOperandFunction, YieldOperandFunctionName, YieldAlias, LikeOperator, SubsequentYield,
        YieldOperandConcatOf, YieldOperandConcatOfSeparator, YieldOperandConcatOfOrderBy, YieldOperandConcatOfOrderByOperand,
        UnaryPredicate,
        ComparisonPredicate, QuantifiedComparisonPredicate, LikePredicate, StandaloneCondExpr,
        StandaloneCondition, OrStandaloneCondition, AndStandaloneCondition,
        StandaloneOrderBy, Order, OrderByOperand, SelectFrom, SelectSource, SelectEnd, SourcelessSelect, DateIntervalUnit,
        YieldAll, YieldSome, YieldTail, Yield1Tail, YieldManyTail, AliasedYield, YieldManyModel, Yield1Model,
        YieldOperandExpr,
        OrderByOperand_Yield, OrderByOperand_OrderingModel, OrderByOperand_Expr,
        OrderBy,
        Offset,
        Limit,
        YieldOperandConcatOfOrderByOperand_Expr, YieldOperandConcatOfOrderByOperand_OrderingModel, MembershipPredicate
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
        ceil,
        floor,
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
        concatOf,
        separator,
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
        groupBy, asc, desc, order, cond, orderBy, limit, offset,
    }

    private CanonicalEqlGrammar() {}

}
