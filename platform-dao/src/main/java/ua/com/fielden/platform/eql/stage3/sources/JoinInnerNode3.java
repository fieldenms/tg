package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public record JoinInnerNode3 (IJoinNode3 leftNode, IJoinNode3 rightNode,
                              JoinType joinType, Conditions3 joinConditions)
        implements IJoinNode3, ToString.IFormattable
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        final String joinConditionsSql = joinConditions.sql(metadata, dbVersion);
        return joinNodeSql(metadata, dbVersion, leftNode)
               + "\n  "
               + joinType
               + "\n"
               + joinNodeSql(metadata, dbVersion, rightNode)
               + (isNotEmpty(joinConditionsSql) ? "  ON " : "")
               + joinConditionsSql;
    }

    private String joinNodeSql(final IDomainMetadata metadata, final DbVersion dbVersion, IJoinNode3 joinNode) {
        return joinNode.needsParentheses()
                ? "(" + joinNode.sql(metadata, dbVersion) + ")"
                : joinNode.sql(metadata, dbVersion);
    }

    @Override
    public boolean needsParentheses() {
        return true;
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("left", leftNode)
                .add("right", rightNode)
                .add("type", joinType)
                .addIfNot("conditions", joinConditions, Conditions3::isEmpty)
                .$();
    }

}
