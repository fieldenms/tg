package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity for "domain tree representation" testing.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithoutKeyTitleAndWithKeyType extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected EntityWithoutKeyTitleAndWithKeyType() {
    }
}