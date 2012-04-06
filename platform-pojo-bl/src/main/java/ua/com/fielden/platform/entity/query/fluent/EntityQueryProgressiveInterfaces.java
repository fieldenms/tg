package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

/**
 * Class for collecting all interfaces, which are part of Entity Query Progressive Interfaces.
 *
 * @author TG Team
 *
 */
public interface EntityQueryProgressiveInterfaces {

    interface IComparisonOperator<T extends ILogicalOperator<?>> {
        T isNull();

        T isNotNull();

        IComparisonSetOperand<T> in();

        IComparisonSetOperand<T> notIn();

        IComparisonOperand<T> like();

        IComparisonOperand<T> iLike();

        IComparisonOperand<T> notLike();

        IComparisonOperand<T> notILike();

        IComparisonQuantifiedOperand<T> eq();

        IComparisonQuantifiedOperand<T> ne();

        IComparisonQuantifiedOperand<T> gt();

        IComparisonQuantifiedOperand<T> lt();

        IComparisonQuantifiedOperand<T> ge();

        IComparisonQuantifiedOperand<T> le();
    }

    interface IBeginCondition<T> {
        /**
         * Starts new group of conditions (opens new parenthesis).
         *
         * @return
         */
        T begin();

        /**
         * Starts new negated group of conditions (opens new parenthesis with NOT preceding it).
         *
         * @return
         */
        T notBegin();
    }

    interface IEndCondition<T> {
        /**
         * Closes parenthesis of the nearest condition group.
         *
         * @return
         */
        T end();
    }

    interface ILogicalOperator<T> {
        T and();

        T or();
    }

    interface ISingleOperand<T> {
        T prop(String propertyName);
        T val(Object value);
        T iVal(Object value);
        T param(String paramName);
        T iParam(String paramName);
        T model(SingleResultQueryModel<?> model);
        T expr(ExpressionModel Expr);

        // built-in SQL functions
        T now();
        IDateDiffFunction<T> countDays();
        IFunctionLastArgument<T> upperCase();
        IFunctionLastArgument<T> lowerCase();
        IFunctionLastArgument<T> secondOf();
        IFunctionLastArgument<T> minuteOf();
        IFunctionLastArgument<T> hourOf();
        IFunctionLastArgument<T> dayOf();
        IFunctionLastArgument<T> monthOf();
        IFunctionLastArgument<T> yearOf();
        IIfNullFunctionArgument<T> ifNull();
        IFunctionWhere0<T> caseWhen();
        IRoundFunctionArgument<T> round();
    }

    interface IMultipleOperand<T> extends ISingleOperand<T> {
        T anyOfProps(String... propertyNames);
        T anyOfValues(Object... values);
        T anyOfParams(String... paramNames);
        T anyOfModels(PrimitiveResultQueryModel... models);
        T anyOfExpressions(ExpressionModel... Expressions);
        T allOfProps(String... propertyNames);
        T allOfValues(Object... values);
        T allOfParams(String... paramNames);
        T allOfModels(PrimitiveResultQueryModel... models);
        T allOfExpressions(ExpressionModel... Expressions);
    }

    interface IComparisonOperand<T> extends IMultipleOperand<T>, IBeginExpression<IExprOperand0<T>> /*another entry point*/{
    }

    interface IExistenceOperator<T extends ILogicalOperator<?>>  {
        T exists(QueryModel subQuery);

        T notExists(QueryModel subQuery);

        T existsAnyOf(QueryModel ... subQueries);

        T notExistsAnyOf(QueryModel ... subQueries);

        T existsAllOf(QueryModel ... subQueries);

        T notExistsAllOf(QueryModel ... subQueries);
    }

    interface IQuantifiedOperand<T> extends IMultipleOperand<T> {
        T all(SingleResultQueryModel subQuery);
        T any(SingleResultQueryModel subQuery);
    }

    interface IComparisonQuantifiedOperand<T> extends IQuantifiedOperand<T>, IBeginExpression<IExprOperand0<T>> /*another entry point*/{
    }

    interface IComparisonSetOperand<T> {
        <E extends Object> T values(E... values);
        T props(String... properties);
        T params(String... paramNames);
        T model(SingleResultQueryModel model);
        // beginSet();
    }

