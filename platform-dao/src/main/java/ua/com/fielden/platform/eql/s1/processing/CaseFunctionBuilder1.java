package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CaseWhen1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class CaseFunctionBuilder1 extends AbstractTokensBuilder1 {

    protected CaseFunctionBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
	setChild(new ConditionBuilder1(this, queryBuilder, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case COND_START: //eats token
	    setChild(new ConditionBuilder1(this, getQueryBuilder(), getParamValues()));
	    break;
	default:
	    super.add(cat, value);
	    break;
	}
    }

    @Override
    public boolean isClosing() {
	return TokenCategory.END_FUNCTION.equals(getLastCat());
    }

    @Override
    public boolean canBeClosed() {
	return getChild() == null;
    }

    public CaseWhen1 getModel() {
	if (TokenCategory.END_FUNCTION.equals(getLastCat())) {
	    getTokens().remove(getSize() - 1);
	}

	final List<Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>> whenThens = new ArrayList<>();
	ISingleOperand1 elseOperand = null;

	for (final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator(); iterator.hasNext();) {
	    final Pair<TokenCategory, Object> firstTokenPair = iterator.next();
	    final Pair<TokenCategory, Object> secondTokenPair = iterator.hasNext() ? iterator.next() : null;

	    if (secondTokenPair != null) {
		whenThens.add(new Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>((ICondition1) firstTokenPair.getValue(), getModelForSingleOperand(secondTokenPair)));
	    } else {
		elseOperand = getModelForSingleOperand(firstTokenPair);
	    }
	}

	return new CaseWhen1(whenThens, elseOperand);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, getModel());
    }
}