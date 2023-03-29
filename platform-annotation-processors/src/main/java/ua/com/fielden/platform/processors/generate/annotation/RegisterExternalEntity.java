package ua.com.fielden.platform.processors.generate.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

/**
 * An annotation that should be used to register 3rd-party entities with a generated {@code ApplicationDomain}.
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Repeatable(RegisterExternalEntity.Multi.class)
@Retention(SOURCE)
@Target(TYPE)
public @interface RegisterExternalEntity {

    Class<? extends AbstractEntity<?>> value();

    /**
     * A helper class that represents instances of {@link RegisterExternalEntity} on the level of {@link TypeMirror}.
     */
    public static class Mirror {
        private final List<TypeMirror> values = new LinkedList<>();

        private Mirror() {}

        public static Optional<Mirror> fromAnnotated(final AnnotatedConstruct annotated, final ElementFinder finder) {
            final RegisterExternalEntity[] annots = annotated.getAnnotationsByType(RegisterExternalEntity.class);
            if (annots.length == 0) {
                return Optional.empty();
            }

            final Mirror mirror = new Mirror();
            Stream.of(annots).map(at -> finder.getAnnotationElementValueOfClassType(at, RegisterExternalEntity::value))
                .forEach(tm -> mirror.values.add(tm));

            return Optional.of(mirror);
        }

        public List<TypeMirror> values() {
            return Collections.unmodifiableList(values);
        }
    }

    /**
     * The containing annotation type to make {@link RegisterExternalEntity} repeatable.
     */
    @Retention(SOURCE)
    @Target(TYPE)
    public static @interface Multi {
        RegisterExternalEntity[] value();
    }

}
