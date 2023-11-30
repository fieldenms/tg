package ua.com.fielden.platform.processors.appdomain;

import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.appdomain.annotation.RegisterEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;

/**
 * A helper class that represents instances of {@link ExtendApplicationDomain} on the level of {@link TypeMirror}.
 */
public final class ExtendApplicationDomainMirror {
    private final List<RegisterEntityMirror> entities;

    private ExtendApplicationDomainMirror(final Collection<RegisterEntityMirror> entities) {
        this.entities = new ArrayList<>(entities);
    }

    public static ExtendApplicationDomainMirror fromAnnotation(final ExtendApplicationDomain annot, final ElementFinder finder) {
        final List<RegisterEntityMirror> atRegisterEntityMirrors = Stream.of(annot.value())
                .map(atRegisterEntity -> RegisterEntityMirror.fromAnnotation(atRegisterEntity, finder))
                .toList();

        return new ExtendApplicationDomainMirror(atRegisterEntityMirrors);
    }

    public List<RegisterEntityMirror> entities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Returns a stream of entity elements corresponding to {@link ExtendApplicationDomain#value()}.
     * Unresolved types are excluded from the stream.
     */
    public Stream<EntityElement> streamEntityElements(EntityFinder entityFinder) {
        return entities.stream()
                .map(RegisterEntityMirror::value)
                .filter(tm -> tm.getKind() != TypeKind.ERROR)
                .map(tm -> entityFinder.newEntityElement(asTypeElementOfTypeMirror(tm)));
    }

    /**
     * A helper class that represents instances of {@link RegisterEntity} on the level of {@link TypeMirror}.
     */
    public static final class RegisterEntityMirror {
        private final TypeMirror value;

        private RegisterEntityMirror(final TypeMirror value) {
            this.value = value;
        }

        public static RegisterEntityMirror fromAnnotation(final RegisterEntity annot, final ElementFinder finder) {
            final TypeMirror entityType = finder.getAnnotationElementValueOfClassType(annot, RegisterEntity::value);
            return new RegisterEntityMirror(entityType);
        }

        public TypeMirror value() {
            return value;
        }
    }

}

