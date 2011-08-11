package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractCloseExp;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractCloseGroup;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractExpOperation;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractLeftSideSubject;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractLogicalCondition;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractOpenExp;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractOpenGroup;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractRightSideSubject;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractSearchCondition;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractWhere;
import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractYieldedLeftSideSubject;
import ua.com.fielden.platform.equery.interfaces.IAbstract.ICaseWhenFunction;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;

/**
 * An interface for grouping of other interfaces, which are part of <em>EnityQuery fluent interface</em> implementation.
 *
 * @author nc
 *
 */
public interface IOthers {

    interface IJoinSearchCondition extends IAbstractSearchCondition<IJoinCompoundCondition, IExpRightArgument<IJoinCompoundCondition>> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IAbstractSearchConditionAtGroup<T> extends IAbstractSearchCondition<T, IExpRightArgument<T>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface ISearchCondition extends IAbstractSearchCondition<ICompoundCondition, IExpRightArgument<ICompoundCondition>> {
    }

    interface ISearchConditionAtGroup1 extends IAbstractSearchConditionAtGroup<ICompoundConditionAtGroup1> {
    }

    interface ISearchConditionAtGroup2 extends IAbstractSearchConditionAtGroup<ICompoundConditionAtGroup2> {
    }

    interface ISearchConditionAtGroup3 extends IAbstractSearchConditionAtGroup<ICompoundConditionAtGroup3> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IAbstractCompoundConditionAtGroup<T1, T2> extends IAbstractLogicalCondition<T1>, IAbstractCloseGroup<T2> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IJoinCompoundCondition extends IAbstractLogicalCondition<IJoinWhere>, IJoin {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface ICompoundCondition extends IAbstractLogicalCondition<IWhere>, ICompleted {
    }

    interface ICompoundConditionAtGroup1 extends IAbstractCompoundConditionAtGroup<IWhereAtGroup1, ICompoundCondition> {
    }

    interface ICompoundConditionAtGroup2 extends IAbstractCompoundConditionAtGroup<IWhereAtGroup2, ICompoundConditionAtGroup1> {
    }

    interface ICompoundConditionAtGroup3 extends IAbstractCompoundConditionAtGroup<IWhereAtGroup3, ICompoundConditionAtGroup2> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IJoinWhere extends IAbstractWhere<IJoinSearchCondition, IJoinCompoundCondition>, IAbstractOpenExp<IExpArgument<IJoinSearchCondition>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IWhere extends IAbstractWhere<ISearchCondition, ICompoundCondition>, IAbstractOpenGroup<IWhereAtGroup1>, IAbstractOpenExp<IExpArgument<ISearchCondition>> /*exp entry point*/{
    }

    interface IWhereAtGroup1 extends IAbstractWhere<ISearchConditionAtGroup1, ICompoundConditionAtGroup1>, IAbstractOpenGroup<IWhereAtGroup2>, IAbstractOpenExp<IExpArgument<ISearchConditionAtGroup1>> {
    }

    interface IWhereAtGroup2 extends IAbstractWhere<ISearchConditionAtGroup2, ICompoundConditionAtGroup2>, IAbstractOpenGroup<IWhereAtGroup3>, IAbstractOpenExp<IExpArgument<ISearchConditionAtGroup2>> {
    }

    interface IWhereAtGroup3 extends IAbstractWhere<ISearchConditionAtGroup3, ICompoundConditionAtGroup3>, IAbstractOpenExp<IExpArgument<ISearchConditionAtGroup3>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IExpRightArgument<T> extends IAbstractRightSideSubject<T>, IAbstractOpenExp<IExpArgument<T>> /*another entry point*/{
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IAbstractExpArgument<T1, T2> extends IAbstractLeftSideSubject<T1>, IAbstractOpenExp<T2> {
    }

    interface IExpArgument<T> extends IAbstractExpArgument<IExpOperationAndClose<T>, IExpArgument1<T>> {
    }

    interface IExpArgument1<T> extends IAbstractExpArgument<IExpOperationAndClose1<T>, IExpArgument2<T>> {
    }

    interface IExpArgument2<T> extends IAbstractExpArgument<IExpOperationAndClose2<T>, IExpArgument3<T>> {
    }

    interface IExpArgument3<T> extends IAbstractLeftSideSubject<IExpOperationAndClose3<T>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IAbstractExpOperationAndClose<T1, T2> extends IAbstractExpOperation<T1>, IAbstractCloseExp<T2> {
    }

    interface IExpOperationAndClose<T> extends IAbstractExpOperationAndClose<IExpArgument<T>, T> {
    }

    interface IExpOperationAndClose1<T> extends IAbstractExpOperationAndClose<IExpArgument1<T>, IExpOperationAndClose<T>> {
    }

