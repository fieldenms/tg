package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(KeyEntity.class)
@KeyTitle(value = "Complex entity")
@DescTitle(value = "Complex description")
public class ComplexEntity extends AbstractEntity<KeyEntity> {

    private static final long serialVersionUID = 48391145890835138L;

    @IsProperty
    private String commonProperty;

    @IsProperty
    private SimpleEntity anotherUncommonProperty;
}
