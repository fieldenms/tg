package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/// A union test entity with two members, used to verify that a union-typed property yields a column per member.
///
public class Union_Place extends AbstractUnionEntity {

    @IsProperty
    @MapTo
    private Entity_Simple simple;

    @IsProperty
    @MapTo("REFS")
    private Entity_WithRefs withRefs;

    public Entity_Simple getSimple() {
        return simple;
    }

    @Observable
    public Union_Place setSimple(final Entity_Simple simple) {
        this.simple = simple;
        return this;
    }

    public Entity_WithRefs getWithRefs() {
        return withRefs;
    }

    @Observable
    public Union_Place setWithRefs(final Entity_WithRefs withRefs) {
        this.withRefs = withRefs;
        return this;
    }

}
