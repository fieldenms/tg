package ua.com.fielden.platform.eql.stage1.builders;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION_MODEL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.functions.Concat1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class ConcatFunctionBuilder extends AbstractTokensBuilder {

    protected ConcatFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getLastCat() == END_FUNCTION;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isClosing()) {
            getTokens().remove(getSize() - 1);
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> items = new ArrayList<>();
        final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
        items.add(getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue()));

        for (; iterator.hasNext();) {
            final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
            final ISingleOperand1 subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());
            items.add(subsequentOperand);
        }

        return new Pair<TokenCategory, Object>(FUNCTION_MODEL, new Concat1(items));
    }
}