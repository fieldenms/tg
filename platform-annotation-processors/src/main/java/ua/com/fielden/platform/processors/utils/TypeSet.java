package ua.com.fielden.platform.processors.utils;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

import javax.lang.model.type.*;
import javax.lang.model.util.TypeKindVisitor14;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.classForPrimitiveType;

/**
 * An immutable set that can store instances of {@link Class} and {@link TypeMirror}, and provides an efficient implementation
 * of {@link Set#contains(Object)}.
 * <p>
 * The underlying common representation of stored instances is the canonical (qualified) name of a type.
 * Therefore, only a subset of {@link TypeMirror} types can be stored -- those that have a canonical name. An attempt
 * to store a type mirror without a canonical name will result in a runtime exception. However, the result of
 * {@link #contains(Object)} on such a type mirror will simply return {@code false}.
 * <p>
 * Although this set type is defined as a set of {@link Object}, the element type should be treated as a sum type of
 * {@link Class} and {@link TypeMirror}.
 */
public final class TypeSet implements Set<Object> {

    /**
     * <b>WARNING</b>: builders must not be reused because they are mutable.
     * <p>
     * Builders are accessed through {@link TypeSet#build(Consumer)}.
     */
    static final class Builder {
        private final HashSet<String> names = new HashSet<>();
        private final HashSet<Object> elements = new HashSet<>();

        private Builder() {}

        public Builder add(final TypeMirror typeMirror) {
            final String name = getCanonicalName(typeMirror);
            if (name == null) {
                throw new IllegalArgumentException("TypeMirror [%s] has no canonical name, cannot be added to [%s]"
                                                   .formatted(typeMirror, TypeSet.class.getTypeName()));
            }
            final boolean added = names.add(name);
            if (added) {
                elements.add(typeMirror);
            }
            return this;
        }

        public Builder addTypeMirrors(final Iterable<? extends TypeMirror> typeMirrors) {
            typeMirrors.forEach(this::add);
            return this;
        }

        public Builder add(final Class<?> type) {
            final boolean added = names.add(type.getCanonicalName());
            if (added) {
                elements.add(type);
            }
            return this;
        }

        public Builder addClasses(final Iterable<? extends Class<?>> classes) {
            classes.forEach(this::add);
            return this;
        }

        private TypeSet build() {
            return new TypeSet(names, elements);
        }
    }

    public static TypeSet build(final Consumer<Builder> fn) {
        final var builder = new Builder();
        fn.accept(builder);
        return builder.build();
    }

    public static TypeSet ofClasses(final Iterable<? extends Class<?>> classes) {
        return new Builder().addClasses(classes).build();
    }

    public static TypeSet ofClasses(final Class<?>... classes) {
        return new Builder().addClasses(Arrays.asList(classes)).build();
    }

    private final HashSet<String> names;
    private final HashSet<Object> elements;

    private TypeSet(final HashSet<String> names, final HashSet<Object> elements) {
        this.names = names;
        this.elements = elements;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return switch (o) {
            case Class<?> type -> contains(type);
            case TypeMirror tm -> contains(tm);
            case null, default -> false;
        };
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return collection.stream().allMatch(this::contains);
    }

    public boolean contains(final Class<?> type) {
        return names.contains(type.getCanonicalName());
    }

    public boolean contains(final TypeMirror typeMirror) {
        final String name = typeMirror.accept(canonicalNameVisitor, null);
        return name != null && names.contains(name);
    }

    public Stream<String> streamTypeNames() {
        return names.stream();
    }

    public <T> Stream<T> stream(final Converter<T> converter) {
        return elements.stream()
                .map(elt -> elt instanceof TypeMirror tm ? converter.convert(tm) : converter.convert((Class<?>) elt));
    }

    public interface Converter<T> {
        T convert(Class<?> type);
        T convert(TypeMirror typeMirror);
    }

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(final Object object) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    public boolean add(final Class<?> type) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    public boolean add(final TypeMirror typeMirror) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    @Override
    public boolean addAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Instances of [%s] are immutable.".formatted(this.getClass().getTypeName()));
    }

    private static @Nullable String getCanonicalName(final TypeMirror typeMirror) {
        return typeMirror.accept(canonicalNameVisitor, null);
    }

    /**
     * Returns the canonical name of a type mirror if it has one, otherwise returns {@code null}.
     */
    private static final TypeKindVisitor14<String, Void> canonicalNameVisitor = new TypeKindVisitor14<>() {
        @Override
        protected String defaultAction(TypeMirror e, Void ignore) {
            return null;
        }

        @Override
        public String visitPrimitive(final PrimitiveType t, final Void ignore) {
            return classForPrimitiveType(t).getCanonicalName();
        }

        // handle the Void type
        @Override
        public String visitNoType(final NoType t, final Void ignore) {
            if (t.getKind() == TypeKind.VOID) {
                return void.class.getCanonicalName();
            }
            return null;
        }

        @Override
        public String visitArray(ArrayType t, Void ignore) {
            final String componentName = t.getComponentType().accept(this, ignore);
            return componentName == null ? null : componentName + "[]";
        }

        @Override
        public String visitDeclared(final DeclaredType t, final Void ignore) {
            return ElementFinder.asTypeElement(t).getQualifiedName().toString();
        }
    };

}
