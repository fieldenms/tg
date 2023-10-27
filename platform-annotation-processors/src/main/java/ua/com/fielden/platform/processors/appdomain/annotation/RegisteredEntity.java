package ua.com.fielden.platform.processors.appdomain.annotation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation that is used by the {@link ApplicationDomainProcessor} to record registered entity types in a generated {@code ApplicationDomain}.
 *
 * @author TG Team
 */
/*
 * We are forced to make this annotations repeatable instead of a simple one with array-typed elements (e.g., Class<?>[] for cls element)
 * because of a bug in com.sun.tools.javac.model.AnnotationProxyMaker that triggers an exception upon a call to
 * javax.lang.model.AnnotatedConstruct.getAnnotation(Class<A>), instead of expectedly throwing a MirroredTypesExceptionProxy.
 * Specifically, this occurs when an ErrorType is encountered, which might be caused by a deletion of a source java file.
 */
@Repeatable(RegisteredEntity.Multi.class)
@Retention(CLASS) // preserve annotations in .class files to support stateful operations (e.g., regeneration of sources)
@Target(TYPE)
public @interface RegisteredEntity {

    static boolean DEFAULT_EXTERNAL = false;

    // NOTE generic entity types are incompatbile and can't be assigned to this member, but there is no such need
    Class<? extends AbstractEntity<?>> value();

    boolean external() default DEFAULT_EXTERNAL;

    /**
     * A helper class that represents instances of {@link RegisteredEntity} on the level of {@link TypeMirror}.
     */
    static class Mirror {
        private final TypeMirror value;
        private final boolean external;

        private Mirror(final TypeMirror value, final boolean external) {
            this.value = value;
            this.external = external;
        }

        public static Mirror fromAnnotation(final RegisteredEntity annot, final EntityFinder finder) {
            final TypeMirror entityType = finder.getAnnotationElementValueOfClassType(annot, RegisteredEntity::value);
            return new Mirror(entityType, annot.external());
        }

        public static List<Mirror> fromAnnotated(final AnnotatedConstruct annotated, final EntityFinder finder) {
            final RegisteredEntity[] annots = annotated.getAnnotationsByType(RegisteredEntity.class);
            return Stream.of(annots).map(at -> Mirror.fromAnnotation(at, finder)).toList();
        }

        public TypeMirror value() {
            return value;
        }

        public boolean external() {
            return external;
        }
    }

    static class Builder {

        private Class<? extends AbstractEntity<?>> value;
        private boolean external;

        private Builder(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
        }

        public static Builder builder(final Class<? extends AbstractEntity<?>> value) {
            return new Builder(value);
        }

        public Builder setValue(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
            return this;
        }

        public Builder setExternal(final boolean external) {
            this.external = external;
            return this;
        }

        public RegisteredEntity build() {
            return new RegisteredEntity() {
                @Override public Class<RegisteredEntity> annotationType() { return RegisteredEntity.class; }

                @Override public Class<? extends AbstractEntity<?>> value() { return value; }
                @Override public boolean external() { return external; }

                @Override
                public boolean equals(final Object other) {
                    return this == other || (other instanceof final RegisteredEntity atOther) &&
                            Objects.equals(this.value(), atOther.value()) &&
                            Objects.equals(this.external(), atOther.external());
                }
            };
        }

    }

    /** The containing annotation type to make {@link RegisteredEntity} repeatable. */
    @Retention(CLASS)
    @Target(TYPE)
    static @interface Multi {
        RegisteredEntity[] value();
    }

}
