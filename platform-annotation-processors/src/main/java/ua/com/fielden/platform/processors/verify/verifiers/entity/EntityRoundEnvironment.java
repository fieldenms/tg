package ua.com.fielden.platform.processors.verify.verifiers.entity;

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
public class EntityRoundEnvironment extends AbstractRoundEnvironment<EntityElement, AbstractEntityElementVerifier> {
    private final EntityFinder entityFinder;

    /** Holds root entity elements in the current round. */
    private List<EntityElement> entities;
    /** Holds root <b>union entity</b> elements in the current round. */
    private List<EntityElement> unionEntities;

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
     * Returns a list of union entity elements being processed in the current round. The result is memoized.
     */
    public List<EntityElement> listUnionEntities() {
        if (unionEntities == null) {
            unionEntities = listEntities().stream()
                    .filter(elt -> entityFinder.isUnionEntityType(elt.asType()))
                    .toList();
        }
        return unionEntities;
    }

    /**
     * Accepts an entity verifier and applies it to each root entity element in this round.
     * Returns a list containing entity elements that did not pass verification.
     *
     * @param verifier
     * @return
     */
    @Override
    public List<ViolatingElement> findViolatingElements(final AbstractEntityElementVerifier verifier) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listEntities().stream()
            .map(entity -> verifier.verify(entity))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return violators;
    }

    /**
     * Accepts a verifier for union entities and applies it to each root union entity element in this round.
     * Returns a list containing entity elements that did not pass verification.
     */
    public List<ViolatingElement> findViolatingUnionEntities(final AbstractEntityElementVerifier verifier) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listUnionEntities().stream()
            .map(entity -> verifier.verify(entity))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return violators;
    }

    /**
     * Accepts a verifier for declared properties of an entity and applies it to each root entity element in this round.
     * Returns a list containing property elements that did not pass verification.
     *
     * @param verifier
     * @return
     */
    public List<ViolatingElement> findViolatingDeclaredProperties(final AbstractPropertyElementVerifier verifier) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listEntities().stream()
            .flatMap(entity -> entityFinder.streamDeclaredProperties(entity).map(prop -> t2(entity, prop)))
            .map(entityAndProp -> verifier.verifyProperty(entityAndProp._1, entityAndProp._2))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return violators;
    }

    /**
     * Accepts a verifier for declared properties of a union entity and applies it to each root union entity element in this round.
     * Returns a list containing property elements that did not pass verification.
     *
     * @param verifier
     * @return
     */
    public List<ViolatingElement> findViolatingUnionEntityDeclaredProperties(final AbstractPropertyElementVerifier verifier) {
        final List<ViolatingElement> violators = new LinkedList<>();

        listUnionEntities().stream()
            .flatMap(entity -> entityFinder.streamDeclaredProperties(entity).map(prop -> t2(entity, prop)))
            .map(entityAndProp -> verifier.verifyProperty(entityAndProp._1, entityAndProp._2))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return violators;
    }

}