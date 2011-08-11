package ua.com.fielden.platform.reflection.test_entities;

import java.lang.ref.Reference;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test class, which represent an second level entity derived FirstLevelEntity.
 * 
 * @author 01es
 * 
 */
public class SecondLevelEntity extends FirstLevelEntity {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(3)
    private Long anotherProperty;

    @IsProperty
    private SecondLevelEntity propertyOfSelfType;

    @IsProperty
    private Reference<SecondLevelEntity> dummyReferenceProperty;

    public SecondLevelEntity() {
	super(null, null, "");
	setKey(new DynamicEntityKey(this));
    }

    public Long getAnotherProperty() {
	return anotherProperty;
    }

    @Observable
    public void setAnotherProperty(final Long anotherProperty) {
	this.anotherProperty = anotherProperty;
    }

    public SecondLevelEntity getPropertyOfSelfType() {
	return propertyOfSelfType;
    }

    @Observable
    public void setPropertyOfSelfType(final SecondLevelEntity itself) {
	this.propertyOfSelfType = itself;
    }

    public Reference<SecondLevelEntity> getDummyReferenceProperty() {
	return dummyReferenceProperty;
    }

    @Observable
    public void setListProperty(final Reference<SecondLevelEntity> dummyReferenceProperty) {
	this.dummyReferenceProperty = dummyReferenceProperty;
    }

    public boolean methodSecondLevel() {
	return true;
    }

    @Override
    @Observable
    public void setProperty(final String property) {
	super.setProperty(property);
    }

}
