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
public class UnionEntityHolder extends AbstractEntity<String> {

    private static final long serialVersionUID = 632043970167639225L;

    @IsProperty
    @Title(value = "union entity", desc = "unoin entity description")
    private UnionEntityForReflector unionEntity;

    public UnionEntityForReflector getUnionEntity() {
	return unionEntity;
    }

    @Observable
    public void setUnionEntity(final UnionEntityForReflector unionEntity) {
	this.unionEntity = unionEntity;
    }

}