    interface IExpOperationAndClose2<T> extends IAbstractExpOperationAndClose<IExpArgument2<T>, IExpOperationAndClose1<T>> {
    }

    interface IExpOperationAndClose3<T> extends IAbstractExpOperationAndClose<IExpArgument3<T>, IExpOperationAndClose2<T>> {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IAbstractExpYieldArgument<T1, T2> extends IAbstractYieldedLeftSideSubject<T1>, IAbstractOpenExp<T2> {
    }

    interface IExpYieldArgument<T> extends IAbstractExpYieldArgument<IExpYieldOperationAndClose<T>, IExpYieldArgument1<T>> {
    }

    interface IExpYieldArgument1<T> extends IAbstractExpYieldArgument<IExpYieldOperationAndClose1<T>, IExpYieldArgument2<T>> {
    }

    interface IExpYieldArgument2<T> extends IAbstractExpYieldArgument<IExpYieldOperationAndClose2<T>, IExpYieldArgument3<T>> {
    }

    interface IExpYieldArgument3<T> extends IAbstractYieldedLeftSideSubject<IExpYieldOperationAndClose3<T>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IExpYieldOperationAndClose<T> extends IAbstractExpOperationAndClose<IExpYieldArgument<T>, T> {
    }

    interface IExpYieldOperationAndClose1<T> extends IAbstractExpOperationAndClose<IExpYieldArgument1<T>, IExpYieldOperationAndClose<T>> {
    }

    interface IExpYieldOperationAndClose2<T> extends IAbstractExpOperationAndClose<IExpYieldArgument2<T>, IExpYieldOperationAndClose1<T>> {
    }

    interface IExpYieldOperationAndClose3<T> extends IAbstractExpOperationAndClose<IExpYieldArgument3<T>, IExpYieldOperationAndClose2<T>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------
    interface IFunctionWhere<T> extends IAbstractWhere<IFunctionSearchCondition<T>, IFunctionCompoundCondition<T>>, IAbstractOpenGroup<IFunctionWhereAtGroup1<T>>, IAbstractOpenExp<IExpArgument<IFunctionSearchCondition<T>>> /*exp entry point*/{
    }

    interface IFunctionWhereAtGroup1<T> extends IAbstractWhere<IFunctionSearchConditionAtGroup1<T>, IFunctionCompoundConditionAtGroup1<T>>, IAbstractOpenGroup<IFunctionWhereAtGroup2<T>>, IAbstractOpenExp<IExpArgument<IFunctionSearchConditionAtGroup1<T>>> {
    }

    interface IFunctionWhereAtGroup2<T> extends IAbstractWhere<IFunctionSearchConditionAtGroup2<T>, IFunctionCompoundConditionAtGroup2<T>>, IAbstractOpenGroup<IFunctionWhereAtGroup3<T>>, IAbstractOpenExp<IExpArgument<IFunctionSearchConditionAtGroup2<T>>> {
    }

    interface IFunctionWhereAtGroup3<T> extends IAbstractWhere<IFunctionSearchConditionAtGroup3<T>, IFunctionCompoundConditionAtGroup3<T>>, IAbstractOpenExp<IExpArgument<IFunctionSearchConditionAtGroup3<T>>> {
    }

    interface IFunctionCompoundCondition<T> extends IAbstractLogicalCondition<IFunctionWhere<T>>, ICaseWhenFunction<T> {
    }

    interface IFunctionCompoundConditionAtGroup1<T> extends IAbstractCompoundConditionAtGroup<IFunctionWhereAtGroup1<T>, IFunctionCompoundCondition<T>> {
    }

    interface IFunctionCompoundConditionAtGroup2<T> extends IAbstractCompoundConditionAtGroup<IFunctionWhereAtGroup2<T>, IFunctionCompoundConditionAtGroup1<T>> {
    }

    interface IFunctionCompoundConditionAtGroup3<T> extends IAbstractCompoundConditionAtGroup<IFunctionWhereAtGroup3<T>, IFunctionCompoundConditionAtGroup2<T>> {
    }


    interface IFunctionSearchCondition<T> extends IAbstractSearchCondition<IFunctionCompoundCondition<T>, IExpRightArgument<IFunctionCompoundCondition<T>>> {
    }

    interface IFunctionSearchConditionAtGroup1<T> extends IAbstractSearchConditionAtGroup<IFunctionCompoundConditionAtGroup1<T>> {
    }

    interface IFunctionSearchConditionAtGroup2<T> extends IAbstractSearchConditionAtGroup<IFunctionCompoundConditionAtGroup2<T>> {
    }

    interface IFunctionSearchConditionAtGroup3<T> extends IAbstractSearchConditionAtGroup<IFunctionCompoundConditionAtGroup3<T>> {
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------------------------




}