    interface IYieldOperand<T> extends ISingleOperand<T> {
        IFunctionLastArgument<T> maxOf();
        IFunctionLastArgument<T> minOf();
        IFunctionLastArgument<T> sumOf();
        IFunctionLastArgument<T> countOf();
        IFunctionLastArgument<T> avgOf();
        T countAll();
        IFunctionLastArgument<T> sumOfDistinct();
        IFunctionLastArgument<T> countOfDistinct();
        IFunctionLastArgument<T> avgOfDistinct();
        T join(String joinAlias);
    }

    interface IDateDiffFunction<T> {
        IDateDiffFunctionArgument<T> between();
    }

    interface ICaseWhenFunction<T> {
        ICaseWhenFunctionArgument<T> then();
    }

    interface IDateDiffFunctionBetween<T> {
        IFunctionLastArgument<T> and();
    }

    interface ICaseWhenFunctionEnd<T> {
        T end();
    }

    interface IIfNullFunctionThen<T> {
        IFunctionLastArgument<T> then();
    }

    interface IRoundFunctionTo<T> {
        T to(Integer precision);
    }

    interface IFirstYieldedItemAlias<T> {
        T as(String alias);
        <E extends AbstractEntity<?>> EntityResultQueryModel<E> modelAsEntity(final Class<E> entityType);
        PrimitiveResultQueryModel modelAsPrimitive();
    }

    interface ISubsequentYieldedItemAlias<T> /*extends ICompletedAndYielded*/ {
        T as(String alias);
    }

    interface IOrder<T> {
        T asc();
        T desc();
    }

    interface IArithmeticalOperator<T> {
        T add();
        T sub();
        T mult();
        T div();
    }

    interface IBeginExpression<T> {
        T beginExpr();
    }

    interface IEndExpression<T> {
        T endExpr();
    }

    public interface IJoin extends IPlainJoin {

        <T extends AbstractEntity<?>> IJoinAlias join(final Class<T> entityType);

        <T extends AbstractEntity<?>> IJoinAlias leftJoin(final Class<T> entityType);

        <T extends AbstractEntity<?>> IJoinAlias join(final EntityResultQueryModel<T> model);

        <T extends AbstractEntity<?>> IJoinAlias leftJoin(final EntityResultQueryModel<T> model);

        IJoinAlias join(final AggregatedResultQueryModel model);

        IJoinAlias leftJoin(final AggregatedResultQueryModel model);

    }

    interface IJoinAlias extends IJoinCondition {
	IJoinCondition as(String alias);
    }

    interface IFromAlias extends IJoin {
	IJoin as(String alias);
    }

    public interface IPlainJoin extends ICompleted {
        IWhere0 where();
    }

    public interface IJoinCondition {
        IJoinWhere0 on();
    }

    public interface ICompleted extends ICompletedAndYielded {
        IFunctionLastArgument<ICompleted> groupBy();
    }

    public interface ICompletedAndYielded extends ICompletedCommon {
        IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded>> yield();
        //////////////////// RETURN /////////////////////////
        <T extends AbstractEntity<?>>  EntityResultQueryModel<T> model();
    }

    public interface ISubsequentCompletedAndYielded extends ICompletedCommon{
        IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded>> yield();
    }

    public interface ICompletedCommon {
        //////////////////// RETURN /////////////////////////
        <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(Class<T> resultType);
        AggregatedResultQueryModel modelAsAggregate();
    }

    interface IWhere<T1 extends IComparisonOperator<T2>, T2 extends ILogicalOperator<?>, T3> extends IComparisonOperand<T1>, IExistenceOperator<T2>, IBeginCondition<T3> {
    //    T2 condition(Object condition);
    }

    interface ICompoundCondition<T1, T2> extends ILogicalOperator<T1>, IEndCondition<T2> {
    }

    //////////////////////////////////////////////////////////---JOIN ---//////////////////////////////////////////////////////////
    interface IJoinWhere0 extends IWhere<IJoinComparisonOperator0, IJoinCompoundCondition0, IJoinWhere1> {
    }

    interface IJoinWhere1 extends IWhere<IJoinComparisonOperator1, IJoinCompoundCondition1, IJoinWhere2> {
    }

    interface IJoinWhere2 extends IWhere<IJoinComparisonOperator2, IJoinCompoundCondition2, IJoinWhere3> {
    }

    interface IJoinWhere3 extends IComparisonOperand<IJoinComparisonOperator3>, IExistenceOperator<IJoinCompoundCondition3> {
    }

    //-------------------------------------------
    interface IJoinCompoundCondition0 extends ILogicalOperator<IJoinWhere0>, IJoin {
    }

