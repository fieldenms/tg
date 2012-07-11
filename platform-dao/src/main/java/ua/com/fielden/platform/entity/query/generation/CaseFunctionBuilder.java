package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CaseWhen;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.utils.Pair;

public class CaseFunctionBuilder extends AbstractTokensBuilder {

    protected CaseFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
	setChild(new ConditionBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public boolean isClosing() {
	return TokenCategory.END_FUNCTION.equals(getLastCat());
	//return getSize() == 2;
    }

    @Override
    public boolean canBeClosed() {
	return getChild() == null;
    }

    public CaseWhen getModel() {
//	if (getChild() != null) {
//	    throw new RuntimeException("Unable to produce result - unfinished model state!");
//	}
//	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
//	final ICondition firstCondition = (ICondition) iterator.next().getValue();
//	final List<CompoundConditionModel> otherConditions = new ArrayList<CompoundConditionModel>();
//	for (; iterator.hasNext();) {
//	    final CompoundConditionModel subsequentCompoundCondition = (CompoundConditionModel) iterator.next().getValue();
//	    otherConditions.add(subsequentCompoundCondition);
//	}
	final List<Pair<ICondition, ISingleOperand>> whenThens = new ArrayList<Pair<ICondition, ISingleOperand>>();
	whenThens.add(new Pair<ICondition, ISingleOperand>((ICondition) firstValue(), (ISingleOperand) secondValue()));

	return new CaseWhen(whenThens, null);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, getModel());
    }
}