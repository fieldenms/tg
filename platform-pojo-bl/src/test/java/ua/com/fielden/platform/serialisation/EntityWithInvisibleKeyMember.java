package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/// An entity with a composite key, one member of which is invisible.
///
@KeyType(DynamicEntityKey.class)
@KeyTitle("Key")
public class EntityWithInvisibleKeyMember extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title("Name")
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @Title("Code")
    @CompositeKeyMember(2)
    @Invisible
    private String code;

    public String getName() {
        return name;
    }

    @Observable
    public EntityWithInvisibleKeyMember setName(final String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    @Observable
    public EntityWithInvisibleKeyMember setCode(final String code) {
        this.code = code;
        return this;
    }

}
