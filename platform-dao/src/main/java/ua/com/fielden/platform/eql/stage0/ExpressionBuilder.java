package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_EXPR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.operands.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class ExpressionBuilder extends AbstractTokensBuilder {

    protected ExpressionBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getLastCat() == END_EXPR;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (isClosing()) {
            getTokens().remove(getSize() - 1);
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue());
        final List<CompoundSingleOperand1> items = new ArrayList<>();
        for (; iterator.hasNext();) {
            final ArithmeticalOperator operator = (ArithmeticalOperator) iterator.next().getValue();
            final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
            final ISingleOperand1<? extends ISingleOperand2<?>> subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());

            items.add(new CompoundSingleOperand1(subsequentOperand, operator));
        }

        return new Pair<TokenCategory, Object>(EXPR, new Expression1(firstOperand, items));
    }
}