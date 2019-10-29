package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditions;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.utils.Pair;

public class GroupedConditionsBuilder extends AbstractTokensBuilder {

    private final boolean negated;

    protected GroupedConditionsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final boolean negated) {
        super(parent, queryBuilder, paramValues);
        this.negated = negated;
        setChild(new ConditionBuilder(this, queryBuilder, paramValues));
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
        final ICondition firstCondition = (ICondition) iterator.next().getValue();
        final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();
        for (; iterator.hasNext();) {
            final CompoundCondition subsequentCompoundCondition = (CompoundCondition) iterator.next().getValue();
            otherConditions.add(subsequentCompoundCondition);
        }
        return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, new GroupedConditions(negated, firstCondition, otherConditions));
    }

    @Override
    public int hashCode() {
        return 31 * (negated ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GroupedConditionsBuilder)) {
            return false;
        }
        final GroupedConditionsBuilder that = (GroupedConditionsBuilder) obj;
        return this.negated == that.negated;
    }
}