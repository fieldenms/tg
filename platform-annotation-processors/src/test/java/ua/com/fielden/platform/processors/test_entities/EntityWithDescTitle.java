package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity, which has {@code @DescTitle} present. 
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class EntityWithDescTitle extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithDescTitle.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
}
