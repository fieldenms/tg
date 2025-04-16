package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

@CompanionObject(TgNoteCo.class)
@MapEntityTo
@KeyType(String.class)
@KeyTitle("Note name")
public class TgNote extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgNote.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @Title("Note")
    private String text;

    public String getText() {
        return text;
    }

    @Observable
    public TgNote setText(final String text) {
        this.text = text;
        return this;
    }

}
