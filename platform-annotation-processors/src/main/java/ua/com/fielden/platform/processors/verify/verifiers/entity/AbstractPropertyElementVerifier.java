package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.Optional;

import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * A base type for verifiers that verify entity properties, represented by {@link PropertyElement}.
 *
 * @author TG Team
 */
public abstract class AbstractPropertyElementVerifier implements IElementVerifier {
    protected EntityFinder entityFinder;

    public AbstractPropertyElementVerifier(final EntityFinder entityFinder) {
        this.entityFinder = entityFinder;
    }

    /**
     * This operation is unsupported for this verifier.
     */
    @Override
    public final Optional<ViolatingElement> verify(final Element element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Verifies a property of an entity.
     */
    public abstract Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property);

}