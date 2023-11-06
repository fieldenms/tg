package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * A test entity, representing an abstract super entity without annotation {@link KeyType}, but with {@link WithMetaModel}.
 *
 * @author TG Team
 *
 */
@WithMetaModel
public abstract class KeyType_AbstractSuperEntityWithoutKeyType<T extends Comparable> extends AbstractEntity<T> {

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private String prop1;

    @Observable
    public KeyType_AbstractSuperEntityWithoutKeyType<T> setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getProp1() {
        return prop1;
    }

}
