package ua.com.fielden.platform.reflection.test_entities;

import java.lang.ref.Reference;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Test class, which represent an second level entity derived FirstLevelEntity.
 *
 * @author 01es
 *
 */
public class SecondLevelEntity extends FirstLevelEntity {

    @IsProperty
    @CompositeKeyMember(3)
    @Optional
    @Title("Another")
    private Long anotherProperty;

    @IsProperty
    @MapTo
    @Title("Self Type")
    private SecondLevelEntity propertyOfSelfType;

    @IsProperty
    @Title("Dummy Reference")
    private Reference<SecondLevelEntity> dummyReferenceProperty;

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
