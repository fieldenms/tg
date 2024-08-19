package ua.com.fielden.platform.eql.stage3.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;

public class JoinLeafNode3 implements IJoinNode3 {
    public final ISource3 source;

    public JoinLeafNode3(final ISource3 source) {
        this.source = source;
    }

    @Override
    public String sql(final EqlDomainMetadata metadata) {
        return source.sql(metadata);
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

        if (!(obj instanceof JoinLeafNode3)) {
            return false;
        }
        
        final JoinLeafNode3 other = (JoinLeafNode3) obj;
        
        return Objects.equals(source, other.source);
    }

    @Override
    public boolean needsParentheses() {
        return false;
    }
}
