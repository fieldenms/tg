package ua.com.fielden.platform.audit;

import com.squareup.javapoet.*;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/// Provides efficient access to some parts of the JavaPoet library.
///
/// It is recommended to use this facility instead of JavaPoet where possible, as this facility is designed to be more efficient
/// For example, it performs caching of reusable elements.
///
final class JavaPoet {

    private final Map<Type, TypeName> typeNameCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, AnnotationSpec> markerAnnotationCache = new ConcurrentHashMap<>();

    public ClassName getClassName(final Class<?> type) {
        return (ClassName) typeNameCache.computeIfAbsent(type, ClassName::get);
    }

    public TypeName getTypeName(final Type type) {
        return typeNameCache.computeIfAbsent(type, TypeName::get);
    }

    public AnnotationSpec getAnnotation(final Class<? extends Annotation> annotationType) {
        return markerAnnotationCache.computeIfAbsent(annotationType, k -> AnnotationSpec.builder(k).build());
    }

    /// Converts the named type to a Java reflection object, if such type exists.
    /// Otherwise, returns `null`.
    ///
    /// Limitations:
    /// -  Unsupported types: arrays, wildcards, type variables.
    /// -  For parameterised type names, only the raw type is used.
    ///
    public @Nullable Class<?> reflectType(final TypeName typeName) {
        class $ {
            static final Map<TypeName, Class<?>> PRIMITIVES = Map.of(
                    TypeName.VOID, void.class,
                    TypeName.BOOLEAN, boolean.class,
                    TypeName.BYTE, byte.class,
                    TypeName.SHORT, short.class,
                    TypeName.INT, int.class,
                    TypeName.LONG, long.class,
                    TypeName.CHAR, char.class,
                    TypeName.FLOAT, float.class,
                    TypeName.DOUBLE, double.class);
        }

        if (typeName.isPrimitive()) {
            return $.PRIMITIVES.get(typeName);
        }
        else {
            return switch (typeName) {
                case ClassName className -> {
                    try {
                        yield Class.forName(className.reflectionName(), false, JavaPoet.class.getClassLoader());
                    } catch (final ClassNotFoundException e) {
                        yield null;
                    }
                }
                case ParameterizedTypeName paramTypeName -> reflectType(paramTypeName.rawType);
                default -> throw new UnsupportedOperationException("Type name [%s] cannot be reflected.");
            };
        }
    }

    /// Returns the contents of the specified java file.
    ///
    public static String readJavaFile(final JavaFile javaFile) {
        final var sb = new StringBuilder();
        try {
            javaFile.writeTo(sb);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static Optional<CodeBlock> annotationMember(final AnnotationSpec annotSpec, final String memberName) {
        return Optional.ofNullable(annotSpec.members.get(memberName)).map(List::getFirst);
    }

    /// Returns a class name for the specified fully-qualified name which must name a top-level class.
    /// Although, this requirement cannot be enforced.
    ///
    public static ClassName topLevelClassName(final CharSequence fqn) {
        final var dotIdx = StringUtils.lastIndexOf(fqn, '.');
        if (dotIdx == -1) {
            return ClassName.get("", fqn.toString());
        }
        else {
            return ClassName.get(fqn.subSequence(0, dotIdx).toString(), fqn.subSequence(dotIdx + 1, fqn.length()).toString());
        }
    }

}
