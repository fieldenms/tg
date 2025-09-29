package ua.com.fielden.platform.processors.minheritance;

import ua.com.fielden.platform.entity.annotation.Extends;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;

/// Represents instances of [Extends] on the level of [TypeMirror].
///
record ExtendsMirror(List<EntityMirror> value, String name) {

    public static ExtendsMirror fromAnnotation(final Extends annot, final ElementFinder finder) {
        final var value = Arrays.stream(annot.value())
                .map(atEntity -> EntityMirror.fromAnnotation(atEntity, finder))
                .toList();

        return new ExtendsMirror(value, annot.name());
    }

    ///
    /// Represents instances of [Extends.Entity] on the level of [TypeMirror].
    record EntityMirror(TypeMirror value, String[] exclude) {

        public static EntityMirror fromAnnotation(final Extends.Entity annot, final ElementFinder finder) {
            return new EntityMirror(finder.getAnnotationElementValueOfClassType(annot, Extends.Entity::value),
                                    annot.exclude());
        }

    }

}
