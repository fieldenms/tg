package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@DescTitle("Description")
@CompanionObject(IEntityOne.class)
@MapEntityTo
public class EntityOne extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private String stringProperty;

    @IsProperty
    @MapTo
    private Double doubleProperty;

    public String getStringProperty() {
        return stringProperty;
    }

    @Observable
    public EntityOne setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
        return this;
    }

    public Double getDoubleProperty() {
        return doubleProperty;
    }

    @Observable
    public EntityOne setDoubleProperty(final Double doubleProperty) {
        this.doubleProperty = doubleProperty;
        return this;
    }

}
