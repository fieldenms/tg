package ua.com.fielden.platform.processors.appdomain.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;

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

    /** The containing annotation type to make {@link RegisteredEntity} repeatable. */
    @Retention(CLASS)
    @Target(TYPE)
    static @interface Multi {
        RegisteredEntity[] value();
    }

}
