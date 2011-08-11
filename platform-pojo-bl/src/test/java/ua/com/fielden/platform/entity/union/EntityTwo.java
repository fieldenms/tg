package ua.com.fielden.platform.entity.union;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(Integer.class)
@DescTitle("Description")
public class EntityTwo extends AbstractEntity<Integer> {
    @IsProperty
    private String stringProperty;

    @IsProperty
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
