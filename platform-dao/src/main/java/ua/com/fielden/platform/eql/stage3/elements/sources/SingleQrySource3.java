package ua.com.fielden.platform.eql.stage3.elements.sources;

import java.util.Objects;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import ua.com.fielden.platform.entity.query.DbVersion;

public class SingleQrySource3 implements IQrySources3 {
    public final IQrySource3 source;

    public SingleQrySource3(final IQrySource3 source) {
        this.source = source;
    }

    @Override
    public String sql(final DbVersion dbVersion, final boolean atFromStmt) {
        final String sql = source.sql(dbVersion);
        return (atFromStmt && isNotEmpty(sql) ? "\nFROM\n" : "") + sql;
    }
    
    @Override
    public int hashCode() {
        return 31 + source.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SingleQrySource3)) {
            return false;
        }
        
        final SingleQrySource3 other = (SingleQrySource3) obj;
        
        return Objects.equals(source, other.source);
    }
}