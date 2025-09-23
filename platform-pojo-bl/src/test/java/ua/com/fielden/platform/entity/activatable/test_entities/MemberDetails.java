package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(DynamicEntityKey.class)
@CompanionObject(MemberDetailsCo.class)
public class MemberDetails extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Union union;

    @IsProperty
    @MapTo
    private Union union2;

    public Union getUnion2() {
        return union2;
    }

    @Observable
    public MemberDetails setUnion2(final Union union2) {
        this.union2 = union2;
        return this;
    }

    @Observable
    public MemberDetails setUnion(final Union union) {
        this.union = union;
        return this;
    }

    public Union getUnion() {
        return union;
    }


    @Observable
    @Override
    public MemberDetails setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    @Override
    public MemberDetails setRefCount(final Integer refCount) {
        super.setRefCount(refCount);
        return this;
    }

}
