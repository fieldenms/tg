package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.RichText;

import static ua.com.fielden.platform.types.RichText.CORE_TEXT;

@KeyType(String.class)
@MapEntityTo
@CompanionObject(EntityWithRichTextCo.class)
@DescTitle("Description")
public class EntityWithRichText extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private RichText text;

    @IsProperty
    @MapTo
    private String plainText;

    @IsProperty
    @Calculated("text" + '.' + CORE_TEXT)
    private String desc;

    public String getPlainText() {
        return plainText;
    }

    @Observable
    public EntityWithRichText setPlainText(final String plainText) {
        this.plainText = plainText;
        return this;
    }

    public RichText getText() {
        return text;
    }

    @Observable
    public EntityWithRichText setText(final RichText text) {
        this.text = text;
        return this;
    }

    @Observable
    @Override
    public EntityWithRichText setKey(final String key) {
        super.setKey(key);
        return this;
    }

    @Observable
    public EntityWithRichText setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    public String getDesc() {
        return desc;
    }

}