    interface IJoinCompoundCondition1 extends ICompoundCondition<IJoinWhere1, IJoinCompoundCondition0> {
    }

    interface IJoinCompoundCondition2 extends ICompoundCondition<IJoinWhere2, IJoinCompoundCondition1> {
    }

    interface IJoinCompoundCondition3 extends ICompoundCondition<IJoinWhere3, IJoinCompoundCondition2> {
    }

    //-------------------------------------------
    interface IJoinComparisonOperator0 extends IComparisonOperator<IJoinCompoundCondition0> {
    }

    interface IJoinComparisonOperator1 extends IComparisonOperator<IJoinCompoundCondition1> {
    }

    interface IJoinComparisonOperator2 extends IComparisonOperator<IJoinCompoundCondition2> {
    }

    interface IJoinComparisonOperator3 extends IComparisonOperator<IJoinCompoundCondition3> {
    }

    //////////////////////////////////////////////////////////---WHERE ---/////////////////////////////////////////////////////////
    interface IComparisonOperator0 extends IComparisonOperator<ICompoundCondition0> {
    }

    interface IComparisonOperator1 extends IComparisonOperator<ICompoundCondition1> {
    }

    interface IComparisonOperator2 extends IComparisonOperator<ICompoundCondition2> {
    }

    interface IComparisonOperator3 extends IComparisonOperator<ICompoundCondition3> {
    }

    //-------------------------------------------
    interface ICompoundCondition0 extends ILogicalOperator<IWhere0>, ICompleted {
    }

    interface ICompoundCondition1 extends ICompoundCondition<IWhere1, ICompoundCondition0> {
    }

    interface ICompoundCondition2 extends ICompoundCondition<IWhere2, ICompoundCondition1> {
    }

    interface ICompoundCondition3 extends ICompoundCondition<IWhere3, ICompoundCondition2> {
    }

    //-------------------------------------------
    interface IWhere0 extends IWhere<IComparisonOperator0, ICompoundCondition0, IWhere1> /*Exp entry point*/{
    }

    interface IWhere1 extends IWhere<IComparisonOperator1, ICompoundCondition1, IWhere2> {
    }

    interface IWhere2 extends IWhere<IComparisonOperator2, ICompoundCondition2, IWhere3> {
    }

    interface IWhere3 extends IComparisonOperand<IComparisonOperator3>, IExistenceOperator<ICompoundCondition3> {
    }

    //////////////////////////////////////////////////////////---FUNCTION ---/////////////////////////////////////////////////////////
    interface IFunctionWhere0<T> extends IWhere<IFunctionComparisonOperator0<T>, IFunctionCompoundCondition0<T>, IFunctionWhere1<T>> /*Exp entry point*/{
    }

    interface IFunctionWhere1<T> extends IWhere<IFunctionComparisonOperator1<T>, IFunctionCompoundCondition1<T>, IFunctionWhere2<T>> {
    }

    interface IFunctionWhere2<T> extends IWhere<IFunctionComparisonOperator2<T>, IFunctionCompoundCondition2<T>, IFunctionWhere3<T>> {
    }

    interface IFunctionWhere3<T> extends IComparisonOperand<IFunctionComparisonOperator3<T>>, IExistenceOperator<IFunctionCompoundCondition3<T>> {
    }

    //-------------------------------------------
    interface IFunctionCompoundCondition0<T> extends ILogicalOperator<IFunctionWhere0<T>>, ICaseWhenFunction<T> {
    }

    interface IFunctionCompoundCondition1<T> extends ICompoundCondition<IFunctionWhere1<T>, IFunctionCompoundCondition0<T>> {
    }

    interface IFunctionCompoundCondition2<T> extends ICompoundCondition<IFunctionWhere2<T>, IFunctionCompoundCondition1<T>> {
    }

    interface IFunctionCompoundCondition3<T> extends ICompoundCondition<IFunctionWhere3<T>, IFunctionCompoundCondition2<T>> {
    }

    //-------------------------------------------
    interface IFunctionComparisonOperator0<T> extends IComparisonOperator<IFunctionCompoundCondition0<T>> {
    }

    interface IFunctionComparisonOperator1<T> extends IComparisonOperator<IFunctionCompoundCondition1<T>> {
    }

    interface IFunctionComparisonOperator2<T> extends IComparisonOperator<IFunctionCompoundCondition2<T>> {
    }

    interface IFunctionComparisonOperator3<T> extends IComparisonOperator<IFunctionCompoundCondition3<T>> {
    }

