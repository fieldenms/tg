package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@KeyTitle(value = "key title", desc = "key description")
public class EntityWithoutDesc extends AbstractEntity<String> {

    private static final long serialVersionUID = -1874035931967151214L;

    @IsProperty
    private String commonProperty;
}
