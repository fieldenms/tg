package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(Member2Co.class)
public class Member2 extends AbstractPersistentEntity<String> {

}
