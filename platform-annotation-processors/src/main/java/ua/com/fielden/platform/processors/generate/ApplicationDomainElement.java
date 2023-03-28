package ua.com.fielden.platform.processors.generate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;

import ua.com.fielden.platform.processors.annotation.ProcessedValue;
import ua.com.fielden.platform.processors.metamodel.elements.AbstractForwardingTypeElement;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * Represents an element modeling the generated {@code ApplicationDomain} class.
 *
 * @author TG Team
 */
public class ApplicationDomainElement extends AbstractForwardingTypeElement {

    /** Currently registered entities */
    private final List<EntityElement> entities = new LinkedList<>();
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
        ProcessedValue.Mirror.fromAnnotated(element, entityFinder)
            .map(elt -> elt.classes()).orElseGet(() -> List.of()) // unpack the Optional
            .stream()
            .forEach(tm -> {
                if (tm.getKind() == TypeKind.ERROR) {
                    errorTypes.add((ErrorType) tm);
                } else {
                    final EntityElement entity = entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(tm));
                    entities.add(entity);
                }
            });
    }

    public List<EntityElement> entities() {
        return Collections.unmodifiableList(entities);
    }

    public List<ErrorType> errorTypes() {
        return Collections.unmodifiableList(errorTypes);
    }

}
