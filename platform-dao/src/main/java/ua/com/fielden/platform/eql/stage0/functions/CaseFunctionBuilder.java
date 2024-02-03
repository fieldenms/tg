package ua.com.fielden.platform.eql.stage0.functions;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION_MODEL;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.ConditionBuilder;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CaseWhen1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

public class CaseFunctionBuilder extends AbstractTokensBuilder {

    public CaseFunctionBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
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
        return getLastCat() == END_FUNCTION;
    }

    public CaseWhen1 getModel() {
        final Object lastValue = getLastValue();
        
        if (isClosing()) {
            getTokens().remove(getSize() - 1);
        }

        final List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThens = new ArrayList<>();
        ISingleOperand1<? extends ISingleOperand2<?>> elseOperand = null;

        for (final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator(); iterator.hasNext();) {
            final Pair<TokenCategory, Object> firstTokenPair = iterator.next();
            final Pair<TokenCategory, Object> secondTokenPair = iterator.hasNext() ? iterator.next() : null;

            if (secondTokenPair != null) {
                whenThens.add(t2((ICondition1<?>) firstTokenPair.getValue(), getModelForSingleOperand(secondTokenPair)));
            } else {
                elseOperand = getModelForSingleOperand(firstTokenPair);
            }
        }

        return new CaseWhen1(whenThens, elseOperand, (ITypeCast) lastValue);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<TokenCategory, Object>(FUNCTION_MODEL, getModel());
    }
}