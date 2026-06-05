package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(Member2Co.class)
public class Member2 extends ActivatableAbstractEntity<String> {

    @Observable
    @Override
    public Member2 setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    @Override
    public Member2 setRefCount(final Integer refCount) {
        super.setRefCount(refCount);
        return this;
    }

}
