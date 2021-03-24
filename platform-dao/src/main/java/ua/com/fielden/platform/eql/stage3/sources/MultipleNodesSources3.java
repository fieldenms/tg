package ua.com.fielden.platform.eql.stage3.sources;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;

public class MultipleNodesSources3 implements ISources3 {
    public final ISources3 leftSource;
    public final ISources3 rightSource;
    public final JoinType joinType;
    public final Conditions3 joinConditions;

    public MultipleNodesSources3(final ISources3 leftSource, final ISources3 rightSource, final JoinType joinType, final Conditions3 joinConditions) {
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public String sql(final DbVersion dbVersion, final boolean atFromStmt) {
        final String joinConditionsSql = joinConditions.sql(dbVersion);
        return (atFromStmt ? "\nFROM\n" : "(") + leftSource.sql(dbVersion, false) + "\n  " + joinType + "\n" + rightSource.sql(dbVersion, false) + (isNotEmpty(joinConditionsSql) ? "  ON " : "") + joinConditionsSql + (atFromStmt ? "" : ")");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftSource.hashCode();
        result = prime * result + rightSource.hashCode();
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MultipleNodesSources3)) {
            return false;
        }
        
        final MultipleNodesSources3 other = (MultipleNodesSources3) obj;
        
        return Objects.equals(leftSource, other.leftSource) &&
                Objects.equals(rightSource, other.rightSource) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}