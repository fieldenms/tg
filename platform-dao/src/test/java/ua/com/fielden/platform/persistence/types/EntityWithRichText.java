package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.RichText;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(EntityWithRichTextCo.class)
public class EntityWithRichText extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private RichText text;

    public RichText getText() {
        return text;
    }

    @Observable
    public EntityWithRichText setText(final RichText text) {
        this.text = text;
        return this;
    }

}
