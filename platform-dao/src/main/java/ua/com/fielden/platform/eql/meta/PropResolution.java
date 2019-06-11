package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;

public class PropResolution {
    private final String aliaslessName;
    private final IQrySource2 source;
    private final Class<?> type;

    public PropResolution(final String aliaslessName, final IQrySource2 source, final Class<?> type) {
        this.aliaslessName = aliaslessName;
        this.source = source;
        this.type = type;
    }

    public String getAliaslessName() {
        return aliaslessName;
    }

    public IQrySource2 getSource() {
        return source;
    }

    public Class<?> getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return source + "  " + type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + aliaslessName.hashCode();
        result = prime * result + source.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PropResolution)) {
            return false;
        }
        final PropResolution other = (PropResolution) obj;
        if (aliaslessName == null) {
            if (other.aliaslessName != null) {
                return false;
            }
        } else if (!aliaslessName.equals(other.aliaslessName)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }
}
