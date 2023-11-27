package ua.com.fielden.platform.processors.appdomain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.appdomain.annotation.RegisteredEntity;
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
    private final List<EntityElement> externalEntities = new LinkedList<>();
    /** Entity types that cannot be located (e.g., due to removal/renaming) */
    private final List<ErrorType> errorTypes = new LinkedList<>();

    private ApplicationDomainElement(final TypeElement element) {
        super(element);
    }

    public ApplicationDomainElement(final TypeElement element, final EntityFinder entityFinder) {
        super(element);
        init(element, entityFinder);
    }

    private void init(final TypeElement element, final EntityFinder entityFinder) {
        RegisteredEntityMirror.fromAnnotated(element, entityFinder)
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

    public List<ErrorType> errorTypes() {
        return Collections.unmodifiableList(errorTypes);
    }

    /**
     * A helper class that represents instances of {@link RegisteredEntity} on the level of {@link TypeMirror}.
     */
    private static class RegisteredEntityMirror {
        private final TypeMirror value;
        private final boolean external;

        private RegisteredEntityMirror(final TypeMirror value, final boolean external) {
            this.value = value;
            this.external = external;
        }

        public static RegisteredEntityMirror fromAnnotation(final RegisteredEntity annot, final EntityFinder finder) {
            final TypeMirror entityType = finder.getAnnotationElementValueOfClassType(annot, RegisteredEntity::value);
            return new RegisteredEntityMirror(entityType, annot.external());
        }

        public static List<RegisteredEntityMirror> fromAnnotated(final AnnotatedConstruct annotated, final EntityFinder finder) {
            final RegisteredEntity[] annots = annotated.getAnnotationsByType(RegisteredEntity.class);
            return Stream.of(annots).map(at -> RegisteredEntityMirror.fromAnnotation(at, finder)).toList();
        }

        public TypeMirror value() {
            return value;
        }

        public boolean external() {
            return external;
        }
    }

}
