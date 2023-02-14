package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.Messager;
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

    /** Holds a memoized list of root entity elements in the current round. */
    private List<EntityElement> entities;

    public EntityRoundEnvironment(final RoundEnvironment roundEnv, final Messager messager, final EntityFinder entityFinder) {
        super(roundEnv, messager);
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
     * Returns a list containing entity elements that did not pass verification.
     * 
     * @param visitor
     * @return
     */
    public List<ViolatingElement> accept(final AbstractEntityVerifyingVisitor visitor) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listEntities().stream()
            .map(entity -> visitor.visitEntity(entity))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return unmodifiableList(violators);
    }

    /**
     * Accepts a verifying visitor for declared properties of an entity and applies it to each root entity element in this round.
     * Returns a list containing property elements that did not pass verification.
     * 
     * @param visitor
     * @return
     */
    public List<ViolatingElement> acceptDeclaredPropertiesVisitor(final AbstractPropertyVerifyingVisitor visitor) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listEntities().stream()
            .flatMap(entity -> entityFinder.streamDeclaredProperties(entity).map(prop -> t2(entity, prop)))
            .map(entityAndProp -> visitor.visitProperty(entityAndProp._1, entityAndProp._2))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return unmodifiableList(violators);
    }

}
