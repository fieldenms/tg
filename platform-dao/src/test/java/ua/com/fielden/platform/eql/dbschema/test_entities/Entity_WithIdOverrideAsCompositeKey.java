package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent composite-key test entity whose *only* `@CompositeKeyMember` is the `@IsProperty`-overridden `id`.
/// Used to verify that DDL generation does not emit a composite unique index over an empty column list:
/// the overridden `id`'s `@CompositeKeyMember` is dismissed (see [ua.com.fielden.platform.eql.dbschema.TableDdl]),
/// so there is no column to index, and the otherwise-empty `CREATE UNIQUE INDEX … ON T()` must be skipped.
///
@MapEntityTo
@KeyType(DynamicEntityKey.class)
public class Entity_WithIdOverrideAsCompositeKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo("_ID")
    @CompositeKeyMember(1)
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