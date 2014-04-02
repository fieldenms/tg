package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntityWithoutDesc extends AbstractUnionEntity {

    private static final long serialVersionUID = -2697003300590628392L;

    @IsProperty
    @Title(value = "First entity", desc = "First entity description")
    private SimplePartEntity simplePartEntity;

    @IsProperty
    @Title(value = "Second entity", desc = "Second entity description")
    private ComplexPartEntity complexPartEntity;

    @IsProperty
    @Title(value = "Third entity", desc = "Third entity description")
    private SimpleWithoutDescEntity simpleEntityWithoutDesc;

    public SimplePartEntity getSimplePartEntity() {
        return simplePartEntity;
    }

    @Observable
    public void setSimplePartEntity(final SimplePartEntity simplePartEntity) {
        this.simplePartEntity = simplePartEntity;
    }

    public ComplexPartEntity getComplexPartEntity() {
        return complexPartEntity;
    }

    @Observable
    public void setComplexPartEntity(final ComplexPartEntity complexPartEntity) {
        this.complexPartEntity = complexPartEntity;
    }

    public SimpleWithoutDescEntity getSimpleEntityWithoutDesc() {
        return simpleEntityWithoutDesc;
    }

    @Observable
    public void setSimpleEntityWithoutDesc(final SimpleWithoutDescEntity simpleEntityWithoutDesc) {
        this.simpleEntityWithoutDesc = simpleEntityWithoutDesc;
    }

}
