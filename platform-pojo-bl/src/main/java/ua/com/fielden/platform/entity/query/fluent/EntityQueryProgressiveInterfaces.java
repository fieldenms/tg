package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
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

    interface IComparisonOperator<T extends ILogicalOperator<?>, ET extends AbstractEntity<?>> {
        T isNull();

        T isNotNull();

        IComparisonSetOperand<T> in();

        IComparisonSetOperand<T> notIn();

        IComparisonOperand<T, ET> like();

        IComparisonOperand<T, ET> iLike();

        IComparisonOperand<T, ET> notLike();

        IComparisonOperand<T, ET> notILike();

        IComparisonQuantifiedOperand<T, ET> eq();

        IComparisonQuantifiedOperand<T, ET> ne();

        IComparisonQuantifiedOperand<T, ET> gt();

        IComparisonQuantifiedOperand<T, ET> lt();

        IComparisonQuantifiedOperand<T, ET> ge();

        IComparisonQuantifiedOperand<T, ET> le();
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

    interface ISingleOperand<T, ET extends AbstractEntity<?>> {
        T prop(String propertyName);
        T extProp(String propertyName);
        T val(Object value);
        T iVal(Object value);
        T param(String paramName);
        T iParam(String paramName);
        T model(SingleResultQueryModel<?> model);
        T expr(ExpressionModel Expr);

        // built-in SQL functions
        T now();
        IDateDiffIntervalFunction<T, ET> count();
        IFunctionLastArgument<T, ET> upperCase();
        IFunctionLastArgument<T, ET> lowerCase();
        IFunctionLastArgument<T, ET> secondOf();
        IFunctionLastArgument<T, ET> minuteOf();
        IFunctionLastArgument<T, ET> hourOf();
        IFunctionLastArgument<T, ET> dayOf();
        IFunctionLastArgument<T, ET> monthOf();
        IFunctionLastArgument<T, ET> yearOf();
        IIfNullFunctionArgument<T, ET> ifNull();
        IFunctionWhere0<T, ET> caseWhen();
        IRoundFunctionArgument<T, ET> round();
        IConcatFunctionArgument<T, ET> concat();
    }

    interface IMultipleOperand<T, ET extends AbstractEntity<?>> //
    extends ISingleOperand<T, ET> {
        T anyOfProps(String... propertyNames);
        T anyOfValues(Object... values);
        T anyOfParams(String... paramNames);
        T anyOfModels(PrimitiveResultQueryModel... models);
        T anyOfExpressions(ExpressionModel... Expressions);
        T allOfProps(String... propertyNames);
        T allOfValues(Object... values);
        T allOfParams(String... paramNames);
        T allOfModels(PrimitiveResultQueryModel... models);
        T allOfExpressions(ExpressionModel... expressions);
    }

    interface IComparisonOperand<T, ET extends AbstractEntity<?>> //
    extends IMultipleOperand<T, ET>, //
    /*    */IBeginExpression<IExprOperand0<T, ET>> /*another entry point*/{
    }

    interface IExistenceOperator<T extends ILogicalOperator<?>>  {
        T exists(QueryModel subQuery);

        T notExists(QueryModel subQuery);

        T existsAnyOf(QueryModel ... subQueries);

        T notExistsAnyOf(QueryModel ... subQueries);

        T existsAllOf(QueryModel ... subQueries);

        T notExistsAllOf(QueryModel ... subQueries);
    }

    interface IQuantifiedOperand<T, ET extends AbstractEntity<?>> //
    extends IMultipleOperand<T, ET> {
        T all(SingleResultQueryModel subQuery);
        T any(SingleResultQueryModel subQuery);
    }

    interface IComparisonQuantifiedOperand<T, ET extends AbstractEntity<?>> //
    extends IQuantifiedOperand<T, ET>, //
    /*    */IBeginExpression<IExprOperand0<T, ET>> /*another entry point*/{
    }

    interface IComparisonSetOperand<T> {
        <E extends Object> T values(E... values);
        T props(String... properties);
        T params(String... paramNames);
        T model(SingleResultQueryModel model);
        // beginSet();
    }

    interface IYieldOperand<T, ET extends AbstractEntity<?>> //
    extends ISingleOperand<T, ET> {
        IFunctionLastArgument<T, ET> maxOf();
        IFunctionLastArgument<T, ET> minOf();
        IFunctionLastArgument<T, ET> sumOf();
        IFunctionLastArgument<T, ET> countOf();
        IFunctionLastArgument<T, ET> avgOf();
        T countAll();
        IFunctionLastArgument<T, ET> sumOfDistinct();
        IFunctionLastArgument<T, ET> countOfDistinct();
        IFunctionLastArgument<T, ET> avgOfDistinct();
        T join(String joinAlias);
    }

    interface IDateDiffFunction<T, ET extends AbstractEntity<?>> {
        IDateDiffFunctionArgument<T, ET> between();
    }

    interface IDateDiffIntervalFunction<T, ET extends AbstractEntity<?>> {
	IDateDiffFunction<T, ET> seconds();
	IDateDiffFunction<T, ET> minutes();
	IDateDiffFunction<T, ET> hours();
	IDateDiffFunction<T, ET> days();
	IDateDiffFunction<T, ET> months();
	IDateDiffFunction<T, ET> years();
    }


    interface ICaseWhenFunction<T, ET extends AbstractEntity<?>> {
        ICaseWhenFunctionArgument<T, ET> then();
    }

    interface IDateDiffFunctionBetween<T, ET extends AbstractEntity<?>> {
        IFunctionLastArgument<T, ET> and();
    }

    interface ICaseWhenFunctionEnd<T> {
        T end();
    }

    interface ICaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> //
    extends ICaseWhenFunctionEnd<T> {
        ICaseWhenFunctionLastArgument<T, ET>  otherwise();
    }

    interface ICaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> //
    extends ICaseWhenFunctionElseEnd<T, ET> {
        IFunctionWhere0<T, ET> when();
    }

    interface IIfNullFunctionThen<T, ET extends AbstractEntity<?>> {
        IFunctionLastArgument<T, ET> then();
    }

    interface IConcatFunctionWith<T, ET extends AbstractEntity<?>> {
	IConcatFunctionArgument<T, ET> with();
	T end();
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

    public interface IJoin<ET extends AbstractEntity<?>> //
    extends IPlainJoin<ET> {

        <T extends AbstractEntity<?>> IJoinAlias<ET> join(final Class<T> entityType);

        <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final Class<T> entityType);

        <T extends AbstractEntity<?>> IJoinAlias<ET> join(final EntityResultQueryModel<T> model);

        <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final EntityResultQueryModel<T> model);

        IJoinAlias<ET> join(final AggregatedResultQueryModel model);

        IJoinAlias<ET> leftJoin(final AggregatedResultQueryModel model);

    }

    interface IJoinAlias<ET extends AbstractEntity<?>> //
    extends IJoinCondition<ET> {
	IJoinCondition<ET> as(String alias);
    }

    interface IFromAlias<ET extends AbstractEntity<?>> //
    extends IJoin<ET> {
	IJoin<ET> as(String alias);
    }

    public interface IPlainJoin<ET extends AbstractEntity<?>> //
    extends ICompleted<ET> {
        IWhere0<ET> where();
    }

    public interface IJoinCondition<ET extends AbstractEntity<?>> {
        IJoinWhere0<ET> on();
    }

    public interface ICompleted<ET extends AbstractEntity<?>> //
    extends ICompletedAndYielded<ET> {
        IFunctionLastArgument<ICompleted<ET>, ET> groupBy();
    }

    public interface ICompletedAndYielded<ET extends AbstractEntity<?>> //
    extends ICompletedCommon<ET> {
        IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield();
        //////////////////// RETURN /////////////////////////
        EntityResultQueryModel<ET> model();
    }

    public interface ISubsequentCompletedAndYielded<ET extends AbstractEntity<?>> //
    extends ICompletedCommon<ET>{
        IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield();
    }

    public interface ICompletedCommon<ET extends AbstractEntity<?>> {
        //////////////////// RETURN /////////////////////////
        <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(Class<T> resultType);
        AggregatedResultQueryModel modelAsAggregate();
    }

