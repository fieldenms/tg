package ua.com.fielden.platform.serialisation.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@KeyTitle(value = "Base Entity No", desc = "Key Property")
public abstract class BaseEntity extends AbstractEntity<String> {

}
