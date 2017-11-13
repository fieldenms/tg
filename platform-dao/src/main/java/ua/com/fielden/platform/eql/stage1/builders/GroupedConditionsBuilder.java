package ua.com.fielden.platform.eql.stage1.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage2.elements.ICondition2;
import ua.com.fielden.platform.eql.stage1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.elements.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.ICondition1;
import ua.com.fielden.platform.utils.Pair;

public class GroupedConditionsBuilder extends AbstractTokensBuilder {

    private final boolean negated;

    protected GroupedConditionsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final boolean negated) {
        super(parent, queryBuilder);
        this.negated = negated;
        setChild(new ConditionBuilder(this, queryBuilder));
    }

    @Override
    public boolean isClosing() {
        return getLastCat().equals(TokenCategory.END_COND);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (TokenCategory.END_COND.equals(getLastCat())) {
            getTokens().remove(getSize() - 1);
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final ICondition1<? extends ICondition2> firstCondition = (ICondition1<? extends ICondition2>) iterator.next().getValue();
        final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
        for (; iterator.hasNext();) {
            final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
            otherConditions.add(subsequentCompoundCondition);
        }
        return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, new Conditions1(negated, firstCondition, otherConditions));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GroupedConditionsBuilder)) {
            return false;
        }
        final GroupedConditionsBuilder other = (GroupedConditionsBuilder) obj;
        if (negated != other.negated) {
            return false;
        }
        return true;
    }
}