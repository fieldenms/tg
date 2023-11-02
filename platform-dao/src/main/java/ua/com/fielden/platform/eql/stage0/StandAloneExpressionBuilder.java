package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlStage0ProcessingException;
import ua.com.fielden.platform.eql.stage1.operands.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneExpressionBuilder extends AbstractTokensBuilder {

    public StandAloneExpressionBuilder(final QueryModelToStage1Transformer queryBuilder, final ExpressionModel exprModel) {
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
    public Pair<TokenCategory, Object> getResult() {
        if (getChild() != null) {
            throw new EqlStage0ProcessingException("Unable to produce result - unfinished model state!");
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
