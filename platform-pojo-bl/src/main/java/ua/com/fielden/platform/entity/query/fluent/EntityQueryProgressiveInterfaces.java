package ua.com.fielden.platform.entity.query.fluent;

import java.util.Collection;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

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

        /**
         * Case-insensitive 'like'.
         *
         * @return
         */
        IComparisonOperand<T, ET> iLike();

        /**
         * Like that casts its left operand to string in case it is integer.
         *
         * @return
         */
        IComparisonOperand<T, ET> likeWithCast();

        /**
         * Case-insensitive 'like' that casts its left operand to string in case it is integer.
         *
         * @return
         */
        IComparisonOperand<T, ET> iLikeWithCast();

        IComparisonOperand<T, ET> notLike();

        IComparisonOperand<T, ET> notLikeWithCast();

        IComparisonOperand<T, ET> notILikeWithCast();

        /**
         * Negated case-insensitive 'like'.
         *
         * @return
         */
        IComparisonOperand<T, ET> notILike();

        /**
         * Equal.
         *
         * @return
         */
        IComparisonQuantifiedOperand<T, ET> eq();

        /**
         * Not equal.
         *
         * @return
         */
        IComparisonQuantifiedOperand<T, ET> ne();

        /**
         * Greater than.
         *
         * @return
         */
        IComparisonQuantifiedOperand<T, ET> gt();

        /**
         * Less than.
         *
         * @return
         */
        IComparisonQuantifiedOperand<T, ET> lt();

        /**
         * Greater or equal.
         *
         * @return
         */
        IComparisonQuantifiedOperand<T, ET> ge();

        /**
         * Less or equal.
         *
         * @return
         */
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
         * Starts new negated group of conditions (opens new parenthesis with
         * NOT preceding it).
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
        /**
         * Property.
         *
         * @return
         */
        T prop(final CharSequence propertyName);

        /**
         * Property.
         *
         * @return
         */
        T prop(final Enum<?> propertyName);

        /**
         * External property (property from master query(ies).
         *
         * @return
         */
        T extProp(final CharSequence propertyName);

        /**
         * External property (property from master query(ies).
         *
         * @return
         */
        T extProp(final Enum<?> propertyName);

        /**
         * Value.
         *
         * @return
         */
        T val(final Object value);

        /**
         * Ignore value -- ignore condition with this operator if null is passed as an argument.
         *
         * @return
         */
        T iVal(final Object value);

        T param(final CharSequence paramName);

        T param(final Enum<?> paramName);

        /**
         * Ignore parameter -- ignore condition with this operator if the given parameter's value is set to null.
         */
        T iParam(final CharSequence paramName);

        /**
         * Ignore parameter -- ignore condition with this operator if the given parameter's value is set to null.
         */
        T iParam(final Enum<?> paramName);

        T model(final SingleResultQueryModel<?> model);

        /**
         * Expression.
         *
         * @return
         */
        T expr(final ExpressionModel Expr);

        // built-in SQL functions
        T now();

        /**
         * Start of an expression for counting of the specified datepart boundaries crossed between the specified enddate and startdate (e.g., there is 1 year crossed between 2024/01/01 00:00:00 and 2023/12/31 59:59:59).
         *
         * If enddate precedes startdate, the result will be negative.
         *
         * If some/all of the dates are NULL, the function result will also be NULL.
         *
         * The following dateparts are supported: seconds, minutes, hours, days, months, years.
         *
         * @return
         */
        IDateDiffIntervalFunction<T, ET> count();

        IFunctionLastArgument<T, ET> upperCase();

        IFunctionLastArgument<T, ET> lowerCase();

        IFunctionLastArgument<T, ET> secondOf();

        IFunctionLastArgument<T, ET> minuteOf();

        IFunctionLastArgument<T, ET> hourOf();

        IFunctionLastArgument<T, ET> dayOf();

        IFunctionLastArgument<T, ET> monthOf();

        IFunctionLastArgument<T, ET> yearOf();

        IFunctionLastArgument<T, ET> dayOfWeekOf();

        IIfNullFunctionArgument<T, ET> ifNull();

        /**
         * Start of an expression for adding a time interval, which is represented by an integer value, to some target value, property or a model result of type {@link Date}.
         *
         * @return
         */
        IDateAddIntervalFunctionArgument<T, ET> addTimeIntervalOf();

        IFunctionWhere0<T, ET> caseWhen();

        IRoundFunctionArgument<T, ET> round();

        /**
         * Start of an expression for concatenation of one, two or more operands.
         *
         * If one of the operands has NULL value, the result of concat() is also null.
         *
         * If operands of non-string values are involved, their values are implicitly converted to string.
         *
         * @return
         */
        IConcatFunctionArgument<T, ET> concat();

        IFunctionLastArgument<T, ET> absOf();

        IFunctionLastArgument<T, ET> dateOf();
    }

    interface IMultipleOperand<T, ET extends AbstractEntity<?>> //
            extends ISingleOperand<T, ET> {

        T anyOfProps(final Collection<? extends CharSequence> propertyNames);

        T anyOfProps(final CharSequence... propertyNames);

        T anyOfValues(final Collection<?> values);

        T anyOfValues(final Object... values);

        T anyOfParams(final CharSequence... paramNames);

        T anyOfParams(final Collection<? extends CharSequence> paramNames);

        /**
         * Shortcut for the group of OR-ed iParam(..) calls.
         *
         * @return
         */
        T anyOfIParams(final Collection<? extends CharSequence> paramNames);

        T anyOfIParams(final CharSequence... paramNames);

        T anyOfModels(final Collection<? extends PrimitiveResultQueryModel> models);

        T anyOfModels(final PrimitiveResultQueryModel... models);

        T anyOfExpressions(final Collection<? extends ExpressionModel> Expressions);

        T anyOfExpressions(final ExpressionModel... Expressions);

        T allOfProps(final CharSequence... propertyNames);

        T allOfProps(final Collection<? extends CharSequence> propertyNames);

        T allOfValues(final Collection<?> values);

        T allOfValues(final Object... values);

        T allOfParams(final CharSequence... paramNames);

        T allOfParams(final Collection<? extends CharSequence> paramNames);

        /**
         * Shortcut for the group of AND-ed iParam(..) calls.
         *
         * @return
         */
        T allOfIParams(final Collection<? extends CharSequence> paramNames);

        T allOfIParams(final CharSequence... paramNames);

        T allOfModels(final Collection<? extends PrimitiveResultQueryModel> models);

        T allOfModels(final PrimitiveResultQueryModel... models);

        T allOfExpressions(final Collection<? extends ExpressionModel> expressions);

        T allOfExpressions(final ExpressionModel... expressions);

    }

    interface IComparisonOperand<T, ET extends AbstractEntity<?>> //
            extends IMultipleOperand<T, ET>, //
            /*    */IBeginExpression<IExprOperand0<T, ET>> /*
                                                             * another entry
                                                             * point
                                                             */ {
    }

    interface ISingleConditionOperator<T extends ILogicalOperator<?>> {
        T exists(final QueryModel<?> subQuery);

        T notExists(final QueryModel<?> subQuery);

        T existsAnyOf(final Collection<? extends QueryModel<?>> subQueries);

        T existsAnyOf(final QueryModel<?>... subQueries);

        T notExistsAnyOf(final Collection<? extends QueryModel<?>> subQueries);

        T notExistsAnyOf(final QueryModel<?>... subQueries);

        T existsAllOf(final Collection<? extends QueryModel<?>> subQueries);

        T existsAllOf(final QueryModel<?>... subQueries);

        T notExistsAllOf(final Collection<? extends QueryModel<?>> subQueries);

        T notExistsAllOf(final QueryModel<?>... subQueries);

        /**
         * Applies value of crit-only property {@code critPropName} (including mnemonics) to persistent property {@code propName} and generates appropriate condition model (as per
         * {@link ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder#buildAtomicCondition(ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty, String)}
         * logic).
         *
         * @param propName
         * @param critPropName
         * @return
         */
        T critCondition(final CharSequence propName, final CharSequence critPropName);

        /**
         * Applies value of crit-only property {@code critPropName} (including mnemonics) to persistent collectional property {@code propName} represented by collection in
         * {@code collectionQueryStart} and enhances this query with generated appropriate condition model (as per {@link ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder#buildAtomicCondition(ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty, String)}
         * logic).
         * <p>
         * Rules for applying mnemonics and search values onto collectional properties have been specified as follows, where {@code v} -- value, {@code n} -- negation,
         * {@code m} -- indicates presence of mnemonics. For more information refer <a href="https://github.com/fieldenms/tg/issues/947">issue 947</a>.
         *
         * <pre>
         * v n m
         * + + +  not (exists collectional element that matches any of the values || empty) == there are no collectional elements that match any of values && not empty
         * + + -  not (exists collectional element that matches any of the values && not empty) == there are no collectional elements that match any of values || empty
         * + - +  exists collectional element that matches any of the values || empty
         * + - -  exists collectional element that matches any of the values && not empty
         * - + +  not empty
         * - + -  no condition
         * - - +  empty
         * - - -  no condition
         * </pre>
         *
         * @param collectionQueryStart
         * @param propName
         * @param critPropName
         * @return
         */
        T critCondition(final ICompoundCondition0<?> collectionQueryStart, final CharSequence propName, final CharSequence critPropName);

        /**
         * The same as {@link #critCondition(ICompoundCondition0, CharSequence, CharSequence)}, but with a default value for the criterion.
         * This value would be used only if the criterion is empty (i.e., no value and no mnemonic).
         * <p>
         * There are 3 primary types that get recognised for {@code defaultValue}:
         * <ul>
         * <li> {@code List} - used to pass a list of strings, suitable for selection criteria associated with entity-typed properties.
         * <li> {@code String} - used to pass a string, suitable for selection criteria associated with String-typed properties.
         * <li> {@code T2} - a pair of values, suitable for representing default values for boolean, numeric, and date properties.
         * </ul>
         * @param collectionQueryStart
         * @param propName
         * @param critPropName
         * @param defaultValue
         * @return
         */
        T critCondition(final ICompoundCondition0<?> collectionQueryStart, final CharSequence propName, final CharSequence critPropName, final Object defaultValue);

        T condition(final ConditionModel condition);

        T negatedCondition(final ConditionModel condition);
    }

    interface IQuantifiedOperand<T, ET extends AbstractEntity<?>> //
            extends IMultipleOperand<T, ET> {
        T all(final SingleResultQueryModel<?> subQuery);

        T any(final SingleResultQueryModel<?> subQuery);
    }

    interface IComparisonQuantifiedOperand<T, ET extends AbstractEntity<?>> //
            extends IQuantifiedOperand<T, ET>, //
            /*    */IBeginExpression<IExprOperand0<T, ET>> /*
                                                             * another entry
                                                             * point
                                                             */ {
    }

    interface IComparisonSetOperand<T> {
        T values(final Collection<?> values);

        <E extends Object> T values(final E... values);

        T props(final CharSequence... properties);

        T props(final Collection<? extends CharSequence> properties);

        T params(final CharSequence... paramNames);

        T params(final Collection<? extends CharSequence> paramNames);

        T iParams(final CharSequence... paramNames);

        T iParams(final Collection<? extends CharSequence> paramNames);

        T model(final SingleResultQueryModel<?> model);
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
    }

    interface IDateDiffFunction<T, ET extends AbstractEntity<?>> {
        /**
         * Indicates difference between more recent date and earlier date.
         *
         * @return
         */
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

    interface IDateAddIntervalFunctionTo<T, ET extends AbstractEntity<?>> {
        /**
         * A junction to be used for specifying where to the time interval should be added.
         * Whatever follows ({@code val}, {@code prop}, {@code model} or an expression) must be computable to a value of type {@link Date}.
         *
         * @return
         */
        IFunctionLastArgument<T, ET> to();
    }

    interface ICaseWhenFunction<T, ET extends AbstractEntity<?>> {
        ICaseWhenFunctionArgument<T, ET> then();
    }

    interface IDateDiffFunctionBetween<T, ET extends AbstractEntity<?>> {
        IFunctionLastArgument<T, ET> and();
    }

    interface ICaseWhenFunctionEnd<T> {
        T end();

        T endAsInt();

        T endAsBool();

        T endAsStr(final int length);

        T endAsDecimal(final int presicion, final int scale);
    }

    interface ICaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> //
            extends ICaseWhenFunctionEnd<T> {
        ICaseWhenFunctionLastArgument<T, ET> otherwise();
    }

    interface ICaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> //
            extends ICaseWhenFunctionElseEnd<T, ET> {
        IFunctionWhere0<T, ET> when();
    }

    /**
     * A contract to specify the units of measure for time interval operations.
     *
     * @param <T>
     * @param <ET>
     */
    interface IDateAddIntervalUnit<T, ET extends AbstractEntity<?>> {
        IDateAddIntervalFunctionTo<T, ET> seconds();

        IDateAddIntervalFunctionTo<T, ET> minutes();

        IDateAddIntervalFunctionTo<T, ET> hours();

        IDateAddIntervalFunctionTo<T, ET> days();

        IDateAddIntervalFunctionTo<T, ET> months();

        IDateAddIntervalFunctionTo<T, ET> years();
    }

    interface IIfNullFunctionThen<T, ET extends AbstractEntity<?>> {
        IFunctionLastArgument<T, ET> then();
    }

    interface IConcatFunctionWith<T, ET extends AbstractEntity<?>> {
        IConcatFunctionArgument<T, ET> with();

        T end();
    }

    interface IRoundFunctionTo<T> {
        T to(final Integer precision);
    }

    interface IFirstYieldedItemAlias<T> {
        T as(final CharSequence alias);

        T as(final Enum<?> alias);

        T asRequired(final CharSequence alias);

        T asRequired(final Enum<?> alias);

        <E extends AbstractEntity<?>> EntityResultQueryModel<E> modelAsEntity(final Class<E> entityType);

        PrimitiveResultQueryModel modelAsPrimitive();
    }

    interface ISubsequentYieldedItemAlias<T> /* extends ICompletedAndYielded */ {
        T as(final CharSequence alias);

        T as(final Enum<?> alias);

        T asRequired(final CharSequence alias);

        T asRequired(final Enum<?> alias);
    }

    interface IArithmeticalOperator<T> {
        T add();

        T sub();

        T mult();

        T div();

        T mod();
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
        IJoinCondition<ET> as(final CharSequence alias);
    }

    interface IFromAlias<ET extends AbstractEntity<?>> //
            extends IJoin<ET> {
        IJoin<ET> as(final CharSequence alias);
    }

    interface IFromNone<ET extends AbstractEntity<?>> //
            extends ICompleted<ET> {
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

        IOrderingItem1<ET> orderBy();
    }

    public interface ICompletedAndYielded<ET extends AbstractEntity<?>> //
            extends ICompletedCommon<ET> {
        IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield();

        ISubsequentCompletedAndYielded<ET> yieldAll();

        //////////////////// RETURN /////////////////////////
        EntityResultQueryModel<ET> model();
    }

    public interface ISubsequentCompletedAndYielded<ET extends AbstractEntity<?>> //
            extends ICompletedCommon<ET> {
        IFunctionYieldedLastArgument<ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield();
    }

    public interface ICompletedCommon<ET extends AbstractEntity<?>> {
        //////////////////// RETURN /////////////////////////
        <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType);

        AggregatedResultQueryModel modelAsAggregate();
    }

    // interface IWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends
    // ILogicalOperator<?>, T3, ET extends AbstractEntity<?>> //
    // extends IComparisonOperand<T1, ET>, //
    // /* */IExistenceOperator<T2>, //
    // /* */IBeginCondition<T3> {
    // T2 condition(ConditionModel condition);
    // }
    interface IWhere<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, T3, ET extends AbstractEntity<?>> //
            extends IWhereWithoutNesting<T1, T2, ET>, //
            /*    */IBeginCondition<T3> {
    }

    interface IWhereWithoutNesting<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
            extends IComparisonOperand<T1, ET>, //
            /*    */ISingleConditionOperator<T2> {
    }

    interface ICompoundCondition<T1, T2> //
            extends ILogicalOperator<T1>, //
            /*    */IEndCondition<T2> {
    }

    ////////////////////////////////////////////////////////// ---JOIN
    ////////////////////////////////////////////////////////// ---//////////////////////////////////////////////////////////
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
            /*    */ISingleConditionOperator<IJoinCompoundCondition3<ET>> {
    }

    // -------------------------------------------
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

    // -------------------------------------------
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

    ////////////////////////////////////////////////////////// ---WHERE
    ////////////////////////////////////////////////////////// ---/////////////////////////////////////////////////////////
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

    // -------------------------------------------
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

    // -------------------------------------------
    interface IWhere0<ET extends AbstractEntity<?>> //
            extends IWhere<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> /*
                                                                                                 * Exp
                                                                                                 * entry
                                                                                                 * point
                                                                                                 */ {
    }

    interface IWhere1<ET extends AbstractEntity<?>> //
            extends IWhere<IComparisonOperator1<ET>, ICompoundCondition1<ET>, IWhere2<ET>, ET> {
    }

    interface IWhere2<ET extends AbstractEntity<?>> //
            extends IWhere<IComparisonOperator2<ET>, ICompoundCondition2<ET>, IWhere3<ET>, ET> {
    }

    interface IWhere3<ET extends AbstractEntity<?>> //
            extends IComparisonOperand<IComparisonOperator3<ET>, ET>, //
            /*    */ISingleConditionOperator<ICompoundCondition3<ET>> {
    }

    ////////////////////////////////////////////////////////// ---FUNCTION
    ////////////////////////////////////////////////////////// ---/////////////////////////////////////////////////////////
    interface IFunctionWhere0<T, ET extends AbstractEntity<?>> //
            extends
            IWhere<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> /*
                                                                                                                         * Exp
                                                                                                                         * entry
                                                                                                                         * point
                                                                                                                         */ {
    }

    interface IFunctionWhere1<T, ET extends AbstractEntity<?>> //
            extends
            IWhere<IFunctionComparisonOperator1<T, ET>, IFunctionCompoundCondition1<T, ET>, IFunctionWhere2<T, ET>, ET> {
    }

    interface IFunctionWhere2<T, ET extends AbstractEntity<?>> //
            extends
            IWhere<IFunctionComparisonOperator2<T, ET>, IFunctionCompoundCondition2<T, ET>, IFunctionWhere3<T, ET>, ET> {
    }

    interface IFunctionWhere3<T, ET extends AbstractEntity<?>> //
            extends IComparisonOperand<IFunctionComparisonOperator3<T, ET>, ET>, //
            /*    */ISingleConditionOperator<IFunctionCompoundCondition3<T, ET>> {
    }

    // -------------------------------------------
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

    // -------------------------------------------
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

    /////////////////////////////////////////////// ---EXPRESSION (WHERE, GROUP,
    /////////////////////////////////////////////// ORDER)
    /////////////////////////////////////////////// ---///////////////////////////////////////
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

    // ----------------------------------------------------------------------------------------------------------------------------------------------
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

    ///////////////////////////////////////////////////// ---EXPRESSION (YIELD)
    ///////////////////////////////////////////////////// ---///////////////////////////////////////////////////
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

    // ----------------------------------------------------------------------------------------------------------------------------------------------
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

    ///////////////////////////////////////////////////// ---OTHERS
    ///////////////////////////////////////////////////// ---///////////////////////////////////////////////////
    interface IDateDiffFunctionArgument<T, ET extends AbstractEntity<?>> //
            extends
            IExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> {
    }

    interface ICaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> //
            extends IExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> {
    }

    interface ICaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> //
            extends IExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> {
    }

    interface IDateAddIntervalFunctionArgument<T, ET extends AbstractEntity<?>> //
            extends IExprOperand<IDateAddIntervalUnit<T, ET>, IExprOperand0<IDateAddIntervalUnit<T, ET>, ET>, ET> {
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
        // ISubsequentCompletedAndYielded<ET> all();
    }

    interface IConcatFunctionArgument<T, ET extends AbstractEntity<?>> //
            extends IExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> {
    }

    ///////////////////////////////////////////////////// ---STAND-ALONE
    ///////////////////////////////////////////////////// EXPRESSION
    ///////////////////////////////////////////////////// ---///////////////////////////////////////////////////
    interface IStandAloneExprOperationAndClose //
            extends IArithmeticalOperator<IStandAloneExprOperand> {
        ExpressionModel model();
    }

    interface IStandAloneExprOperand //
            extends IYieldOperand<IStandAloneExprOperationAndClose, AbstractEntity<?>> {
    }

    ///////////////////////////////////////////////////// ---STAND-ALONE
    ///////////////////////////////////////////////////// CONDITION
    ///////////////////////////////////////////////////// ---///////////////////////////////////////////////////
    interface IStandAloneConditionOperand<ET extends AbstractEntity<?>> //
            extends
            IWhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET> {
    }

    interface IStandAloneConditionCompoundCondition<ET extends AbstractEntity<?>> //
            extends ILogicalOperator<IStandAloneConditionOperand<ET>> {
        ConditionModel model();
    }

    interface IStandAloneConditionComparisonOperator<ET extends AbstractEntity<?>> //
            extends IComparisonOperator<IStandAloneConditionCompoundCondition<ET>, ET> {
    }

    ///////////////////////////////////////////////////// ---ORDER BY
    ///////////////////////////////////////////////////// ---///////////////////////////////////////////////////
