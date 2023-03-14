package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.Optional;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

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

    /**
     * Verifies an element and forwards to {@link #verifyEntity(EntityElement)} only if the element represents an entity.
     * Otherwise returns an empty optional.
     */
    @Override
    public final Optional<ViolatingElement> verify(final EntityElement element) {
        if (entityFinder.isEntityType(element.asType())) {
            return verifyEntity(entityFinder.newEntityElement(element));
        }
        return Optional.empty();
    }

    public abstract Optional<ViolatingElement> verifyEntity(final EntityElement entity);

}