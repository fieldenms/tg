package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with a composite key.
/// Key members are declared in an order opposite to their `@CompositeKeyMember` order,
/// so that the ordering of columns in the generated composite unique index can be verified.
///
@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class Entity_WithCompositeKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String second;

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Entity_Simple first;

    public String getSecond() {
        return second;
    }

    @Observable
    public Entity_WithCompositeKey setSecond(final String second) {
        this.second = second;
        return this;
    }

    public Entity_Simple getFirst() {
        return first;
    }

    @Observable
    public Entity_WithCompositeKey setFirst(final Entity_Simple first) {
        this.first = first;
        return this;
    }

}
