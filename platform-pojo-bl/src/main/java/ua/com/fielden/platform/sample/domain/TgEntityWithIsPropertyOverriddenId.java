package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity that redeclares the inherited [#id] field with `@IsProperty`.
///
/// Used by Web UI tests to verify that entity-master retrieval works for entities whose `id` is annotated with `@IsProperty`.
/// The corresponding entity master does not expose `id` intentionally.
///
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@CompanionObject(TgEntityWithIsPropertyOverriddenIdCo.class)
public class TgEntityWithIsPropertyOverriddenId extends AbstractEntity<String> {

    @IsProperty
    // `id` is intentionally redeclared as `@IsProperty` — the scenario under test.
    // No `@MapTo` is required: `_ID` is always generated from `AbstractEntity.id` (see `TableDdl`).
    @Title(value = "Id", desc = "Surrogate unique identifier.")
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
