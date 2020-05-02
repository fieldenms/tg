package ua.com.fielden.platform.eql.stage3.elements.conditions;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Conditions3 implements ICondition3 {
    private final List<List<? extends ICondition3>> allConditionsAsDnf = new ArrayList<>();
    private final boolean negated;

    public Conditions3(final boolean negated, final List<List<? extends ICondition3>> allConditions) {
        this.allConditionsAsDnf.addAll(allConditions);
        this.negated = negated;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return sql(dbVersion, false);
    }
    
    public String sql(final DbVersion dbVersion, final boolean atWhere) {
        final StringBuffer sb = new StringBuffer();
        if (!allConditionsAsDnf.isEmpty()) {
            if (atWhere) {
                sb.append("\nWHERE ");    
            }
            sb.append(allConditionsAsDnf.stream().map(dl -> dl.stream().map(cond -> cond.sql(dbVersion)).collect(joining(" AND "))).collect(joining(" OR ")));
        }
        return sb.toString();    
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + allConditionsAsDnf.hashCode();
        result = prime * result + (negated ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Conditions3)) {
            return false;
        }
        
        final Conditions3 other = (Conditions3) obj;
        
        return Objects.equals(allConditionsAsDnf, other.allConditionsAsDnf) && (negated == other.negated);
    }
}