package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.Concat;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.utils.Pair;

public class ConcatFunctionBuilder extends AbstractTokensBuilder {

    protected ConcatFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
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
        final List<ISingleOperand> items = new ArrayList<ISingleOperand>();
        final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
        items.add(getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue()));

        for (; iterator.hasNext();) {
            final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
            final ISingleOperand subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());
            items.add(subsequentOperand);
        }

        return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, new Concat(items, getDbVersion()));
    }
}