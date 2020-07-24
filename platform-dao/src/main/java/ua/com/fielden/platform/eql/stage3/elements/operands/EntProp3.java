package ua.com.fielden.platform.eql.stage3.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EntProp3 implements ISingleOperand3 {
    public final String name;
    public final IQrySource3 source;
    public final Class<?> type;
    public final Object hibType;

    public EntProp3(final String name, final IQrySource3 source, final Class<?> type, final Object hibType) {
        this.name = name;
        this.source = source;
        this.type = type;
        this.hibType = hibType;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        if (source.column(name) == null) {
            System.out.println("CAN'T find column for " + name + " in " +  source);
        }
        return source.sqlAlias() + "." + source.column(name);
    }
    
    @Override
    public Class<?> type() {
        return type;
    }
    
    @Override
    public Object hibType() {
        return hibType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + source.hashCode();
        result = prime * result + hibType.getClass().getName().hashCode();
        result = prime * result + (type == null ? 0 : source.hashCode());
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
        
        return Objects.equals(name, other.name) && Objects.equals(source, other.source) && Objects.equals(type, other.type) && Objects.equals(hibType.getClass().getName(), other.hibType.getClass().getName());
    }
}