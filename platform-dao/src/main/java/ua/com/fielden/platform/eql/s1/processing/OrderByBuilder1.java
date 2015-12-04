package ua.com.fielden.platform.eql.s1.processing;

import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.QRY_YIELD;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.YIELD;
import ua.com.fielden.platform.entity.query.fluent.SortingOrderDirection;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.OrderBy1;
import ua.com.fielden.platform.utils.Pair;

public class OrderByBuilder1 extends AbstractTokensBuilder1 {

    protected OrderByBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final SortingOrderDirection sortingOrder = (SortingOrderDirection) secondValue();
        if (firstCat() == YIELD) {
            return new Pair<TokenCategory, Object>(QRY_YIELD, new OrderBy1((String) firstValue(), sortingOrder));
        } else {
            final ISingleOperand1 operand = getModelForSingleOperand(firstCat(), firstValue());
            return new Pair<TokenCategory, Object>(QRY_YIELD, new OrderBy1(operand, sortingOrder));
        }
    }
}