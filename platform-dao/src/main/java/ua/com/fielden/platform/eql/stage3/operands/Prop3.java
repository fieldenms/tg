package ua.com.fielden.platform.eql.stage3.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class Prop3 extends AbstractSingleOperand3 {
    public final String name;
    public final ISource3 source;

    public Prop3(final String name, final ISource3 source, final PropType type) {
        super(type);
        this.name = name;
        this.source = source;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return source.column(name);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + (source == null ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof Prop3)) {
            return false;
        }
        
        final Prop3 other = (Prop3) obj;
        
        return Objects.equals(name, other.name) && Objects.equals(source, other.source);
    }
}