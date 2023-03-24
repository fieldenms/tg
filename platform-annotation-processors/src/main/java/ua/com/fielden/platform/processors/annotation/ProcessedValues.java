package ua.com.fielden.platform.processors.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Retention;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

/**
 * Annotation that acts as a general-purpose value container that should be processed by an annotation processor.
 * The motivation behind it is to compensate for the inability of annotation processors to analyse method bodies and class/instance initialisers.
 * <p>
 * <b><i>NOTE</i></b>: Currently, only {@link Class} values can be contained. More value types can be supported if the need arises.
 *
 * @author TG Team
 */
@Retention(SOURCE)
public @interface ProcessedValues {

    Class<?>[] classes() default {};


    public static class Mirror {
        private final List<TypeMirror> classes = new LinkedList<>();

        private Mirror() {}

        public static Optional<Mirror> fromAnnotated(final AnnotatedConstruct annotated, final ElementFinder finder) {
            final ProcessedValues annot = annotated.getAnnotation(ProcessedValues.class);
            if (annot == null) {
                return Optional.empty();
            }

            final Mirror elt = new Mirror();
            elt.classes.addAll(finder.getAnnotationElementValueOfClassArrayType(() -> annot.classes()));

            return Optional.of(elt);
        }

        public List<TypeMirror> classes() {
            return unmodifiableList(classes);
        }
    }

}
