package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
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
@DisplayDescription
public class TgEntityWithRichTextProp extends AbstractPersistentEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgEntityWithRichTextProp.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @Title(value = "Rich Text Prop", desc = "Rich Text Property")
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private RichText richTextProp;

    public RichText getRichTextProp() {
        return richTextProp;
    }

    @Observable
    public TgEntityWithRichTextProp setRichTextProp(final RichText richTextProp) {
        this.richTextProp = richTextProp;
        return this;
    }

    @Override
    @Observable
    public TgEntityWithRichTextProp setDesc(String desc) {
        return super.setDesc(desc);
    }

}
