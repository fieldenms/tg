package ua.com.fielden.platform.processors.annotation;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
/*
 * We are forced to make this annotations repeatable instead of a simple one with array-typed elements (e.g., Class<?>[] for cls element)
 * because of a bug in com.sun.tools.javac.model.AnnotationProxyMaker that triggers an exception upon a call to
 * javax.lang.model.AnnotatedConstruct.getAnnotation(Class<A>), instead of expectedly throwing a MirroredTypesExceptionProxy.
 * Specifically, this occurs when an ErrorType is encountered, which might be caused by a deletion of a source java file.
 */
@Repeatable(ProcessedValue.Multi.class)
@Retention(CLASS) // preserve annotations in .class files to support stateful operations (e.g., regeneration of sources)
public @interface ProcessedValue {

    public static final Class<?> DEFAULT_CLS = Void.class;
    Class<?> cls() default Void.class; // a class literal must be used, so make sure to keep in sync with DEFAULT_CLS


    public static class Mirror {
        private final List<TypeMirror> classes = new LinkedList<>();

        private Mirror() {}

        public static Optional<Mirror> fromAnnotated(final AnnotatedConstruct annotated, final ElementFinder finder) {
            final ProcessedValue[] annots = annotated.getAnnotationsByType(ProcessedValue.class);
            if (annots.length == 0) {
                return Optional.empty();
            }

            final Mirror mirror = new Mirror();
            Stream.of(annots).map(at -> finder.getAnnotationElementValueOfClassType(at, ProcessedValue::cls))
                .forEach(tm -> mirror.classes.add(tm));

            return Optional.of(mirror);
        }

        public List<TypeMirror> classes() {
            return unmodifiableList(classes);
        }
    }

    /**
     * The containing annotation type to make {@link ProcessedValue} repeatable.
     *
     * @author TG Team
     */
    @Retention(CLASS)
    public static @interface Multi {
        ProcessedValue[] value();
    }

}
