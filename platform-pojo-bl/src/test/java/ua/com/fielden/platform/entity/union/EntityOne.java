package ua.com.fielden.platform.entity.union;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@DescTitle("Description")
public class EntityOne extends AbstractEntity<String> {

    @IsProperty
    private String stringProperty;

    @IsProperty
    private Double doubleProperty;

    public String getStringProperty() {
	return stringProperty;
    }

    @Observable
    public void setStringProperty(final String stringProperty) {
	this.stringProperty = stringProperty;
    }

    public Double getDoubleProperty() {
	return doubleProperty;
    }

    @Observable
    public void setDoubleProperty(final Double doubleProperty) {
	this.doubleProperty = doubleProperty;
    }

}
