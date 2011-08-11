package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IExpressionAlias;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IFunctionLastArgument;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.IJoinWhere;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;

public interface IMain {

    /**
     * Interface that forms part of Entity Query API needed specifically to support type checking and proper auto-completion for chaining of API methods. This interface caters for
     * different kind of <em>JOIN</em> clauses in the query.
     *
     * @author nc
     * @author 01es
     *
     */
    public interface IJoin extends IPlainJoin {

	<T extends AbstractEntity> IJoinCondition join(final Class<T> entityType, String alias);

	<T extends AbstractEntity> IJoinCondition leftJoin(final Class<T> entityType, String alias);

	<T extends AbstractEntity> IJoinCondition join(final IQueryModel model, String alias);

	<T extends AbstractEntity> IJoinCondition leftJoin(final IQueryModel model, String alias);

    }

    public interface IPlainJoin extends ICompleted {
	/**
	 * Composes the start of the <em>WHERE</em> clause.
	 *
	 * @return
	 */
	IWhere where();
    }

    public interface IJoinCondition {
	IJoinWhere on();
    }

    /**
     * Interface that forms part of Entity Query API needed specifically to support type checking and proper auto-completion for chaining of API methods.
     *
     *
     * @author nc
     * @author 01es
     *
     */
    public interface ICompletedAndOrdered {
	<T extends AbstractEntity> IQueryOrderedModel<T> model();

	<T extends AbstractEntity> IQueryOrderedModel<T> model(Class<T> resultType);

	ICompletedAndOrdered orderBy(String... otherFields);
    }

    /**
     * Interface that forms part of Entity Query API needed specifically to support type checking and proper auto-completion for chaining of API methods.
     *
     *
     * @author nc
     * @author 01es
     *
     */
    public interface ICompleted extends ICompletedAndYielded {
	//////////////////// GROUPING /////////////////////////
	ICompleted groupByProp(String property); // TODO remove

	ICompleted groupByExp(String expression, final Object...values); // TODO remove

	//----------------------------------------------------------------------------------------
	IFunctionLastArgument<ICompleted> groupBy(); // TODO implement

	//////////////////// RETURN /////////////////////////

	<E extends AbstractEntity> ICompleted resultType(final Class<E> resultType);
    }

    public interface ICompletedAndYielded extends ICompletedAndOrdered {

	//////////////////// YIELDING /////////////////////////

	ICompletedAndYielded yieldExp(String propertyExpression, String alias, final Object...values); // TODO remove

	ICompletedAndYielded yieldProp(String propertyExpression, String alias); // TODO remove

	ICompletedAndYielded yieldProp(String propertyExpression); // TODO remove

	ICompletedAndYielded yieldValue(Object value, String alias); // TODO remove

	<E extends AbstractEntity> ICompletedAndYielded yieldModel(IQueryModel<? extends E> subModel, String alias); // TODO remove

	//----------------------------------------------------------------------------------------
	IFunctionYieldedLastArgument<IExpressionAlias<ICompletedAndYielded>> yield(); // TODO implement

	//////////////////// RETURN /////////////////////////

	<T extends AbstractEntity> IQueryModel<T> model();

	<T extends AbstractEntity> IQueryModel<T> model(Class<T> resultType);
    }
}