//    interface IWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, T3, ET extends AbstractEntity<?>> //
//    extends IComparisonOperand<T1, ET>, //
//    /*    */IExistenceOperator<T2>, //
//    /*    */IBeginCondition<T3> {
//        T2 condition(ConditionModel condition);
//    }
    interface IWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, T3, ET extends AbstractEntity<?>> //
    extends IWhereWithoutNesting<T1, T2, ET>, //
    /*    */IBeginCondition<T3> {
    }

    interface IWhereWithoutNesting<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
    extends IComparisonOperand<T1, ET>, //
    /*    */IExistenceOperator<T2> {
        T2 condition(ConditionModel condition);
    }

    interface ICompoundCondition<T1, T2> //
    extends ILogicalOperator<T1>, //
    /*    */IEndCondition<T2> {
    }

    //////////////////////////////////////////////////////////---JOIN ---//////////////////////////////////////////////////////////
    interface IJoinWhere0<ET extends AbstractEntity<?>> //
    extends IWhere<IJoinComparisonOperator0<ET>, IJoinCompoundCondition0<ET>, IJoinWhere1<ET>, ET> {
    }

    interface IJoinWhere1<ET extends AbstractEntity<?>> //
    extends IWhere<IJoinComparisonOperator1<ET>, IJoinCompoundCondition1<ET>, IJoinWhere2<ET>, ET> {
    }

    interface IJoinWhere2<ET extends AbstractEntity<?>> //
    extends IWhere<IJoinComparisonOperator2<ET>, IJoinCompoundCondition2<ET>, IJoinWhere3<ET>, ET> {
    }

    interface IJoinWhere3<ET extends AbstractEntity<?>> //
    extends IComparisonOperand<IJoinComparisonOperator3<ET>, ET>, //
    /*    */IExistenceOperator<IJoinCompoundCondition3<ET>> {
    }

    //-------------------------------------------
    interface IJoinCompoundCondition0<ET extends AbstractEntity<?>> //
    extends ILogicalOperator<IJoinWhere0<ET>>, //
    /*    */IJoin<ET> {
    }

    interface IJoinCompoundCondition1<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IJoinWhere1<ET>, IJoinCompoundCondition0<ET>> {
    }

    interface IJoinCompoundCondition2<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IJoinWhere2<ET>, IJoinCompoundCondition1<ET>> {
    }

    interface IJoinCompoundCondition3<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IJoinWhere3<ET>, IJoinCompoundCondition2<ET>> {
    }

    //-------------------------------------------
    interface IJoinComparisonOperator0<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IJoinCompoundCondition0<ET>, ET> {
    }

    interface IJoinComparisonOperator1<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IJoinCompoundCondition1<ET>, ET> {
    }

    interface IJoinComparisonOperator2<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IJoinCompoundCondition2<ET>, ET> {
    }

    interface IJoinComparisonOperator3<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IJoinCompoundCondition3<ET>, ET> {
    }

    //////////////////////////////////////////////////////////---WHERE ---/////////////////////////////////////////////////////////
    interface IComparisonOperator0<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<ICompoundCondition0<ET>, ET> {
    }

    interface IComparisonOperator1<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<ICompoundCondition1<ET>, ET> {
    }

    interface IComparisonOperator2<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<ICompoundCondition2<ET>, ET> {
    }

    interface IComparisonOperator3<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<ICompoundCondition3<ET>, ET> {
    }

    //-------------------------------------------
    interface ICompoundCondition0<ET extends AbstractEntity<?>> //
    extends ILogicalOperator<IWhere0<ET>>, ICompleted<ET> {
    }

    interface ICompoundCondition1<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IWhere1<ET>, ICompoundCondition0<ET>> {
    }

    interface ICompoundCondition2<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IWhere2<ET>, ICompoundCondition1<ET>> {
    }

    interface ICompoundCondition3<ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IWhere3<ET>, ICompoundCondition2<ET>> {
    }

    //-------------------------------------------
    interface IWhere0<ET extends AbstractEntity<?>> //
    extends IWhere<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> /*Exp entry point*/{
    }

    interface IWhere1<ET extends AbstractEntity<?>> //
    extends IWhere<IComparisonOperator1<ET>, ICompoundCondition1<ET>, IWhere2<ET>, ET> {
    }

    interface IWhere2<ET extends AbstractEntity<?>> //
    extends IWhere<IComparisonOperator2<ET>, ICompoundCondition2<ET>, IWhere3<ET>, ET> {
    }

    interface IWhere3<ET extends AbstractEntity<?>> //
    extends IComparisonOperand<IComparisonOperator3<ET>, ET>, //
    /*    */IExistenceOperator<ICompoundCondition3<ET>> {
    }

    //////////////////////////////////////////////////////////---FUNCTION ---/////////////////////////////////////////////////////////
    interface IFunctionWhere0<T, ET extends AbstractEntity<?>> //
    extends IWhere<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> /*Exp entry point*/{
    }

    interface IFunctionWhere1<T, ET extends AbstractEntity<?>> //
    extends IWhere<IFunctionComparisonOperator1<T, ET>, IFunctionCompoundCondition1<T, ET>, IFunctionWhere2<T, ET>, ET> {
    }

    interface IFunctionWhere2<T, ET extends AbstractEntity<?>> //
    extends IWhere<IFunctionComparisonOperator2<T, ET>, IFunctionCompoundCondition2<T, ET>, IFunctionWhere3<T, ET>, ET> {
    }

    interface IFunctionWhere3<T, ET extends AbstractEntity<?>> //
    extends IComparisonOperand<IFunctionComparisonOperator3<T, ET>, ET>, //
    /*    */IExistenceOperator<IFunctionCompoundCondition3<T, ET>> {
    }

    //-------------------------------------------
    interface IFunctionCompoundCondition0<T, ET extends AbstractEntity<?>> //
    extends ILogicalOperator<IFunctionWhere0<T, ET>>, //
    /*    */ICaseWhenFunction<T, ET> {
    }

    interface IFunctionCompoundCondition1<T, ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IFunctionWhere1<T, ET>, IFunctionCompoundCondition0<T, ET>> {
    }

    interface IFunctionCompoundCondition2<T, ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> {
    }

    interface IFunctionCompoundCondition3<T, ET extends AbstractEntity<?>> //
    extends ICompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> {
    }

    //-------------------------------------------
    interface IFunctionComparisonOperator0<T, ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> {
    }

    interface IFunctionComparisonOperator1<T, ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IFunctionCompoundCondition1<T, ET>, ET> {
    }

    interface IFunctionComparisonOperator2<T, ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IFunctionCompoundCondition2<T, ET>, ET> {
    }

    interface IFunctionComparisonOperator3<T, ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IFunctionCompoundCondition3<T, ET>, ET> {
    }

    ///////////////////////////////////////////////---EXPRESSION (WHERE, GROUP, ORDER) ---///////////////////////////////////////
    interface IExprOperand<T1, T2, ET extends AbstractEntity<?>> //
    extends ISingleOperand<T1, ET>, //
    /*    */IBeginExpression<T2> {
    }

    interface IExprOperand0<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> {
    }

    interface IExprOperand1<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IExprOperationOrEnd1<T, ET>, IExprOperand2<T, ET>, ET> {
    }

    interface IExprOperand2<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> {
    }

    interface IExprOperand3<T, ET extends AbstractEntity<?>> //
    extends ISingleOperand<IExprOperationOrEnd3<T, ET>, ET> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IExprOperationOrEnd<T1, T2, ET extends AbstractEntity<?>> //
    extends IArithmeticalOperator<T1>, //
    /*    */IEndExpression<T2> {
    }

    interface IExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> {
    }

    interface IExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET> {
    }

    interface IExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IExprOperand2<T, ET>, IExprOperationOrEnd1<T, ET>, ET> {
    }

    interface IExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IExprOperand3<T, ET>, IExprOperationOrEnd2<T, ET>, ET> {
    }

    /////////////////////////////////////////////////////---EXPRESSION (YIELD) ---///////////////////////////////////////////////////
    interface IYieldExprOperand<T1, T2, ET extends AbstractEntity<?>> //
    extends IYieldOperand<T1, ET>, //
    /*    */IBeginExpression<T2> {
    }

    interface IYieldExprItem0<T, ET extends AbstractEntity<?>> //
    extends IYieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> {
    }

    interface IYieldExprItem1<T, ET extends AbstractEntity<?>> //
    extends IYieldExprOperand<IYieldExprOperationOrEnd1<T, ET>, IYieldExprItem2<T, ET>, ET> {
    }

    interface IYieldExprItem2<T, ET extends AbstractEntity<?>> //
    extends IYieldExprOperand<IYieldExprOperationOrEnd2<T, ET>, IYieldExprItem3<T, ET>, ET> {
    }

    interface IYieldExprItem3<T, ET extends AbstractEntity<?>> //
    extends IYieldOperand<IYieldExprOperationOrEnd3<T, ET>, ET> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IYieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> {
    }

    interface IYieldExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IYieldExprItem1<T, ET>, IYieldExprOperationOrEnd0<T, ET>, ET> {
    }

    interface IYieldExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IYieldExprItem2<T, ET>, IYieldExprOperationOrEnd1<T, ET>, ET> {
    }

    interface IYieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
    extends IExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET> {
    }

    /////////////////////////////////////////////////////---OTHERS ---///////////////////////////////////////////////////
    interface IDateDiffFunctionArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET>{
    }

    interface ICaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> {
    }

    interface ICaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> {
    }

    interface IIfNullFunctionArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> {
    }

    interface IRoundFunctionArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>, ET>, ET> {
    }

    interface IFunctionLastArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<T, IExprOperand0<T, ET>, ET> {
    }

    interface IFunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> //
    extends IYieldExprOperand<T, IYieldExprItem0<T, ET>, ET> {
    }

    interface IConcatFunctionArgument<T, ET extends AbstractEntity<?>> //
    extends IExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> {
    }

    /////////////////////////////////////////////////////---STAND-ALONE EXPRESSION ---///////////////////////////////////////////////////
    interface IStandAloneExprOperationAndClose //
    extends IArithmeticalOperator<IStandAloneExprOperand> {
	ExpressionModel model();
    }

    interface IStandAloneExprOperand //
    extends IYieldOperand<IStandAloneExprOperationAndClose, AbstractEntity<?>> {
    }

    /////////////////////////////////////////////////////---STAND-ALONE CONDITION ---///////////////////////////////////////////////////
    interface IStandAloneConditionOperand<ET extends AbstractEntity<?>> //
    extends IWhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET>{
    }

    interface IStandAloneConditionCompoundCondition<ET extends AbstractEntity<?>> //
    extends ILogicalOperator<IStandAloneConditionOperand<ET>> {
	ConditionModel model();
    }

    interface IStandAloneConditionComparisonOperator<ET extends AbstractEntity<?>> //
    extends IComparisonOperator<IStandAloneConditionCompoundCondition<ET>, ET> {
    }

    /////////////////////////////////////////////////////---ORDER BY ---///////////////////////////////////////////////////
    interface IOrder<T> {
        T asc();
        T desc();
    }

    interface ISingleOperandOrderable //
    extends IOrder<IOrderingItemCloseable> {
    }

    interface IOrderingItemCloseable //
    extends IOrderingItem {
	OrderingModel model();
    }

    interface IOrderingItem //
    extends IExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> {
	ISingleOperandOrderable yield(String yieldAlias);
    }
}