    ///////////////////////////////////////////////---EXPRESSION (WHERE, GROUP, ORDER) ---///////////////////////////////////////
    interface IExprOperand<T1, T2> extends ISingleOperand<T1>, IBeginExpression<T2> {
    }

    interface IExprOperand0<T> extends IExprOperand<IExprOperationOrEnd0<T>, IExprOperand1<T>> {
    }

    interface IExprOperand1<T> extends IExprOperand<IExprOperationOrEnd1<T>, IExprOperand2<T>> {
    }

    interface IExprOperand2<T> extends IExprOperand<IExprOperationOrEnd2<T>, IExprOperand3<T>> {
    }

    interface IExprOperand3<T> extends ISingleOperand<IExprOperationOrEnd3<T>> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IExprOperationOrEnd<T1, T2> extends IArithmeticalOperator<T1>, IEndExpression<T2> {
    }

    interface IExprOperationOrEnd0<T> extends IExprOperationOrEnd<IExprOperand0<T>, T> {
    }

    interface IExprOperationOrEnd1<T> extends IExprOperationOrEnd<IExprOperand1<T>, IExprOperationOrEnd0<T>> {
    }

    interface IExprOperationOrEnd2<T> extends IExprOperationOrEnd<IExprOperand2<T>, IExprOperationOrEnd1<T>> {
    }

    interface IExprOperationOrEnd3<T> extends IExprOperationOrEnd<IExprOperand3<T>, IExprOperationOrEnd2<T>> {
    }

    /////////////////////////////////////////////////////---EXPRESSION (YIELD) ---///////////////////////////////////////////////////
    interface IYieldExprOperand<T1, T2> extends IYieldOperand<T1>, IBeginExpression<T2> {
    }

    interface IYieldExprItem0<T> extends IYieldExprOperand<IYieldExprOperationOrEnd0<T>, IYieldExprItem1<T>> {
    }

    interface IYieldExprItem1<T> extends IYieldExprOperand<IYieldExprOperationOrEnd1<T>, IYieldExprItem2<T>> {
    }

    interface IYieldExprItem2<T> extends IYieldExprOperand<IYieldExprOperationOrEnd2<T>, IYieldExprItem3<T>> {
    }

    interface IYieldExprItem3<T> extends IYieldOperand<IYieldExprOperationOrEnd3<T>> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IYieldExprOperationOrEnd0<T> extends IExprOperationOrEnd<IYieldExprItem0<T>, T> {
    }

    interface IYieldExprOperationOrEnd1<T> extends IExprOperationOrEnd<IYieldExprItem1<T>, IYieldExprOperationOrEnd0<T>> {
    }

    interface IYieldExprOperationOrEnd2<T> extends IExprOperationOrEnd<IYieldExprItem2<T>, IYieldExprOperationOrEnd1<T>> {
    }

    interface IYieldExprOperationOrEnd3<T> extends IExprOperationOrEnd<IYieldExprItem3<T>, IYieldExprOperationOrEnd2<T>> {
    }

    /////////////////////////////////////////////////////---OTHERS ---///////////////////////////////////////////////////
    interface IDateDiffFunctionArgument<T> extends IExprOperand<IDateDiffFunctionBetween<T>, IExprOperand0<IDateDiffFunctionBetween<T>>>{
    }

    interface ICaseWhenFunctionArgument<T> extends IExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>>> {
    }

    interface IIfNullFunctionArgument<T> extends IExprOperand<IIfNullFunctionThen<T>, IExprOperand0<IIfNullFunctionThen<T>>> {
    }

    interface IRoundFunctionArgument<T> extends IExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>>> {
    }

    interface IFunctionLastArgument<T> extends IExprOperand<T, IExprOperand0<T>> {
    }

    interface IFunctionYieldedLastArgument<T> extends IYieldExprOperand<T, IYieldExprItem0<T>> {
    }


    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IStandAloneExprOperationAndClose extends IArithmeticalOperator<IStandAloneExprOperand>, IStandAloneExprCompleted {
    }

    interface IStandAloneExprOperand extends IYieldOperand<IStandAloneExprOperationAndClose> {
    }

    interface IStandAloneExprCompleted {
        ExpressionModel model();
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface ISingleOperandOrderable extends IOrder<IOrderingItemCloseable> {
    }

    interface IOrderingItemCloseable extends IOrderingItem {
	OrderingModel model();
    }

    interface IOrderingItem extends IExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable>> {
    }
}