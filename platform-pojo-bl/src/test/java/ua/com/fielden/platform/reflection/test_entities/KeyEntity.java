package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class KeyEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -6362757147177400282L;

    @IsProperty
    @Title(value = "simple entity", desc = "simple entity description")
    private SimpleEntity simpleEntity;

    public SimpleEntity getSimpleEntity() {
	return simpleEntity;
    }

    @Observable
    public void setSimpleEntity(final SimpleEntity simpleEntity) {
	this.simpleEntity = simpleEntity;
    }
}
