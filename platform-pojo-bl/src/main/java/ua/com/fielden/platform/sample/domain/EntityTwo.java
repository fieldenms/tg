package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

@KeyType(String.class)
@DescTitle("Description")
@CompanionObject(IEntityTwo.class)
@MapEntityTo
public class EntityTwo extends AbstractEntity<String> {

    public enum Property implements IConvertableToPath {
        stringProperty, integerProperty;

        @Override public String toPath() { return name(); }
    }

    @IsProperty
    @MapTo
    private String stringProperty;

    @IsProperty
    @MapTo
    private Integer integerProperty;

    @IsProperty
    @MapTo
    private EntityThree entityThree;

    public EntityThree getEntityThree() {
        return entityThree;
    }

    @Observable
    public EntityTwo setEntityThree(final EntityThree entityThree) {
        this.entityThree = entityThree;
        return this;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    @Observable
    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Integer getIntegerProperty() {
        return integerProperty;
    }

    @Observable
    public void setIntegerProperty(final Integer integerProperty) {
        this.integerProperty = integerProperty;
    }

    @Override
    @Observable
    public EntityTwo setDesc(String desc) {
        return super.setDesc(desc);
    }

}
