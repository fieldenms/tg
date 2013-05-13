package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CaseWhen;
import ua.com.fielden.platform.eql.s1.elements.ICondition;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand;
import ua.com.fielden.platform.utils.Pair;

public class CaseFunctionBuilder extends AbstractTokensBuilder {

    protected CaseFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
	setChild(new ConditionBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case COND_START: //eats token
	    setChild(new ConditionBuilder(this, getQueryBuilder(), getParamValues()));
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

    public CaseWhen getModel() {
	if (TokenCategory.END_FUNCTION.equals(getLastCat())) {
	    getTokens().remove(getSize() - 1);
	}

	final List<Pair<ICondition, ISingleOperand>> whenThens = new ArrayList<Pair<ICondition, ISingleOperand>>();
	ISingleOperand elseOperand = null;

	for (final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator(); iterator.hasNext();) {
	    final Pair<TokenCategory, Object> firstTokenPair = iterator.next();
	    final Pair<TokenCategory, Object> secondTokenPair = iterator.hasNext() ? iterator.next() : null;

	    if (secondTokenPair != null) {
		whenThens.add(new Pair<ICondition, ISingleOperand>((ICondition) firstTokenPair.getValue(), getModelForSingleOperand(secondTokenPair)));
	    } else {
		elseOperand = getModelForSingleOperand(firstTokenPair);
	    }
	}

	return new CaseWhen(whenThens, elseOperand);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, getModel());
    }
}