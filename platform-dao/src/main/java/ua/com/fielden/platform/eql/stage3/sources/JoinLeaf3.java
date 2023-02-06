package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class JoinLeaf3 implements IJoinNode3 {
    public final ISource3 source;

    public JoinLeaf3(final ISource3 source) {
        this.source = source;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return source.sql(dbVersion);
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

        if (!(obj instanceof JoinLeaf3)) {
            return false;
        }
        
        final JoinLeaf3 other = (JoinLeaf3) obj;
        
        return Objects.equals(source, other.source);
    }

    @Override
    public boolean needsParentheses() {
        return false;
    }
}