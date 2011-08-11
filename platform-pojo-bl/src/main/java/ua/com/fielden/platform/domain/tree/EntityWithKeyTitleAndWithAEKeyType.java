package ua.com.fielden.platform.domain.tree;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(EntityWithNormalNature.class)
public class EntityWithKeyTitleAndWithAEKeyType extends AbstractEntity<EntityWithNormalNature> {
    private static final long serialVersionUID = 1L;

    protected EntityWithKeyTitleAndWithAEKeyType() {
    }
}