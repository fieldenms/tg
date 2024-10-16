package ua.com.fielden.platform.eql.stage3.queries;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class SubQuery3 extends AbstractQuery3 implements ISingleOperand3 {
    private final PropType type;
    
    public SubQuery3(final QueryComponents3 queryComponents, final PropType type) {
        super(queryComponents, type.javaType());
        this.type = type;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return "(" + super.sql(dbVersion) + ")";
    }
    
    @Override
    public PropType type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type.hashCode();
        return prime * result + SubQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof SubQuery3)) {
            return false;
        }
        
        final SubQuery3 other = (SubQuery3) obj;
        
        return Objects.equals(type, other.type);
    }
    
}