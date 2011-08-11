package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 * 
 */
public class EntityExt extends Entity {
    @IsProperty
    @Title("Additional property")
    private Double additionalProperty;

    public Double getAdditionalProperty() {
	return additionalProperty;
    }

    @Observable
    public void setAdditionalProperty(final Double additionalProperty) {
	this.additionalProperty = additionalProperty;
    }

}
