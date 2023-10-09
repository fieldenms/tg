package ua.com.fielden.platform.eql.stage3.sources;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;

public class JoinInnerNode3 implements IJoinNode3 {
    public final IJoinNode3 leftNode;
    public final IJoinNode3 rightNode;
    public final JoinType joinType;
    public final Conditions3 joinConditions;

    public JoinInnerNode3(final IJoinNode3 leftNode, final IJoinNode3 rightNode, final JoinType joinType, final Conditions3 joinConditions) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        final String joinConditionsSql = joinConditions.sql(dbVersion);
        return joinNodeSql(dbVersion, leftNode) + "\n  " + joinType + "\n" + joinNodeSql(dbVersion, rightNode) + (isNotEmpty(joinConditionsSql) ? "  ON " : "") + joinConditionsSql;
    }
    
    private String joinNodeSql(final DbVersion dbVersion, IJoinNode3 joinNode) {
        return joinNode.needsParentheses() ? "(" + joinNode.sql(dbVersion) + ")" : joinNode.sql(dbVersion); 
     }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftNode.hashCode();
        result = prime * result + rightNode.hashCode();
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JoinInnerNode3)) {
            return false;
        }
        
        final JoinInnerNode3 other = (JoinInnerNode3) obj;
        
        return Objects.equals(leftNode, other.leftNode) &&
                Objects.equals(rightNode, other.rightNode) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }

    @Override
    public boolean needsParentheses() {
        return true;
    }
}