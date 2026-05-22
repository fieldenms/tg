package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity that redeclares the inherited `id` field with `@IsProperty` and `@MapTo` without a value.
/// Used to verify that DDL generation rejects this configuration.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithIdOverrideMapToEmpty extends AbstractEntity<String> {

    @IsProperty
    @MapTo
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
