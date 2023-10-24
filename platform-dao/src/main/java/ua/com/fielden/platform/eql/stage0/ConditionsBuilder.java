package ua.com.fielden.platform.eql.stage0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.utils.Pair;

public class ConditionsBuilder extends AbstractTokensBuilder {

    protected ConditionsBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
        //	setChild(new ConditionBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    public Conditions1 getModel() {
        if (getChild() != null) {
            throw new RuntimeException("Unable to produce result - unfinished model state!");
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        if (!iterator.hasNext()) {
            return Conditions1.EMPTY_CONDITIONS;
        } else {
            final ICondition1<? extends ICondition2<?>> firstCondition = (ICondition1<? extends ICondition2<?>>) iterator.next().getValue();
            final List<CompoundCondition1> otherConditions = new ArrayList<>();
            for (; iterator.hasNext();) {
                final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
                otherConditions.add(subsequentCompoundCondition);
            }
            return new Conditions1(false, firstCondition, otherConditions);
        }
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new RuntimeException("Not applicable!");
    }
}
