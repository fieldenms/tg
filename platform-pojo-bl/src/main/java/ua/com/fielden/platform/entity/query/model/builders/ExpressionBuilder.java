package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.model.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.Expression;
import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class ExpressionBuilder extends AbstractTokensBuilder {

    protected ExpressionBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
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
