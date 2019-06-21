package ua.com.fielden.platform.eql.stage3.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EntProp3 implements ISingleOperand3 {
    public final String name;
    public final IQrySource3 source;

    public EntProp3(final String name, final IQrySource3 source) {
        this.name = name;
        this.source = source;
    }

    @Override
    public String sql() {
        return source.sqlAlias() + "." + source.column(name).name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof EntProp3)) {
            return false;
        }
        
        final EntProp3 other = (EntProp3) obj;
        
        return Objects.equals(name, other.name) &&
                Objects.equals(source, other.source);
    }
}