package ua.com.fielden.platform.eql.stage3.elements.sources;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;

public class JoinedQrySource3 implements IQrySources3 {
    public final IQrySources3 leftSource;
    public final IQrySources3 rightSource;
    public final JoinType joinType;
    public final Conditions3 joinConditions;

    public JoinedQrySource3(final IQrySources3 leftSource, final IQrySources3 rightSource, final JoinType joinType, final Conditions3 joinConditions) {
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        final String joinConditionsSql = joinConditions.sql(dbVersion);
        return "(" + leftSource.sql(dbVersion) + "\n  " + joinType + "\n" + rightSource.sql(dbVersion) + (isNotEmpty(joinConditionsSql) ? "  ON " : "") + joinConditionsSql + ")";
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

        if (!(obj instanceof JoinedQrySource3)) {
            return false;
        }
        
        final JoinedQrySource3 other = (JoinedQrySource3) obj;
        
        return Objects.equals(leftSource, other.leftSource) &&
                Objects.equals(rightSource, other.rightSource) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}