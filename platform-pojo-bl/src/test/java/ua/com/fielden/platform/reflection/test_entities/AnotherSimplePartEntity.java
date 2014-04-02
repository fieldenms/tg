package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "key", desc = "key description")
@DescTitle(value = "description", desc = "desc description")
public class AnotherSimplePartEntity extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Level property", desc = "Level property description")
    private SimplePartEntity simplePartEntity;

    public SimplePartEntity getSimplePartEntity() {
        return simplePartEntity;
    }

    @Observable
    public void setSimplePartEntity(final SimplePartEntity simplePartEntity) {
        this.simplePartEntity = simplePartEntity;
    }
}
