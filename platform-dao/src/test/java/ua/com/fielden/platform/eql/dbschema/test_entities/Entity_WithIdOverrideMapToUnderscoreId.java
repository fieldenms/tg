package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity that redeclares the inherited `id` field with `@IsProperty` and `@MapTo("_ID")`.
/// Used to verify that DDL generation gracefully skips the duplicate column.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithIdOverrideMapToUnderscoreId extends AbstractEntity<String> {

    @IsProperty
    @MapTo("_ID")
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    @Observable
    public void setId(final Long id) {
        this.id = id;
    }

}
