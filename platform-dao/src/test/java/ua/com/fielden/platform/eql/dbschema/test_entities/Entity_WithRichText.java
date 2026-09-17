package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.RichText;

/// A persistent test entity with a `RichText` property, which is a component-typed property.
/// Used to verify that a column is generated per component, and that the search-text component is indexed.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithRichText extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private RichText note;

    public RichText getNote() {
        return note;
    }

    @Observable
    public Entity_WithRichText setNote(final RichText note) {
        this.note = note;
        return this;
    }

}
