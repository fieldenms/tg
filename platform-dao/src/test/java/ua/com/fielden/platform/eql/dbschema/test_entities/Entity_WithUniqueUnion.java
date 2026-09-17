package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with a `@Unique` union-typed property.
/// Used to verify which columns of a union-typed property are uniquely indexed.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithUniqueUnion extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Unique
    private Union_Place place;

    public Union_Place getPlace() {
        return place;
    }

    @Observable
    public Entity_WithUniqueUnion setPlace(final Union_Place place) {
        this.place = place;
        return this;
    }

}
