package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(Member5Co.class)
@DeactivatableDependencies(MemberDetails.class)
public class Member5 extends ActivatableAbstractEntity<String> {

    @Observable
    @Override
    public Member5 setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    @Override
    public Member5 setRefCount(final Integer refCount) {
        super.setRefCount(refCount);
        return this;
    }

}
