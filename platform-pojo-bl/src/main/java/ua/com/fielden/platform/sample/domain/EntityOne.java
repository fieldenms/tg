package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
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

    @IsProperty
    @MapTo
    private EntityThree entityThree;

    public EntityThree getEntityThree() {
        return entityThree;
    }

    @Observable
    public EntityOne setEntityThree(final EntityThree entityThree) {
        this.entityThree = entityThree;
        return this;
    }

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

    @Override
    @Observable
    public EntityOne setDesc(String desc) {
        return super.setDesc(desc);
    }

}
