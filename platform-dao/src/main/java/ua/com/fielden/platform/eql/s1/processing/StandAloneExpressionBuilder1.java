package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.s1.elements.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneExpressionBuilder1 extends AbstractTokensBuilder1 {

    public StandAloneExpressionBuilder1(final EntQueryGenerator1 queryBuilder, final ExpressionModel exprModel) {
        super(null, queryBuilder);

        for (final Pair<TokenCategory, Object> tokenPair : exprModel.getTokens()) {
            add(tokenPair.getKey(), tokenPair.getValue());
        }

    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return getChild() == null;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (getChild() != null) {
            throw new RuntimeException("Unable to produce result - unfinished model state!");
        }

        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final Pair<TokenCategory, Object> firstOperandPair = iterator.next();
        final ISingleOperand1 firstOperand = getModelForSingleOperand(firstOperandPair.getKey(), firstOperandPair.getValue());
        final List<CompoundSingleOperand1> items = new ArrayList<CompoundSingleOperand1>();
        for (; iterator.hasNext();) {
            final ArithmeticalOperator operator = (ArithmeticalOperator) iterator.next().getValue();
            final Pair<TokenCategory, Object> subsequentOperandPair = iterator.next();
            final ISingleOperand1 subsequentOperand = getModelForSingleOperand(subsequentOperandPair.getKey(), subsequentOperandPair.getValue());

            items.add(new CompoundSingleOperand1(subsequentOperand, operator));
        }

        return new Pair<TokenCategory, Object>(TokenCategory.EXPR, new Expression1(firstOperand, items));
    }
}
