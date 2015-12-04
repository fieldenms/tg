package ua.com.fielden.platform.entity.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.*;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.SortingOrderDirection;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBy;
import ua.com.fielden.platform.utils.Pair;

public class OrderByBuilder extends AbstractTokensBuilder {

    protected OrderByBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final SortingOrderDirection sortingOrderDirection = (SortingOrderDirection) secondValue();
        if (firstCat() == YIELD) {
            return new Pair<TokenCategory, Object>(QRY_YIELD, new OrderBy((String) firstValue(), sortingOrderDirection));
        } else {
            final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
            return new Pair<TokenCategory, Object>(QRY_YIELD, new OrderBy(operand, sortingOrderDirection));
        }
    }
}
