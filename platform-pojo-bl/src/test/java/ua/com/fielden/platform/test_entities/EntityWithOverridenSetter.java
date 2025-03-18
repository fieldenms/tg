package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 */
@KeyType(value = String.class)
public class EntityWithOverridenSetter extends ActivatableAbstractEntity<String> {

    private int witness = 0;

    public int getWitness() {
        return witness;
    }

    @Observable
    @Override
    protected EntityWithOverridenSetter setActive(final boolean active) {
        witness += 1;
        super.setActive(active);
        return this;
    }

}
