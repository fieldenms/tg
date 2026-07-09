package ua.com.fielden.platform.eql.stage3.conditions;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;

import static java.util.stream.Collectors.joining;

public record Conditions3 (boolean negated, List<List<? extends ICondition3>> allConditionsAsDnf)
        implements ICondition3, ToString.IFormattable
{

    public Conditions3(final boolean negated, final List<List<? extends ICondition3>> allConditionsAsDnf) {
        this.allConditionsAsDnf = ImmutableList.copyOf(allConditionsAsDnf);
        this.negated = negated;
    }

    public boolean isEmpty() {
        return allConditionsAsDnf().isEmpty();
    }

    public Conditions3 update(final boolean negated, final List<List<? extends ICondition3>> allConditionsAsDnf) {
        if (negated == this.negated && allConditionsAsDnf == this.allConditionsAsDnf) {
            return this;
        }
        else {
            return new Conditions3(negated, allConditionsAsDnf);
        }
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String sqlBody = allConditionsAsDnf.stream()
                .map(dl -> dl.stream().map(cond -> cond.sql(metadata, dbVersion)).collect(joining(" AND ")))
                .collect(joining(" OR "));
        final boolean parenthesesNeeded = allConditionsAsDnf.size() > 1 || negated;
        final String negation = negated ? " NOT " : "";
        return negation + (parenthesesNeeded ? "(" : "") + sqlBody + (parenthesesNeeded ? ")" : "");
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("negated", negated)
                .add("dnf", allConditionsAsDnf)
                .$();
    }

}
