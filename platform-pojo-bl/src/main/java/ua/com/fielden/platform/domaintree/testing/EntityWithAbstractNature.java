package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity for "domain tree representation" testing (with 'abstract' modifier).
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public abstract class EntityWithAbstractNature extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected EntityWithAbstractNature() {
    }
}
