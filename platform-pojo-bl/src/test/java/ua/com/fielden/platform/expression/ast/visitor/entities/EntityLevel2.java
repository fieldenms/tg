package ua.com.fielden.platform.expression.ast.visitor.entities;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
public class EntityLevel2 extends AbstractEntity<String> {

    @IsProperty
    private Integer intProperty;

    @IsProperty(EntityLevel3.class)
    private List<EntityLevel3> collectional;

    public Integer getIntProperty() {
	return intProperty;
    }

    @Observable
    public void setIntProperty(final Integer strProperty) {
	this.intProperty = strProperty;
    }

    public List<EntityLevel3> getCollectional() {
        return collectional;
    }

    @Observable
    public void setCollectional(final List<EntityLevel3> collectional) {
        this.collectional = collectional;
    }
}
