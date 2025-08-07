package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(Member3Co.class)
public class Member3 extends AbstractEntity<String> {

}
