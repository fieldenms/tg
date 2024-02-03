package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity, which is metamodeled and extends an entity annotated with {@link DescTitle}, but not metamodeled.
 *
 * @author TG Team
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
public abstract class EntityWithoutDescTitle_extends_EntityWithDescTitleWithoutMetaModel extends EntityWithDescTitleWithoutMetaModel {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithoutDescTitle_extends_EntityWithDescTitleWithoutMetaModel.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
}
