package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

public class Source3BasedOnTable extends AbstractSource3 {
    public final String tableName;

    public Source3BasedOnTable(final EqlTable table, final Integer id, final int sqlId) {
        super("T_" + sqlId, id, table.columns());
        this.tableName = table.name();
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return tableName + " AS " + sqlAlias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + tableName.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Source3BasedOnTable that
                  && Objects.equals(this.tableName, that.tableName)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("tableName", tableName);
    }

}
