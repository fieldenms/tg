package ua.com.fielden.platform.processors.appdomain;

import ua.com.fielden.platform.processors.appdomain.annotation.RegisteredEntity;
import ua.com.fielden.platform.processors.metamodel.elements.AbstractForwardingTypeElement;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents an element modeling the generated {@code ApplicationDomain} class.
 *
 * @author TG Team
 */
public class ApplicationDomainElement extends AbstractForwardingTypeElement {

    /** Currently registered entities */
    private final List<EntityElement> entities = new LinkedList<>();
    private final List<EntityElement> externalEntities = new LinkedList<>();
    /** Entity types that cannot be located (e.g., due to removal/renaming) */
    private final List<ErrorType> errorTypes = new LinkedList<>();

    private final PackageElement packageElement;

    private ApplicationDomainElement(final TypeElement element) {
        super(element);
        this.packageElement = null;
    }

    public ApplicationDomainElement(final TypeElement element, final EntityFinder entityFinder) {
        super(element);
        this.packageElement = entityFinder.getPackageOfTypeElement(element);
        init(element, entityFinder);
    }

    private void init(final TypeElement element, final EntityFinder entityFinder) {
        RegisteredEntity.Mirror.fromAnnotated(element, entityFinder)
            .stream()
            .forEach(mirr -> {
                final TypeMirror entityType = mirr.value();
                if (entityType.getKind() == TypeKind.ERROR) {
                    errorTypes.add((ErrorType) entityType);
                } else {
                    final EntityElement entity = entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(entityType));
                    if (mirr.external()) {
                        externalEntities.add(entity);
                    } else {
                        entities.add(entity);
                    }
                }
            });
    }

    public List<EntityElement> entities() {
        return Collections.unmodifiableList(entities);
    }

    public List<EntityElement> externalEntities() {
        return Collections.unmodifiableList(externalEntities);
    }

    public Stream<EntityElement> streamAllEntities() {
        return Stream.concat(entities.stream(), externalEntities.stream());
    }

    public List<ErrorType> errorTypes() {
        return Collections.unmodifiableList(errorTypes);
    }

}
