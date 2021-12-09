package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with a simple key.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class SimpleEntity extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Property")
    private String property;

    @IsProperty
    @Title(value = "propertyTwo")
    private String propertyTwo;

    protected SimpleEntity(final String key) {
        super(null, key, "");
    }

    public String getProperty() {
        return property;
    }

    @Observable
    public void setProperty(final String property) {
        this.property = property;
    }

    public String getPropertyTwo() {
        return propertyTwo;
    }

    @Observable
    public void setPropertyTwo(final String propertyTwo) {
        this.propertyTwo = propertyTwo;
    }
}