package ua.com.fielden.platform.entity.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.YIELD;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.SortingOrder;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBy;
import ua.com.fielden.platform.utils.Pair;

public class OrderByBuilder extends AbstractTokensBuilder {
    private boolean descOrder;

    protected OrderByBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final SortingOrder orderDirection = (SortingOrder) secondValue();
        if (firstCat() == YIELD) {
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy((String) firstValue(), SortingOrder.ASC.equals(orderDirection) ? false : true));
        } else {
            final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy(operand, SortingOrder.ASC.equals(orderDirection) ? false : true));
        }
    }
}
