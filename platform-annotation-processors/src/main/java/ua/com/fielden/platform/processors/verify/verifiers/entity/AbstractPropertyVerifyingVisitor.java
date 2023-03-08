package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.Optional;

import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IVerifyingVisitor;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Verifying visitor for entity properties.
 *
 * @author TG Team
 */
public abstract class AbstractPropertyVerifyingVisitor implements IVerifyingVisitor {
    protected EntityFinder entityFinder;

    public AbstractPropertyVerifyingVisitor(final EntityFinder entityFinder) {
        this.entityFinder = entityFinder;
    }

    /**
     * This operation is unsupported for this visitor.
     */
    @Override
    public final Optional<ViolatingElement> visitElement(final Element element) { 
        throw new UnsupportedOperationException();
    }

    /**
     * Visits a property of an entity.
     */
    public abstract Optional<ViolatingElement> visitProperty(final EntityElement entity, final PropertyElement property);

}
