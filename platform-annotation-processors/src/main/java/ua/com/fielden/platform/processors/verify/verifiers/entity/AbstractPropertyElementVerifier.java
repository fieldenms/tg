package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.types.tuples.T2;

import javax.lang.model.type.TypeKind;
import java.util.Optional;

/**
 * A base type for verifiers that verify entity properties, represented by {@link PropertyElement}.
 *
 * @author TG Team
 */
public abstract class AbstractPropertyElementVerifier implements IElementVerifier<T2<EntityElement, PropertyElement>> {
    protected EntityFinder entityFinder;

    public AbstractPropertyElementVerifier(final EntityFinder entityFinder) {
        this.entityFinder = entityFinder;
    }

    /**
     * Delegates to {@link #verifyProperty(EntityElement, PropertyElement)}.
     */
    @Override
    public final Optional<ViolatingElement> verify(final T2<EntityElement, PropertyElement> element) {
        return verifyProperty(element._1, element._2);
    }

    /**
     * Verifies a property of an entity.
     */
    public abstract Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property);

    protected boolean hasErrorType(PropertyElement property) {
        return property.getType().getKind() == TypeKind.ERROR;
    }

}
