package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with an explicitly mapped table name and references to other persistent entities.
/// Used to verify generation of foreign keys and of indices for entity-typed properties.
///
@MapEntityTo("REFS_TABLE")
@KeyType(String.class)
public class Entity_WithRefs extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Entity_Simple simple;

    @IsProperty
    @MapTo("OTHER_")
    private Entity_Simple withCustomColumnName;

    public Entity_Simple getSimple() {
        return simple;
    }

    @Observable
    public Entity_WithRefs setSimple(final Entity_Simple simple) {
        this.simple = simple;
        return this;
    }

    public Entity_Simple getWithCustomColumnName() {
        return withCustomColumnName;
    }

    @Observable
    public Entity_WithRefs setWithCustomColumnName(final Entity_Simple withCustomColumnName) {
        this.withCustomColumnName = withCustomColumnName;
        return this;
    }

}
