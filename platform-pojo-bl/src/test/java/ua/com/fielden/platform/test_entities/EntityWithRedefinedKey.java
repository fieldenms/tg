package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithRedefinedKey extends ActivatableAbstractEntity<String> {

    private int setterWitness = 0; // a helper field to count key assignments
    private int getterWitness = 0; // a helper field to count key accesses

    @IsProperty
    @MapTo
    @Title(value = "Key", desc = "Redefined key; this happens when custom BCE/ACE handlers are required.")
    private String key;

    public String getKey() {
        getterWitness += 1;
        return key;
    }

    @Observable
    public EntityWithRedefinedKey setKey(final String key) {
        setterWitness += 1;
        this.key = key;
        return this;
    }

    public int getSetterWitness() {
        return setterWitness;
    }

    public int getGetterWitness() {
        return getterWitness;
    }

}
