package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@KeyTitle(value = "key title", desc = "key description")
public class EntityWithoutDesc extends AbstractEntity<String> {

    @IsProperty
    private String commonProperty;

}