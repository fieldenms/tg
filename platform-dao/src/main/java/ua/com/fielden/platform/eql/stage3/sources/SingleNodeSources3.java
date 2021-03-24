package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Objects;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import ua.com.fielden.platform.entity.query.DbVersion;

public class SingleNodeSources3 implements ISources3 {
    public final ISource3 source;

    public SingleNodeSources3(final ISource3 source) {
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

        if (!(obj instanceof SingleNodeSources3)) {
            return false;
        }
        
        final SingleNodeSources3 other = (SingleNodeSources3) obj;
        
        return Objects.equals(source, other.source);
    }
}