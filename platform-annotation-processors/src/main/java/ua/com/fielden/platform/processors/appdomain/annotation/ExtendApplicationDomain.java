package ua.com.fielden.platform.processors.appdomain.annotation;

import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Should be used to annotate a type (<i>extension point</i>) that provides additional information for the generation of {@code ApplicationDomain}.
 * <p>
 * <b>At most one extension point is allowed.</b>
 *
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
        private final List<RegisterEntity.Mirror> entities;

        private Mirror(final Collection<RegisterEntity.Mirror> entities) {
            this.entities = new ArrayList<>(entities);
        }

        public static Mirror from(final ExtendApplicationDomain annot, final ElementFinder finder) {
            final List<RegisterEntity.Mirror> atRegisterEntityMirrors = Stream.of(annot.entities())
                    .map(atRegisterEntity -> RegisterEntity.Mirror.from(atRegisterEntity, finder))
                    .toList();

            return new Mirror(atRegisterEntityMirrors);
        }

        public List<RegisterEntity.Mirror> entities() {
            return Collections.unmodifiableList(entities);
        }
    }

}
