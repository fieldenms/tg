package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with a simple key.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class SimpleEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
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
    @NotNull
    @DomainValidation
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
