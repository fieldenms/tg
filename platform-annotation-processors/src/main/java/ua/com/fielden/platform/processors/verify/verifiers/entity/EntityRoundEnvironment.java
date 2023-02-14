package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * A round environment wrapper that's designed to operate on entity elements.
 *
 * @author TG Team
 */
public class EntityRoundEnvironment extends AbstractRoundEnvironment {
    private EntityFinder entityFinder;

    private List<EntityElement> entities;

    public EntityRoundEnvironment(final RoundEnvironment roundEnv, final EntityFinder entityFinder) {
        super(roundEnv);
        this.entityFinder = entityFinder;
    }

    /**
     * Returns a list of entity elements being processed in the current round. The result is memoized.
     */
    public List<EntityElement> listEntities() {
        if (entities == null) {
            entities = roundEnv.getRootElements().stream()
                    .map(elt -> (TypeElement) elt)
                    .filter(el -> entityFinder.isEntityType(el.asType()))
                    .map(el -> entityFinder.newEntityElement(el))
                    .toList();
        }
        return entities;
    }

    /**
     * Accepts an entity verifying visitor and applies it to each root entity element in this round.
     * Returns a list containing elements that did not pass verification.
     * 
     * @param visitor
     * @return
     */
    public List<ViolatingElement> accept(final AbstractEntityVerifyingVisitor visitor) {
        return listEntities().stream()
            .map(entity -> visitor.visitEntity(entity))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

}
