package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Test entity type with key of type {@link String}, which is implicit (i.e., there is no explicitly declared field {@code key} in this class).
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithImplicitStringKey extends AbstractEntity<String> {

}
