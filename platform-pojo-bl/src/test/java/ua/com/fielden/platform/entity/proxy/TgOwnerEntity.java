package ua.com.fielden.platform.entity.proxy;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@KeyType(String.class)
public class TgOwnerEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    private TgEntityForProxy entityProp;

    @IsProperty
    @MapTo
    private Integer intProp;

    @IsProperty
    @MapTo
    private boolean booleanProp;

    @IsProperty(TgEntityForProxy.class)
    private final Set<TgEntityForProxy> children = new HashSet<>();

    @Observable
    protected TgOwnerEntity setChildren(final Set<TgEntityForProxy> children) {
        this.children.clear();
        this.children.addAll(children);
        return this;
    }

    public Set<TgEntityForProxy> getChildren() {
        return unmodifiableSet(children);
    }
    
    @Observable
    public TgOwnerEntity setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public boolean isBooleanProp() {
        return booleanProp;
    }

    @Observable
    public TgOwnerEntity setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public TgOwnerEntity setEntityProp(final TgEntityForProxy prop1) {
        this.entityProp = prop1;
        return this;
    }
    
    public TgEntityForProxy getEntityProp() {
        return entityProp;
    }
    
}
