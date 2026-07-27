package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(TestExists_Member2Co.class)
public class TestExists_Member2 extends AbstractEntity<String> {

}
