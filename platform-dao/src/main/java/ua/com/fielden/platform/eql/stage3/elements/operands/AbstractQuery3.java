package ua.com.fielden.platform.eql.stage3.elements.operands;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBys3;
import ua.com.fielden.platform.eql.stage3.elements.OrderBys3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;

public abstract class AbstractQuery3 {

    public final IQrySources3 sources;
    public final Conditions3 conditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    public final Class<?> resultType;

    public AbstractQuery3(final EntQueryBlocks3 queryBlocks, final Class<?> resultType) {
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
        sb.append(sourcesSql(dbVersion));
        sb.append(conditions.sql(dbVersion, true));
        sb.append(groups.sql(dbVersion));
        sb.append(orderings.sql(dbVersion));
        return sb.toString();
    }

    protected String sourcesSql(final DbVersion dbVersion) {
        if (sources == null) {
            return dbVersion == ORACLE ? " FROM DUAL " : " ";
        } else {
            return sources.sql(dbVersion, true);
        }
    }

    @Override
    public String toString() {
        return sql(DbVersion.H2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditions.hashCode();
        result = prime * result + yields.hashCode();
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + groups.hashCode();
        result = prime * result + orderings.hashCode();
        result = prime * result + resultType.hashCode();
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