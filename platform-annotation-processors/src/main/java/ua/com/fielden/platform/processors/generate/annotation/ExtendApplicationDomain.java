package ua.com.fielden.platform.processors.generate.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * Should be used to annotate a type that provides additional information for the generation of {@code ApplicationDomain}.
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface ExtendApplicationDomain {

    /**
     * A list of additional entity types that should be registered.
     */
    RegisterEntity[] entities() default {};

    /**
     * A helper class that represents instances of {@link ExtendApplicationDomain} on the level of {@link TypeMirror}.
     */
    static class Mirror {
        private final List<RegisterEntity.Mirror> entities = new LinkedList<>();

        private Mirror() {}

        public static Optional<Mirror> fromAnnotated(final AnnotatedConstruct annotated, final EntityFinder finder) {
            final ExtendApplicationDomain annot = annotated.getAnnotation(ExtendApplicationDomain.class);
            if (annot == null) {
                return Optional.empty();
            }

            final Mirror mirror = new Mirror();

            Stream.of(annot.entities())
                .map(atRegisterEntity -> RegisterEntity.Mirror.from(atRegisterEntity, finder))
                .forEach(atRegisterEntityMirror -> mirror.entities.add(atRegisterEntityMirror));

            return Optional.of(mirror);
        }

        public List<RegisterEntity.Mirror> entities() {
            return Collections.unmodifiableList(entities);
        }
    }

}
