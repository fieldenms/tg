package ua.com.fielden.platform.eql.meta.model;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityProp implements IProp {
    public final Class<? extends AbstractEntity<?>> javaType;
    public final String name;
    public final PropColumn column;
    
    public EntityProp(final String name, final Class<? extends AbstractEntity<?>> javaType, final PropColumn column) {
        this.name = name;
        this.javaType = javaType;
        this.column = column;
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<String, IProp> props() {
        // TODO Auto-generated method stub
        return null;
    }
}
