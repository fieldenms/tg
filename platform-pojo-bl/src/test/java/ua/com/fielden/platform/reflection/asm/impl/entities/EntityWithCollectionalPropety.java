package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * A test entity with collectional property
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithCollectionalPropety extends AbstractEntity<String> {

    @IsProperty(EntityBeingEnhanced.class)
    private Collection<EntityBeingEnhanced> prop1;
    
    @IsProperty(Double.class)
    private final List<Double> items = new ArrayList<>();

    public Collection<EntityBeingEnhanced> getProp1() {
        return prop1;
    }

    @Observable
    public EntityWithCollectionalPropety setProp1(final Collection<EntityBeingEnhanced> prop1) {
        this.prop1.clear();
        this.prop1.addAll(prop1);
        return this;
    }

    @Observable
    public void addToProp1(final EntityBeingEnhanced value) {
        if (prop1 != null) {
            prop1.add(value);
        }
    }

    @Observable
    public void removeFromProp1(final EntityBeingEnhanced value) {
        if (prop1 != null) {
            prop1.remove(value);
        }
    }
    
    public List<Double> getItems() {
        return unmodifiableList(items);
    }
    
    @Observable
    public EntityWithCollectionalPropety setItems(final List<Double> items) {
        this.items.clear();
        this.items.addAll(items);
        return this;
    }

}
