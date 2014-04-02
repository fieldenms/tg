package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntityForReflector extends AbstractUnionEntity {

    private static final long serialVersionUID = 8000899765577496777L;

    @IsProperty
    @Title(value = "First entity", desc = "First entity description")
    private SimplePartEntity simplePartEntity;

    @IsProperty
    @Title(value = "Second entity", desc = "Second entity description")
    private ComplexPartEntity1 complexPartEntity;

    @IsProperty
    @Title(value = "Third entity", desc = "Third entity description")
    private DynamicKeyPartEntity dynamicKeyPartEntity;

    public SimplePartEntity getSimplePartEntity() {
        return simplePartEntity;
    }

    @Observable
    public void setSimplePartEntity(final SimplePartEntity simplePartEntity) {
        this.simplePartEntity = simplePartEntity;
    }

    public ComplexPartEntity1 getComplexPartEntity() {
        return complexPartEntity;
    }

    @Observable
    public void setComplexPartEntity(final ComplexPartEntity1 complexPartEntity) {
        this.complexPartEntity = complexPartEntity;
    }

    public DynamicKeyPartEntity getDynamicKeyPartEntity() {
        return dynamicKeyPartEntity;
    }

    @Observable
    public void setDynamicKeyPartEntity(final DynamicKeyPartEntity dynamicKeyPartEntity) {
        this.dynamicKeyPartEntity = dynamicKeyPartEntity;
    }

}
