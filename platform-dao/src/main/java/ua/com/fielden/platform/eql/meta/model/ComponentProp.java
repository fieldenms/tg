package ua.com.fielden.platform.eql.meta.model;

import java.util.HashMap;
import java.util.Map;

public class ComponentProp implements IProp {
    
    public final String name;
    public final Class<?> javaType;
    public final Map<String, IProp> props = new HashMap<>();
    
    public ComponentProp(final String name, final Class<?> javaType, final Map<String, IProp> props) {
        this.name = name;
        this.javaType = javaType;
        this.props.putAll(props);
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
