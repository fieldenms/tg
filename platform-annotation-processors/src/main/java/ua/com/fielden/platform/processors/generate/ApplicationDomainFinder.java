package ua.com.fielden.platform.processors.generate;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isStatic;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.streamDeclaredFields;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

/**
 * A collection of utility methods for operating on {@link Element} API in relation to {@link ApplicationDomain}.
 *
 * @author TG Team
 */
public record ApplicationDomainFinder(EntityFinder entityFinder) {

    /**
     * Returns a stream of entity elements that are registered with {@link ApplicationDomain} represented by the given type element.
     *
     * @param appDomainElt  type element representing the {@link ApplicationDomain} class
     * @return              stream of registered entity elements
     */
    public Stream<EntityElement> streamRegisteredEntities(final TypeElement appDomainElt) {
        return streamDeclaredFields(appDomainElt)
                .filter(elt -> isStatic(elt) && entityFinder.isSameType(elt.asType(), String.class))
                .map(elt -> entityFinder.findEntity((String) elt.getConstantValue()));
    }

    /**
     * Collects the elements of a stream produced by {@link #streamRegisteredEntities(TypeElement)} into a modifiable list.
     *
     * @param appDomainElt  type element representing the {@link ApplicationDomain} class
     * @return              list of registered entity elements
     */
    public List<EntityElement> findRegisteredEntities(final TypeElement appDomainElt) {
        return streamRegisteredEntities(appDomainElt).collect(toCollection(LinkedList::new));
    }

}