package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;

public class StandAloneConditionComparisonOperator
//extends AbstractConditionalOperand<IComparisonOperand<IStandAloneConditionComparisonOperator<>, ET>, IExistenceOperator<IStandAloneConditionCompoundCondition>, ET> //
extends AbstractQueryLink
implements IStandAloneConditionComparisonOperator {
    protected StandAloneConditionComparisonOperator(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IStandAloneConditionCompoundCondition isNull() {
	return new StandAloneConditionCompoundCondition(getTokens().isNull(false));
    }

    @Override
    public IStandAloneConditionCompoundCondition isNotNull() {
	return new StandAloneConditionCompoundCondition(getTokens().isNull(true));
    }

    @Override
    public IComparisonSetOperand<IStandAloneConditionCompoundCondition> in() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonSetOperand<IStandAloneConditionCompoundCondition> notIn() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> like() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> iLike() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> notLike() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> notILike() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> eq() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> ne() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> gt() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> lt() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> ge() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IComparisonQuantifiedOperand<IStandAloneConditionCompoundCondition, AbstractEntity<?>> le() {
	// TODO Auto-generated method stub
	return null;
    }
}