package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.IVerifyingVisitor;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Verifying visitor for entity elements.
 *
 * @author TG Team
 */
public abstract class AbstractEntityVerifyingVisitor implements IVerifyingVisitor {
    protected EntityFinder entityFinder;

    public AbstractEntityVerifyingVisitor(final EntityFinder entityFinder) {
        this.entityFinder = entityFinder;
    }

    /**
     * Visits an element and forwards to {@link #visitEntity(EntityElement)} only if the element represents an entity.
     * Otherwise returns an empty optional.
     */
    @Override
    public final Optional<ViolatingElement> visitElement(final Element element) { 
        if (entityFinder.isEntityType(element.asType())) {
            return visitEntity(entityFinder.newEntityElement((TypeElement) element));
        }
        return Optional.empty();
    }

    public abstract Optional<ViolatingElement> visitEntity(final EntityElement entity);

}
