package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.Expression;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneExpressionBuilder extends AbstractTokensBuilder {

    public StandAloneExpressionBuilder(final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final ExpressionModel exprModel) {
	super(null, queryBuilder, paramValues);

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

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}

	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
	final ISingleOperand firstOperand = getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue());
	final List<CompoundSingleOperand> items = new ArrayList<CompoundSingleOperand>();
	for (; iterator.hasNext();) {
	    final ArithmeticalOperator operator = (ArithmeticalOperator) iterator.next().getValue();
	    final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
	    final ISingleOperand subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());

	    items.add(new CompoundSingleOperand(subsequentOperand, operator));
	}

	return new Pair<TokenCategory, Object>(TokenCategory.EXPR, new Expression(firstOperand, items));    }
}
