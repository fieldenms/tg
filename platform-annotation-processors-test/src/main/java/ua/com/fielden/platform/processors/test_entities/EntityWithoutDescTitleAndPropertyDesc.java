package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity, which deliberately does not have {@code @DescTitle} present, nor declares property {@code desc}.
 *
 * @author TG Team
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
public class EntityWithoutDescTitleAndPropertyDesc extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithoutDescTitleAndPropertyDesc.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
}