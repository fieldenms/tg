package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with a union-typed property.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithUnion extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Union_Place place;

    public Union_Place getPlace() {
        return place;
    }

    @Observable
    public Entity_WithUnion setPlace(final Union_Place place) {
        this.place = place;
        return this;
    }

}
