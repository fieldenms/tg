package ua.com.fielden.platform.audit;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides efficient access to some parts of the JavaPoet library.
 * <p>
 * It is recommended to use this facility instead of JavaPoet where possible, as this facility is designed to be more efficient
 * (for example, it performs caching of reusable elements).
 */
final class JavaPoet {

    private static final JavaPoet INSTANCE = new JavaPoet();

    public static JavaPoet getInstance() {
        return INSTANCE;
    }

    private JavaPoet() {};

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

    /**
     * Converts the named type to a Java reflection object, if such type exists; otherwise, returns {@code null}.
     * <p>
     * Limitations:
     * <ul>
     *   <li> Unsupported types: arrays, wildcards, type variables.
     *   <li> For parameterised type names, only the raw type is used.
     * </ul>
     */
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

}
