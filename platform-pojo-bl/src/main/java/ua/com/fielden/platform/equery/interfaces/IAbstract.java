package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpYieldArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IFunctionWhere;

/**
 * Group of the abstract interfaces, which are used for implementation of those EntityQuery interfaces that have to support nested groups.
 *
 * @author nc
 *
 */
public interface IAbstract {

    interface IAbstractSearchCondition<T1, T2> {

	T2 eq();

	T2 ne();

	T2 gt();

	// T3 gtAnyOf

	// T3 gtAllOf

	T2 lt();

	T2 ge();

	T2 le();

	T2 like(); //OR group

	//T3 likeAnyOf();

	T2 notLike();

	T2 in(); //OR group only for models

	//T3 inAnyOf()

	T2 notIn();

	T1 between(Object value1, Object value2);

	T1 isNull();

	T1 isNotNull();

	T1 isTrue();

	T1 isFalse();
    }

    interface IAbstractOpenGroup<T> {
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

    interface IAbstractLogicalCondition<T> {
	T and();

	T or();
    }

    interface IAbstractCloseGroup<T> {
	/**
	 * 'Stops' the nearest group by adding its closing parenthesis. It is supposed to be the group with a nested level of 1.
	 *
	 * @return
	 */
	T end();
    }

    interface IAbstractWhere<T1 extends IAbstractSearchCondition, T2> extends IAbstractLeftSideSubject<T1> {
	/**
	 * Composes <em>EXISTS</em> condition of the query.
	 *
	 * @param subQuery
	 * @return
	 */
	<E extends AbstractEntity> T2 exists(IQueryModel<E> subQuery);

	/**
	 * Composes <em>NOT EXISTS</em> condition of the query.
	 *
	 * @param subQuery
	 * @return
	 */
	<E extends AbstractEntity> T2 notExists(IQueryModel<E> subQuery);
    }

    interface IAbstractLeftSideSubject<T> {
	T exp(String expression, Object... values);
	T val(Object value);
	<E extends AbstractEntity> T model(IQueryModel<E> model);
	T param(String paramName);
	T prop(String propertyName);

	// built-in SQL functions
	T now();
	IDateDiffFunction<T> countDays();
	IFunctionLastArgument<T> upperCase();
	IIfNullFunctionArgument<T> ifNull();
	IFunctionWhere<T> caseWhen();
	IRoundFunctionArgument<T> round();
    }

    interface IAbstractYieldedLeftSideSubject<T> extends IAbstractLeftSideSubject<T> {
	IFunctionLastArgument<T> maxOf();
	IFunctionLastArgument<T> minOf();
	IFunctionLastArgument<T> sumOf();
	IFunctionLastArgument<T> averageOf();
    }

    interface IAbstractRightSideSubject<T> extends IAbstractLeftSideSubject<T> {
	<E extends Object> T val(E... values);
	T model(IQueryModel... models);
    }

    interface IDateDiffFunction<T> {
	IDateDiffFunctionArgument<T> between();
    }

    interface ICaseWhenFunction<T> {
	ICaseWhenFunctionArgument<T> then();
    }

    interface IDateDiffFunctionArgument<T> extends IAbstractLeftSideSubject<IDateDiffFunctionBetween<T>>, IAbstractOpenExp<IExpArgument<IDateDiffFunctionBetween<T>>> {
    }

    interface ICaseWhenFunctionArgument<T> extends IAbstractLeftSideSubject<ICaseWhenFunctionEnd<T>>, IAbstractOpenExp<IExpArgument<ICaseWhenFunctionEnd<T>>> {
    }

    interface IIfNullFunctionArgument<T> extends IAbstractLeftSideSubject<IIfNullFunctionThen<T>>, IAbstractOpenExp<IExpArgument<IIfNullFunctionThen<T>>> {
    }


    interface IRoundFunctionArgument<T> extends IAbstractLeftSideSubject<IRoundFunctionTo<T>>, IAbstractOpenExp<IExpArgument<IRoundFunctionTo<T>>> {
    }

    interface IFunctionLastArgument<T> extends IAbstractLeftSideSubject<T>, IAbstractOpenExp<IExpArgument<T>> {
    }

    interface IFunctionYieldedLastArgument<T> extends IAbstractYieldedLeftSideSubject<T>, IAbstractOpenExp<IExpYieldArgument<T>> {
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

    interface IExpressionAlias<T> extends ICompletedAndYielded {
	T as(String alias);
    }

    interface IAbstractExpOperation<T> {
	T add();
	T subtract();
	T multiply();
	T divide();
    }

    interface IAbstractOpenExp<T> {
	T beginExp();
    }

    interface IAbstractCloseExp<T> {
	T endExp();
    }
}
