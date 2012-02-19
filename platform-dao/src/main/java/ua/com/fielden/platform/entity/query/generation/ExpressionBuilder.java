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
import ua.com.fielden.platform.utils.Pair;

public class ExpressionBuilder extends AbstractTokensBuilder {

    protected ExpressionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return TokenCategory.END_EXPR.equals(getLastCat());
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (TokenCategory.END_EXPR.equals(getLastCat())) {
	    getTokens().remove(getSize() - 1);
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

	return new Pair<TokenCategory, Object>(TokenCategory.EXPR, new Expression(firstOperand, items));
    }
}
