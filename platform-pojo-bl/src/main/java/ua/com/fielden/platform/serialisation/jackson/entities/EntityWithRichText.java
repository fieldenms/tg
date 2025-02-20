package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.RichText;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 */
@KeyType(String.class)
@DescTitle("Description")
public class EntityWithRichText extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    @Ignore
    private RichText richText;

    @Observable
    public EntityWithRichText setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

}
