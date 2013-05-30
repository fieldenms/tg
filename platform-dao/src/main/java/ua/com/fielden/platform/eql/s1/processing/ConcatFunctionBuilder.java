package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.Concat;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class ConcatFunctionBuilder extends AbstractTokensBuilder {

    protected ConcatFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return TokenCategory.END_FUNCTION.equals(getLastCat());
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (TokenCategory.END_FUNCTION.equals(getLastCat())) {
	    getTokens().remove(getSize() - 1);
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final List<ISingleOperand<? extends ISingleOperand2>> items = new ArrayList<>();
	final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
	items.add(getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue()));

	for (; iterator.hasNext();) {
	    final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
	    final ISingleOperand subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());
	    items.add(subsequentOperand);
	}

	return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, new Concat(items));
    }
}