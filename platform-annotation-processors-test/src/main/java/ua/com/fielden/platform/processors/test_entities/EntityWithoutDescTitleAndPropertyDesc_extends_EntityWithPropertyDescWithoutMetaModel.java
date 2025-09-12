package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity without {@link DescTitle} and not declaring property {@code desc} that extends an entity declaring {@code desc}.
 *
 * @author TG Team
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
public class EntityWithoutDescTitleAndPropertyDesc_extends_EntityWithPropertyDescWithoutMetaModel extends EntityWithPropertyDescWithoutMetaModel {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithoutDescTitleAndPropertyDesc_extends_EntityWithPropertyDescWithoutMetaModel.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

}