package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.testannotation.ConstructorAnnotationRuntime;

import java.math.BigDecimal;

/**
 * A test entity type that declares constructors.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class EntityWithConstructors extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Property")
    private String property;

    @IsProperty
    @MapTo
    @Title(value = "Number", desc = "A numeric property")
    private BigDecimal number;

    private EntityWithConstructors() {};

    @ConstructorAnnotationRuntime
    protected EntityWithConstructors(final String key) {
        super(null, key, "");
    }
    
    private EntityWithConstructors(final BigDecimal number) {
        this.number = number;
    }
    
    @ConstructorAnnotationRuntime
    public EntityWithConstructors(final String property, final BigDecimal number) {
        this.property = property;
        this.number = number;
    }

    public String getProperty() {
        return property;
    }

    @Observable
    public void setProperty(final String property) {
        this.property = property;
    }

    @Observable
    public EntityWithConstructors setName(final BigDecimal number) {
        this.number = number;
        return this;
    }

    public BigDecimal getName() {
        return number;
    }
    
}
