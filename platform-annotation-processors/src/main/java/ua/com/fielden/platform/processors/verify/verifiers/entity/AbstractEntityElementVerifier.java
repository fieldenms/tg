package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import java.util.Optional;

/**
 * A base type for verifiers that verify entities, represented by {@link EntityElement}.
 *
 * @author TG Team
 */
public abstract class AbstractEntityElementVerifier implements IElementVerifier<EntityElement> {
    protected EntityFinder entityFinder;

    public AbstractEntityElementVerifier(final EntityFinder entityFinder) {
        this.entityFinder = entityFinder;
    }

    @Override
    public abstract Optional<ViolatingElement> verify(final EntityElement element);

}
