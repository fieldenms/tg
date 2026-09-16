package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with a composite key, one of whose members is union-typed.
/// Used to verify which columns of a union-typed property participate in the composite unique index.
///
@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class Entity_WithUnionKeyMember extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Union_Place place;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    public Union_Place getPlace() {
        return place;
    }

    @Observable
    public Entity_WithUnionKeyMember setPlace(final Union_Place place) {
        this.place = place;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public Entity_WithUnionKeyMember setName(final String name) {
        this.name = name;
        return this;
    }

}