//	interface IOrder<T> {
//		T asc();
//
//		T desc();
//	}
//
//	interface ISingleOperandOrderable //
//			extends IOrder<IOrderingItemCloseable> {
//	}

    /**
     * Sort order of an ordering item.
     * <p>
     * Continuation: {@link IOrderingItem}
     */
    interface ISingleOperandOrderable<ET extends AbstractEntity<?>>  {
        IOrderingItem<ET> asc();
        IOrderingItem<ET> desc();
    }

    /**
     * Mandatory ordering item.
     *
     * @param <ET>  entity type
     */
    interface IOrderingItem1<ET extends AbstractEntity<?>>
            extends
            IExprOperand<ISingleOperandOrderable<ET>, IExprOperand0<ISingleOperandOrderable<ET>, ET>, ET>
    {
        ISingleOperandOrderable<ET> yield(final CharSequence yieldAlias);

        /**
         * Include the given ordering model into this one.
         */
        IOrderingItem<ET> order(final OrderingModel model);
    }

    /**
     * Subsequent ordering item (after the first one) or the end of this order model.
     *
     * @param <ET>  entity type
     */
    interface IOrderingItem<ET extends AbstractEntity<?>>
            extends
            IOrderingItem1<ET>,
            ICompletedAndYielded<ET>,
            IOrderByLimit<ET>,
            IOrderByOffset<ET>
    {}

    /**
     * Limit or the end of this order model.
     */
    interface IOrderByLimit<ET extends AbstractEntity<?>> extends ICompletedAndYielded<ET> {

        /**
         * Limits the number of retrieved entities to the given number.
         *
         * @param n  number that is greater than zero
         */
        IOrderByOffset<ET> limit(long n);

        /**
         * Limits the number of retrieved entities to the given limit.
         *
         * @param limit  limit that is greater than zero
         */
        IOrderByOffset<ET> limit(Limit limit);
    }

    /**
     * Offset or the end of this order model.
     */
    interface IOrderByOffset<ET extends AbstractEntity<?>> extends ICompletedAndYielded<ET> {

        /**
         * Skips the specified number of entities from the beginning of a result set.
         *
         * @param n  non-negative number (>= 0)
         */
        ICompletedAndYielded<ET> offset(long n);

    }

    interface StandaloneOrderBy {

        interface ISingleOperandOrderable  {
            IOrderingItemCloseable asc();
            IOrderingItemCloseable desc();
        }

        interface IOrderByEnd {
            OrderingModel model();
        }

        interface IOrderingItemCloseable
                extends
                IOrderingItem,
                IOrderByEnd,
                IOrderByLimit,
                IOrderByOffset
        {}

        interface IOrderingItem
                extends
                IExprOperand<ISingleOperandOrderable, IExprOperand0<ISingleOperandOrderable, AbstractEntity<?>>, AbstractEntity<?>> {
            ISingleOperandOrderable yield(final CharSequence yieldAlias);
            IOrderingItemCloseable order(final OrderingModel model);
        }

        /**
         * Limit or the end of this order model.
         */
        interface IOrderByLimit extends IOrderByEnd {

            /**
             * Limits the number of retrieved entities to the given number.
             *
             * @param n  number that is greater than zero
             */
            IOrderByOffset limit(long n);

            /**
             * Limits the number of retrieved entities to the given limit.
             *
             * @param limit  limit that is greater than zero
             */
            IOrderByOffset limit(Limit limit);
            
        }

        /**
         * Offset or the end of this order model.
         */
        interface IOrderByOffset extends IOrderByEnd {

            /**
             * Skips the specified number of entities from the beginning of a result set.
             *
             * @param n  non-negative number (>= 0)
             */
            IOrderByEnd offset(long n);

        }

    }

}
