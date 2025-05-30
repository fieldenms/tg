package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(Integer.class)
@DescTitle("Description")
@CompanionObject(IEntityTwo.class)
@MapEntityTo
public class EntityTwo extends AbstractEntity<Integer> {

    @IsProperty
    @MapTo
    private String stringProperty;

    @IsProperty
    @MapTo
    private Integer integerProperty;

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

}
