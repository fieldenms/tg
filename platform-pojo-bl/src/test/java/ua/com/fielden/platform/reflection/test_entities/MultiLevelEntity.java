package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test class, which represent an second level entity derived SecondLevelEntity.
 *
 * @author TG Team
 *
 */
public class MultiLevelEntity extends SecondLevelEntity {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private MultiLevelEntity propertyOfSelfType;
    @IsProperty
    private SimplePartEntity simplePartEntity;
    @IsProperty
    private SecondLevelEntity propertyOfParentType;
    @IsProperty
    private AnotherSimplePartEntity anotherSimplePartType;


    public MultiLevelEntity() {
	setKey(new DynamicEntityKey(this));
    }

    public MultiLevelEntity getPropertyOfSelfType() {
        return propertyOfSelfType;
    }
    @Observable
    public void setPropertyOfSelfType(final MultiLevelEntity propertyOfSelfType) {
        this.propertyOfSelfType = propertyOfSelfType;
    }

    public SecondLevelEntity getPropertyOfParentType() {
        return propertyOfParentType;
    }
    @Observable
    public void setPropertyOfParentType(final SecondLevelEntity propertyOfParentType) {
        this.propertyOfParentType = propertyOfParentType;
    }

    public SimplePartEntity getSimplePartEntity() {
        return simplePartEntity;
    }
    @Observable
    public void setSimplePartEntity(final SimplePartEntity simplePartEntity) {
        this.simplePartEntity = simplePartEntity;
    }

    public AnotherSimplePartEntity getAnotherSimplePartType() {
        return anotherSimplePartType;
    }
    @Observable
    public void setAnotherSimplePartType(final AnotherSimplePartEntity anotherSimplePartType) {
        this.anotherSimplePartType = anotherSimplePartType;
    }

}
