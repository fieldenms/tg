package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

import java.math.BigDecimal;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 * 
 */
public class EntityExt extends Entity {

    @IsProperty
    @Title("Additional property")
    private BigDecimal additionalProperty;

    public BigDecimal getAdditionalProperty() {
        return additionalProperty;
    }

    @Observable
    public void setAdditionalProperty(final BigDecimal additionalProperty) {
        this.additionalProperty = additionalProperty;
    }

}
