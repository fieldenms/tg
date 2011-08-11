package ua.com.fielden.platform.domain.tree;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Entity for "domain tree representation" testing (with 'abstract' modifier).
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
public class EntityWithoutKeyType extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected EntityWithoutKeyType() {
    }
}