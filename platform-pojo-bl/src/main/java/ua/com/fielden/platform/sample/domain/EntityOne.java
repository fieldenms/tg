package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.math.BigDecimal;

@KeyType(String.class)
@DescTitle("Description")
@CompanionObject(IEntityOne.class)
@MapEntityTo
public class EntityOne extends AbstractEntity<String> {

    public enum Property implements IConvertableToPath {
        stringProperty, bigDecimalProperty;

        @Override public String toPath() { return name(); }
    }

    @IsProperty
    @MapTo
    private String stringProperty;

    @IsProperty
    @MapTo
    private BigDecimal bigDecimalProperty;

    public String getStringProperty() {
        return stringProperty;
    }

    @Observable
    public EntityOne setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
        return this;
    }

    public BigDecimal getBigDecimalProperty() {
        return bigDecimalProperty;
    }

    @Observable
    public EntityOne setBigDecimalProperty(final BigDecimal bigDecimalProperty) {
        this.bigDecimalProperty = bigDecimalProperty;
        return this;
    }

}
