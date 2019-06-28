package ua.com.fielden.platform.eql.stage1.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.elements.functions.CaseWhen1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class CaseFunctionBuilder extends AbstractTokensBuilder {

    protected CaseFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
        setChild(new ConditionBuilder(this, queryBuilder));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case COND_START: //eats token
            setChild(new ConditionBuilder(this, getQueryBuilder()));
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

        final List<Pair<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThens = new ArrayList<>();
        ISingleOperand1<? extends ISingleOperand2<?>> elseOperand = null;

        for (final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator(); iterator.hasNext();) {
            final Pair<TokenCategory, Object> firstTokenPair = iterator.next();
            final Pair<TokenCategory, Object> secondTokenPair = iterator.hasNext() ? iterator.next() : null;

            if (secondTokenPair != null) {
                whenThens.add(new Pair<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>((ICondition1<?>) firstTokenPair.getValue(), getModelForSingleOperand(secondTokenPair)));
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