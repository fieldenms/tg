package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity declaring property {@code desc}, but not metamodeled.
 *
 * @author TG Team
 */
@KeyType(String.class)
@KeyTitle("Key")
public class EntityWithPropertyDescWithoutMetaModel extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithPropertyDescWithoutMetaModel.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Desc", desc = "Description's description")
    private String desc;

    @Observable
    public EntityWithPropertyDescWithoutMetaModel setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

    public String getDesc() {
        return desc;
    }

}
