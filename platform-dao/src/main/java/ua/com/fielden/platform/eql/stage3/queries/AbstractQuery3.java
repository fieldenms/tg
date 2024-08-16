package ua.com.fielden.platform.eql.stage3.queries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

import java.util.Objects;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

public abstract class AbstractQuery3 {

    public final IJoinNode3 joinRoot;
    public final Conditions3 whereConditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    public final Class<?> resultType;

    public AbstractQuery3(final QueryComponents3 queryComponents, final Class<?> resultType) {
        this.joinRoot = queryComponents.joinRoot();
        this.whereConditions = queryComponents.whereConditions();
        this.yields = queryComponents.yields();
        this.groups = queryComponents.groups();
        this.orderings = queryComponents.orderings();
        this.resultType = resultType;
    }

    public String sql(final DbVersion dbVersion) {
        final StringBuffer sb = new StringBuffer();
        sb.append(yields.sql(dbVersion));
        sb.append(joinRoot != null ? "\nFROM\n" + joinRoot.sql(dbVersion) : (dbVersion == ORACLE ? " FROM DUAL " : ""));
        sb.append(whereConditions != null ? "\nWHERE " + whereConditions.sql(dbVersion) : "");
        sb.append(groups != null ? "\nGROUP BY " + groups.sql(dbVersion) : "");
        sb.append(orderings != null ? "\nORDER BY " + orderings.sql(dbVersion, this) : "");
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
        result = prime * result + ((whereConditions == null) ? 0 : whereConditions.hashCode());
        result = prime * result + ((joinRoot == null) ? 0 : joinRoot.hashCode());
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

        return Objects.equals(joinRoot, other.joinRoot) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(whereConditions, other.whereConditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings) &&
                Objects.equals(resultType, other.resultType);
    }

    public static boolean isTopLevelQuery(final AbstractQuery3 query) {
        return query instanceof ResultQuery3;
    }

    public static boolean isSubQuery(final AbstractQuery3 query) {
        return !isTopLevelQuery(query);
    }

}
