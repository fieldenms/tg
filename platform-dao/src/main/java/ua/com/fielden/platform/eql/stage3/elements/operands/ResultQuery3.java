package ua.com.fielden.platform.eql.stage3.elements.operands;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;

public class ResultQuery3 extends AbstractQuery3 {

    public ResultQuery3(final EntQueryBlocks3 queryBlocks, final Class<?> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        final StringBuffer sb = new StringBuffer();
        final String yieldsSql = yields.sql(dbVersion);
        sb.append("SELECT\n");
        sb.append(yieldsSql);
        sb.append(sourcesSql(dbVersion));
        final String conditionsSql = conditions.sql(dbVersion);
        if (isNotEmpty(conditionsSql)) {
            sb.append("\nWHERE ");
            sb.append(conditionsSql);
        }
        sb.append(groups.sql(dbVersion));
        sb.append(orderings.sql(dbVersion));
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ResultQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery3;
    }
}