package ua.com.fielden.platform.eql.stage1.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.stage1.elements.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneConditionBuilder extends AbstractTokensBuilder {
    private final boolean negated;

    public StandAloneConditionBuilder(final EntQueryGenerator queryBuilder, final ConditionModel exprModel, final boolean negated) {
        super(null, queryBuilder);
        this.negated = negated;
        setChild(new ConditionBuilder(this, queryBuilder));

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

    public Conditions1 getModel() {
        if (getChild() != null) {
            throw new RuntimeException("Unable to produce result - unfinished model state!");
        }

        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final ICondition1<? extends ICondition2<?>> firstCondition = (ICondition1<? extends ICondition2<?>>) iterator.next().getValue();
        final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
        for (; iterator.hasNext();) {
            final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
            otherConditions.add(subsequentCompoundCondition);
        }
        return new Conditions1(negated, firstCondition, otherConditions);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new RuntimeException("Not applicable!");
    }
}