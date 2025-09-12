package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.Pair;

/**
 * Master entity object.
 *
 * @author TG Team
 */
@KeyType(String.class)
@CompanionObject(TgEntityWithRichTextRefCo.class)
@MapEntityTo
@KeyTitle(value = "Key", desc = "Key Description")
@DescTitle(value = "Description", desc = "Entity Description")
public class TgEntityWithRichTextRef extends AbstractPersistentEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgEntityWithRichTextRef.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Rich Text Ref", desc = "Reference on entity with rich text property")
    private TgEntityWithRichTextProp richTextRef;

    @Observable
    public TgEntityWithRichTextRef setRichTextRef(final TgEntityWithRichTextProp richTextRef) {
        this.richTextRef = richTextRef;
        return this;
    }

    public TgEntityWithRichTextProp getRichTextRef() {
        return richTextRef;
    }

}
