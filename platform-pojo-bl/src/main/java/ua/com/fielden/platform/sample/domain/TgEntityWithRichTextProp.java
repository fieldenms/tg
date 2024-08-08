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
@CompanionObject(TgEntityWithRichTextPropCo.class)
@MapEntityTo
@KeyTitle("Rich Text Key")
@DescTitle("Rich Text Description")
public class TgEntityWithRichTextProp extends AbstractPersistentEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgEntityWithRichTextProp.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Rich Text Prop", desc = "Rich Text Property")
    private String richTextProp;

    public String getRichTextProp() {
        return richTextProp;
    }

    @Observable
    public TgEntityWithRichTextProp setRichTextProp(final String richTextProp) {
        this.richTextProp = richTextProp;
        return this;
    }
}
