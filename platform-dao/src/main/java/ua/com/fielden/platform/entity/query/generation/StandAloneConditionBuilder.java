package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditions;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneConditionBuilder extends AbstractTokensBuilder {

    public StandAloneConditionBuilder(final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final ConditionModel exprModel) {
	super(null, queryBuilder, paramValues);
	setChild(new ConditionBuilder(this, queryBuilder, paramValues));

	for (final Pair<TokenCategory, Object> tokenPair : exprModel.getTokens()) {
	    add(tokenPair.getKey(), tokenPair.getValue());
	}

    }

    @Override
    public boolean isClosing() {
	return false;
    }

    @Override
    public boolean canBeClosed() {
	return getChild() == null;
    }


    public GroupedConditions getModel() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}

//	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
//	final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
//	final ISingleOperand firstOperand = getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue());
//	final List<CompoundSingleOperand> items = new ArrayList<CompoundSingleOperand>();
//	for (; iterator.hasNext();) {
//	    final ArithmeticalOperator operator = (ArithmeticalOperator) iterator.next().getValue();
//	    final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
//	    final ISingleOperand subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());
//
//	    items.add(new CompoundSingleOperand(subsequentOperand, operator));
//	}
//
//	return new Pair<TokenCategory, Object>(TokenCategory.EXPR, new Expression(firstOperand, items));    }
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ICondition firstCondition = (ICondition) iterator.next().getValue();
	final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();
	for (; iterator.hasNext();) {
	    final CompoundCondition subsequentCompoundCondition = (CompoundCondition) iterator.next().getValue();
	    otherConditions.add(subsequentCompoundCondition);
	}
	return new GroupedConditions(false, firstCondition, otherConditions);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }

}
