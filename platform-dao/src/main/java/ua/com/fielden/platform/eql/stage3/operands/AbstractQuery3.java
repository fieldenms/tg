package ua.com.fielden.platform.eql.stage3.operands;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.QueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public abstract class AbstractQuery3 {

    public final ISources3 sources;
    public final Conditions3 conditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    public final Class<?> resultType;

    public AbstractQuery3(final QueryBlocks3 queryBlocks, final Class<?> resultType) {
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
    }
    
    public String sql(final DbVersion dbVersion) {
        final StringBuffer sb = new StringBuffer();
        sb.append(yields.sql(dbVersion));
        sb.append(sources != null ? "\nFROM\n" + sources.sql(dbVersion) : (dbVersion == ORACLE ? " FROM DUAL " : ""));
        sb.append(conditions != null ? "\nWHERE " + conditions.sql(dbVersion) : "");
        sb.append(groups != null ? "\nGROUP BY " + groups.sql(dbVersion) : "");
        sb.append(orderings != null ? "\nORDER BY " + orderings.sql(dbVersion) : "");
        return sb.toString();
    }

    @Override
    public String toString() {
        return sql(DbVersion.H2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + yields.hashCode();
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQuery3)) {
            return false;
        }

        final AbstractQuery3 other = (AbstractQuery3) obj;

        return Objects.equals(sources, other.sources) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings) &&
                Objects.equals(resultType, other.resultType);
    }
}