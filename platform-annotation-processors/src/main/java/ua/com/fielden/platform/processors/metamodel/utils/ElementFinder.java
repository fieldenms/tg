package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.iterate;
import static ua.com.fielden.platform.utils.StreamUtils.stopAfter;

import java.lang.annotation.Annotation;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.metamodel.elements.AbstractForwardingElement;
import ua.com.fielden.platform.processors.metamodel.elements.utils.TypeElementCache;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;
import ua.com.fielden.platform.utils.StreamUtils;

/**
 * A collection of utility methods for operating on elements and types, an extension of {@link Elements} and {@link Types}. 
 *
 * @author TG Team
 */
public class ElementFinder {
    public static final Class<?> ROOT_CLASS = Object.class;

    public final Elements elements;
    public final Types types;

    public ElementFinder(final Elements elements, final Types types) {
        if (elements == null) {
            throw new ElementFinderException("Argument elements cannot be null.");
        }
        if (types == null) {
            throw new ElementFinderException("Argument types cannot be null.");
        }

        this.elements = elements;
        this.types = types;
    }

    /**
     * Similar to {@link #getTypeElement(String)}. Uses {@link Class#getCanonicalName()} to obtain the name.
     * <p>
     * Presumably, this method should never throw, since the passed in {@link Class} instance guarantees the existence of that type. 
     * 
     * @param clazz
     * @return type element representing {@code clazz}
     * @throws ElementFinderException if no coresponding type element was found
     */
    public TypeElement getTypeElement(final Class<?> clazz) {
        return getTypeElement(clazz.getCanonicalName());
    }

    /**
     * Similar to {@link Elements#getTypeElement(CharSequence)} with caching of results.
     * <p>
     * If no corespodning type element was found, then a runtime exception is thrown.
     * In case of a multi-module application where there are multiple classes with the same canonical name, the first match is returned.
     * 
     * @see TypeElementCache
     * @param name canonical name of the element to be found
     * @return
     * @throws ElementFinderException if no coresponding type element was found
     */
    public TypeElement getTypeElement(final String name) {
        final TypeElement elt = TypeElementCache.getTypeElement(elements, name);
        if (elt == null) {
            throw new ElementFinderException("No type element was found for type [%s]".formatted(name));
        }
        return elt;
    }

    /**
     * Tests whether the type element and class represent the same type.
     * <p>
     * The comparison is based on the canonical name of the underlying type.
     * A local class, local interface, or anonymous class does not have a canonical name.
     * <p>
     * <b>NOTE</b>: For sake of simplicity, this method is incapable of differentiating between application modules.
     * 
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-6.html#jls-6.7">Java SE16 Language Specification - Fully Qualified Names and Canonical Names</a>
     * @param element
     * @param clazz
     */
    public static boolean isSameType(final TypeElement element, final Class<?> clazz) {
        if (element == null || clazz == null) {
            throw new EntityMetaModelException("Neither typeElement nor type arguments can be null.");
        }
        return element.getQualifiedName().toString().equals(clazz.getCanonicalName());
    }

    /**
     * Returns the immediate superclass of a type element if there is one.
     * An empty optional is returned if the type element represents an interface type or the {@link Object} class.
     */
    public static Optional<TypeElement> findSuperclass(final TypeElement element) {
        final TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return Optional.empty();
        }
        return Optional.of(asTypeElementOfTypeMirror(superclass));
    }

    /**
     * Returns an ordered stream of all superclasses of the type element.
     * The type hierarchy is traversed until either {@code rootType} or an interface type is reached.
     * <p>
     * If the type element is not a subtype of {@code rootType}, then an empty stream is returned.
     * <p>
     * The type element representing {@code rootType} is included in the stream only if it's a class type.
     *
     * @param typeElement
     * @param rootType
     * @return
     */
    public Stream<TypeElement> streamSuperclasses(final TypeElement typeElement, final Class<?> rootType) {
        if (!isSubtype(typeElement.asType(), rootType)) {
            return Stream.empty();
        }
        return stopAfter(
                iterate(Optional.of(typeElement), Optional::isPresent, elt -> elt.flatMap(ElementFinder::findSuperclass)).map(Optional::get),
                elt -> isSameType(elt, rootType))
                .skip(1); // drop the typeElement itself
    }

    /**
     * Like {@link #streamSuperclasses(TypeElement, Class)} with {@code rootType} equal to {@code Object}. 
     */
    public Stream<TypeElement> streamSuperclasses(final TypeElement typeElement) {
        return streamSuperclasses(typeElement, ROOT_CLASS);
    }

    /**
     * Collects the elements of {@link #streamSuperclasses(TypeElement, Class)} into a list. 
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement, final Class<?> rootType) {
        return streamSuperclasses(typeElement, rootType).toList();
    }

    /**
     * The same as {@link #findSuperclasses(TypeElement, Class)}, but with the {@code rootType} set as {@code Object}. 
     *
     * @param typeElement
     * @param includeRootClass
     * @return
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement) {
        return streamSuperclasses(typeElement, ROOT_CLASS).toList();
    }

    /**
     * Like {@link #streamSuperclasses(TypeElement, Class)}, but excludes {@code rootType}.
     * 
     * @param typeElement
     * @param rootType
     * @return
     */
    public Stream<TypeElement> streamSuperclassesBelow(final TypeElement typeElement, final Class<?> rootType) {
        if (!isSubtype(typeElement.asType(), rootType)) {
            return Stream.empty();
        }
        return iterate(Optional.of(typeElement), Optional::isPresent, elt -> elt.flatMap(ElementFinder::findSuperclass))
                .map(Optional::get)
                .takeWhile(elt -> !isSameType(elt, rootType))
                // drop the typeElement itself
                .skip(1);
    }

    /**
     * Collects the elements of {@link #streamSuperclassesBelow(TypeElement, Class)} into a list. 
     */
    public List<TypeElement> findSuperclassesBelow(final TypeElement typeElement, final Class<?> rootType) {
        return streamSuperclassesBelow(typeElement, rootType).toList();
    }

    /**
     * Returns a stream of variable elements, representing declared fields of the type element.
     *
     * @param element
     * @return
     */
    public Stream<VariableElement> streamDeclaredFields(final TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(elt -> elt.getKind().isField())
                .map(elt -> (VariableElement) elt);
    }

    /**
     * Collects the elements of {@link #streamDeclaredFields(TypeElement)} into a list.
     */
    public List<VariableElement> findDeclaredFields(final TypeElement element) {
        return streamDeclaredFields(element).toList();
    }

    /**
     * Returns a stream of variable elements, representing fields inherited by the type element with upper limit on superclasses equal to {@code rootType}.
     *
     * @param element
     * @param predicate
     * @return
     */
    public Stream<VariableElement> streamInheritedFields(final TypeElement element, final Class<?> rootType) {
        return streamSuperclasses(element, rootType).flatMap(elt -> streamDeclaredFields(elt));
    }

    /**
     * Returns a stream of variable elements, representing all inherited fields of the type element.
     */
    public Stream<VariableElement> streamInheritedFields(final TypeElement element) {
        return streamInheritedFields(element, ROOT_CLASS);
    }

    /**
     * Collects the elements of {@link #streamInheritedFields(TypeElement)} into a list.
     */
    public List<VariableElement> findInheritedFields(final TypeElement element) {
        return streamInheritedFields(element).toList();
    }

    /**
     * Returns a stream of variable elements, representing all fields of the type element: both declared and inherited.
     */
    public Stream<VariableElement> streamFields(final TypeElement element) {
        return Stream.concat(streamDeclaredFields(element), streamInheritedFields(element));
    }

    /**
     * Collects the elements of {@link #streamFields(TypeElement)} into a list.
     */
    public List<VariableElement> findFields(TypeElement element) {
        return streamFields(element).toList();
    }

    /**
     * Returns an optional describing a variable element that represents a field named {@code fieldName} and matching {@code predicate},
     * traversing the whole type hierarchy.
     *
     * @param typeElement
     * @param fieldName
     * @param predicate
     * @return
     */
    public Optional<VariableElement> findField(final TypeElement typeElement, final String fieldName, final Predicate<VariableElement> predicate) {
        // first search declared, then inherited fields
        return streamFields(typeElement)
               .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName) && predicate.test(varEl))
               .findFirst();
    }

    /**
     * Returns an optional describing a variable element that represents a field named {@code fieldName}, traversing the whole type hierarchy.
     *
     * @param typeElement
     * @param fieldName
     * @return
     */
    public Optional<VariableElement> findField(final TypeElement typeElement, final String fieldName) {
        return findField(typeElement, fieldName, (varEl) -> true);
    }

    /**
     * Returns an optional describing a variable element that represents a declared field named {@code fieldName} and matching {@code predicate}.
     *
     * @param typeElement
     * @param fieldName
     * @param predicate
     * @return
     */
    public Optional<VariableElement> findDeclaredField(final TypeElement typeElement, final String fieldName, final Predicate<VariableElement> predicate) {
        return streamDeclaredFields(typeElement)
               .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName) && predicate.test(varEl))
               .findFirst();
    }

    /**
     * Returns an optional describing a variable element that represents a declared field named {@code fieldName}.
     *
     * @param typeElement
     * @param fieldName
     * @return
     */
    public Optional<VariableElement> findDeclaredField(final TypeElement typeElement, final String fieldName) {
        return findDeclaredField(typeElement, fieldName, (varEl) -> true);
    }
    
    public List<VariableElement> findDeclaredFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return streamDeclaredFields(typeElement)
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .toList();
    }

    public List<VariableElement> findInheritedFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return streamInheritedFields(typeElement)
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .toList();
    }

    public List<VariableElement> findFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return streamFields(typeElement)
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .toList();
    }

    /**
     * Returns a list of annotations that are directly present on a variable element if it represents a field.
     * Otherwise an empty list is returned.
     */
    public List<? extends AnnotationMirror> getFieldAnnotations(final VariableElement element) {
        // return an empty list for non-field elements
        if (!element.getKind().isField()) {
            return List.of();
        }

        return element.getAnnotationMirrors();
    }

    /**
     * Streams declared methods of a type element.
     * @see ElementKind#METHOD
     */
    public Stream<ExecutableElement> streamDeclaredMethods(final TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(elt -> elt.getKind().equals(ElementKind.METHOD))
                .map(elt -> (ExecutableElement) elt);
    }

    /**
     * Collects the elements of {@link #streamDeclaredMethods(TypeElement)} into an unmodifiable list.
     * @see ElementKind#METHOD
     */
    public List<ExecutableElement> findDeclaredMethods(final TypeElement typeElement) {
        return streamDeclaredMethods(typeElement).toList();
    }

    /**
     * Streams all inherited methods of a type element. Ignores superinterfaces. 
     * @see ElementKind#METHOD
     */
    public Stream<ExecutableElement> streamInheritedMethods(final TypeElement typeElement) {
        return streamSuperclasses(typeElement).flatMap(type -> streamDeclaredMethods(type));
    }

    /**
     * Collects the elements of {@link #streamInheritedMethods(TypeElement)} into an unmodifiable list.
     * <p>
     * <b>NOTE</b>: The returned list may include methods with equivalent signatures declared by different types (e.g. when a method is overriden).
     * For a finer control of what is included use {@link #streamInheritedMethods(TypeElement)}.
     */
    public List<ExecutableElement> findInheritedMethods(final TypeElement typeElement) {
        return streamInheritedMethods(typeElement).toList();
    }

    /**
     * Streams all methods of a type element (both declared and inherited). Ignores superinterfaces.
     * The results are ordered such that declared methods appear first.
     */
    public Stream<ExecutableElement> streamMethods(TypeElement typeElement) {
        return Stream.concat(streamDeclaredMethods(typeElement), streamInheritedMethods(typeElement));
    }

    /**
     * Collects the elements of {@link #streamMethods(TypeElement)} into an unmodifiable list.
     * <p>
     * <b>NOTE</b>: The returned list may include methods with equivalent signatures declared by different types (e.g. when a method is overriden).
     * For a finer control of what is included use {@link #streamMethods(TypeElement)}.
     */
    public List<ExecutableElement> findMethods(TypeElement typeElement) {
        return streamMethods(typeElement).toList();
    }

    /**
     * The same as {@link #getFieldAnnotations(VariableElement)}, but without annotations {@code ignoredAnnotationsClasses}.
     *
     * @param field
     * @param ignoredAnnotationsClasses
     * @return
     */
    public List<? extends AnnotationMirror> getFieldAnnotationsExcept(final VariableElement field, final List<Class<? extends Annotation>> ignoredAnnotationsClasses) {
        final List<? extends AnnotationMirror> annotations = getFieldAnnotations(field);

        final Set<String> ignoredAnnotationNames = ignoredAnnotationsClasses.stream()
                .map(Class::getCanonicalName)
                .collect(toSet());
        
        return annotations.stream()
                .filter(annotMirror -> {
                    final String annotQualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
                    return !ignoredAnnotationNames.contains(annotQualifiedName);
                })
                .collect(toList());
    }

    /**
     * Finds an annotation of the specified type that is directly present on the element.
     */
    public Optional<? extends AnnotationMirror> findAnnotationMirror(final AnnotatedConstruct element, final Class<? extends Annotation> annotType) {
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> isSameType(mirror.getAnnotationType(), annotType))
                .findAny();
    }

    /**
     * Returns an optional desribing an {@link AnnotationValue} of the annotation's element with the specified name.
     * If the element is not explicitly present in the annotation, then its default value is returned.
     * <p>
     * Use {@link #getAnnotationElementValue(AnnotationMirror, String)} to obtain the value directly (i.e. unpack {@link AnnotationValue}).
     * 
     * @param annotation
     * @param name
     * @return
     */
    public Optional<AnnotationValue> findAnnotationValue(final AnnotationMirror annotation, final String name) {
        return elements.getElementValuesWithDefaults(annotation).entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().toString().equals(name))
                .findFirst().map(Entry::getValue);
    }

    /**
     * Returns the value of the annotation's element with the specified name type casted to the type argument.
     * If the element is not explicitly present in the annotation, then its default value is returned.
     * 
     * @param <T> the type to which to cast the value
     * @param annotation
     * @param name
     * @return
     * @throws ElementFinderException if no annotation element named {@code name} exists or type cast failed
     */
    public <T> T getAnnotationElementValue(final AnnotationMirror annotation, final String name) {
        final Object value = findAnnotationValue(annotation, name).map(AnnotationValue::getValue)
                .orElseThrow(() -> new ElementFinderException("No element named [%s] was found in annotation [%s]".formatted(name, annotation)));
        try {
            return (T) value;
        } catch (final Exception e) {
            throw new ElementFinderException("Failed to cast the annotation element's value.", e);
        }
    }

    /**
     * Returns the type mirror representing the {@link Class}-typed value of the annotation's element.
     * <p>
     * Special care is required for {@link Class} values, since information to locate and load a class is unavailable during annotation processing.
     * For a more detailed explanation refer to {@link Element#getAnnotation(Class)}.
     * 
     * @param annot annotation instance being examined
     * @param provider function applied to {@code annot} that provides the {@link Class} value
     * @return
     */
    public <A extends Annotation> TypeMirror getAnnotationElementValueOfClassType(final A annot, Function<A, Class<?>> provider) {
        try {
            // should ALWAYS throw, since the information to locate and load a class is unavailable during annotation processing
            final Class<?> clazz = provider.apply(annot);
            // if it somehow was available, then construct a TypeMirror from it
            return asType(clazz);
        } catch (final MirroredTypeException ex) {
            // the exception provides the desired type mirror
            return ex.getTypeMirror();
        }
    }

    /**
     * Returns a type mirror coresponding to the type represented by {@code clazz}.
     * <p>
     * For generic types a raw type representation is returned.
     *
     * @param clazz
     * @return
     * @throws ElementFinderException if no coresponding type element was found
     */
    public TypeMirror asType(final Class<?> clazz) {
        if (clazz == void.class) {
            return types.getNoType(TypeKind.VOID);
        }
        else if (clazz.isPrimitive()) {
            return types.getPrimitiveType(PRIMITIVE_CLASS_MAP.get(clazz));
        }
        else if (clazz.isArray()) {
            return types.getArrayType(asType(clazz.getComponentType()));
        }
        // clazz is class or interface, so return a raw type mirror
        return types.getDeclaredType(getTypeElement(clazz));
    }
    // void.class is not included in this map, so handle it separately
    private static final Map<Class<?>, TypeKind> PRIMITIVE_CLASS_MAP;
    static {
        PRIMITIVE_CLASS_MAP = new HashMap<>();
        PRIMITIVE_CLASS_MAP.put(int.class,     TypeKind.INT);
        PRIMITIVE_CLASS_MAP.put(boolean.class, TypeKind.BOOLEAN);
        PRIMITIVE_CLASS_MAP.put(double.class,  TypeKind.DOUBLE);
        PRIMITIVE_CLASS_MAP.put(long.class,    TypeKind.LONG);
        PRIMITIVE_CLASS_MAP.put(short.class,   TypeKind.SHORT);
        PRIMITIVE_CLASS_MAP.put(byte.class,    TypeKind.BYTE);
        PRIMITIVE_CLASS_MAP.put(float.class,   TypeKind.FLOAT);
        PRIMITIVE_CLASS_MAP.put(char.class,    TypeKind.CHAR);
    }

    /**
     * Converts a type mirror to a declared type iff the type mirror represents a declared type, otherwise an exception is thrown.
     * 
     * @param mirror
     * @return
     * @throws ElementFinderException
     */
    public static DeclaredType asDeclaredType(final TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            throw new ElementFinderException("Illegal type kind [%s] of [%s]".formatted(mirror.getKind(), mirror.toString()));
        }
        return (DeclaredType) mirror;
    }

    /**
     * Tests whether the type mirror represents the same type as the given class.
     * <p>
     * This comparison makes sense only if the type mirror represents one of:
     * primitive type, void type, array type, declared type (class/interface). Otherwise {@code false} is returned.
     * <p>
     * Comparison of generic types requires special care.
     * Since {@link Class} instances can represent only raw types, the generic part of the type mirror is always stripped before comparing.
     * 
     * @throws ElementFinderException if no coresponding type element was found
     */
    public boolean isSameType(final TypeMirror mirror, final Class<?> clazz) {
        return mirror.accept(new IsSameTypeVisitor(clazz), null);
    }
    // where
    private final class IsSameTypeVisitor extends SimpleTypeVisitor14<Boolean, Void> {
        private final Class<?> clazz;

        protected IsSameTypeVisitor(final Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        protected Boolean defaultAction(TypeMirror e, Void p) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(final PrimitiveType t, final Void p) {
            return PRIMITIVE_TYPE_MAP.get(t.getKind()) == clazz;
        }

        private static final Map<TypeKind, Class<?>> PRIMITIVE_TYPE_MAP;
        static {
            PRIMITIVE_TYPE_MAP = new EnumMap<>(TypeKind.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.BOOLEAN, boolean.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.BYTE,    byte.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.SHORT,   short.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.INT,     int.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.LONG,    long.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.CHAR,    char.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.FLOAT,   float.class);
            PRIMITIVE_TYPE_MAP.put(TypeKind.DOUBLE,  double.class);
        }

        // handle void type
        @Override
        public Boolean visitNoType(final NoType t, final Void p) {
            return t.getKind() == TypeKind.VOID && clazz == void.class;
        }

        @Override
        public Boolean visitArray(ArrayType t, Void p) {
            return clazz.isArray() && isSameType(t.getComponentType(), clazz.componentType());
        }

        @Override
        public Boolean visitDeclared(final DeclaredType t, final Void p) {
            return isSameType(asTypeElement(t), clazz);
        }
    }

    /**
     * Tests whether the type represented by the type mirror is a subtype of the given class. Any type is considered to be a subtype of itself.
     * <p>
     * This comparison makes sense only if the type mirror represents one of:
     * primitive type, void type, array type, declared type (class/interface). Otherwise {@code false} is returned.
     * <p>
     * Comparison of generic types requires special care.
     * Since {@link Class} instances can represent only raw types, the generic part of the type mirror is always stripped before comparing.
     * 
     * @throws ElementFinderException if no coresponding type element was found
     */
    public boolean isSubtype(final TypeMirror mirror, final Class<?> clazz) {
        return mirror.accept(new IsSubtypeVisitor(clazz), null);
    }
    // where
    private final class IsSubtypeVisitor extends SimpleTypeVisitor14<Boolean, Void> {
        private final Class<?> clazz;

        protected IsSubtypeVisitor(final Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        protected Boolean defaultAction(TypeMirror e, Void p) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(final PrimitiveType t, final Void p) {
            if (!clazz.isPrimitive()) {
                return false;
            }
            final Set<Class<?>> set = PRIMITIVE_SUPERTYPES_MAP.get(t.getKind());
            return set != null && set.contains(clazz);
        }

        // from type to the set of itself and its supertypes
        private static final Map<TypeKind, Set<Class<?>>> PRIMITIVE_SUPERTYPES_MAP;
        static {
            PRIMITIVE_SUPERTYPES_MAP = new EnumMap<>(TypeKind.class);
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.INT,   Set.of(int.class, long.class, float.class, double.class));
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.LONG,  Set.of(long.class, float.class, double.class));
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.SHORT, Set.of(short.class, int.class, long.class, float.class, double.class));
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.BYTE,  Set.of(byte.class, short.class, int.class, long.class, float.class, double.class));
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.FLOAT, Set.of(float.class, double.class));
            PRIMITIVE_SUPERTYPES_MAP.put(TypeKind.CHAR,  Set.of(char.class, int.class, long.class, float.class, double.class));
        }

        // handle void type
        @Override
        public Boolean visitNoType(final NoType t, final Void p) {
            return t.getKind() == TypeKind.VOID && clazz == void.class;
        }

        @Override
        public Boolean visitArray(ArrayType t, Void p) {
            return clazz.isArray() && isSubtype(t.getComponentType(), clazz.componentType());
        }

        @Override
        public Boolean visitDeclared(final DeclaredType t, final Void p) {
            final TypeElement elt = asTypeElement(t);
            return isSameType(elt, clazz)
                    || streamAllSupertypes(elt).anyMatch(sup -> isSameType(sup, clazz));
        }
    }

    public Stream<TypeElement> streamAllSupertypes(final TypeElement element) {
        return doStreamAllSupertypes(element).skip(1); // skip the initial element
    }

    private Stream<TypeElement> doStreamAllSupertypes(final TypeElement element) {
        // TODO optimise: traverse the hierarchy lazily 
        return StreamUtils.distinct(
                Stream.concat(Stream.of(element),
                        types.directSupertypes(element.asType()).stream()
                            .map(t -> asTypeElementOfTypeMirror(t))
                            .flatMap(this::doStreamAllSupertypes)),
                // using Name rather than String should be faster, since Name-s are interned
                elt -> elt.getQualifiedName());
    }

    public static boolean isTopLevelClass(final Element element) {
        return element.getKind() == ElementKind.CLASS && element.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }

    /**
     * Tests whether the element contains the {@code static} modifier.
     */
    public static boolean isStatic(final Element element) {
        return element.getModifiers().contains(Modifier.STATIC);

    }
    public static boolean isAbstract(final Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    public static boolean isPublic(final Element element) {
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    public static boolean isProtected(final Element element) {
        return element.getModifiers().contains(Modifier.PROTECTED);
    }

    public static boolean isFinal(final Element element) {
        return element.getModifiers().contains(Modifier.FINAL);
    }

    public static boolean isGeneric(final TypeElement element) {
        return element != null && !element.getTypeParameters().isEmpty();
    }

    public static boolean isRawType(final TypeMirror type) {
        return TypeKind.DECLARED == type.getKind() && ((DeclaredType) type).getTypeArguments().isEmpty();
    }

    /**
     * Wraps {@link Elements#getPackageOf} in order to avoid ClassCastException, since Sun's internal implementation of {@link Elements} expects a {@link com.sun.tools.javac.code.Symbol} instance.
     * <p>
     * In order to enable support for {@link ForwardingElement} we have to dynamically check the type of <code>element</code>.
     * 
     * @param element
     * @return
     */
    public Optional<PackageElement> getPackageOf(final Element element) {
        if (AbstractForwardingElement.class.isAssignableFrom(element.getClass())) {
            return Optional.ofNullable(elements.getPackageOf(((AbstractForwardingElement<Element>) element).element()));
        }
        return Optional.ofNullable(elements.getPackageOf(element));
    }
    
    public Optional<String> getPackageName(final Element element) {
        return getPackageOf(element).map(pkgEl -> pkgEl.getQualifiedName().toString());
    }

    /**
     * Like {@link #getPackageOf(Element)}, but accepting a type element, which should always have a package, hence the absence of {@link Optional}.
     * 
     * @param element
     * @return
     * @throws ElementFinderException if the type element's package could not be found
     */
    public PackageElement getPackageOfTypeElement(final TypeElement element) {
        return getPackageOf(element)
                .orElseThrow(() -> new ElementFinderException("No package was found for %s".formatted(element.getQualifiedName())));
    }

    /**
     * Returns the type element coresponding to the given declared type.
     */
    public static TypeElement asTypeElement(final DeclaredType type) {
        return (TypeElement) type.asElement();
    }

    /**
     * Returns the type element coresponding to the given type mirror iff it represents a declared type, otherwise an exception is thrown.
     * 
     * @param mirror
     * @return
     * @throws ElementFinderException
     */
    public static TypeElement asTypeElementOfTypeMirror(final TypeMirror mirror) {
        return asTypeElement(asDeclaredType(mirror));
    }


    /**
     * Returns simple name of a type represented by the type mirror.
     * <p>
     * If the type mirror does not represent a declared type an exception is thrown.
     * 
     * @throws ElementFinderException
     */
    public static String getSimpleName(final TypeMirror mirror) {
        return asTypeElementOfTypeMirror(mirror).getSimpleName().toString();
    }

    /**
     * Returns the element's simple name as a {@link String}. 
     * <p>
     * This is a shortcut for {@link Element#getSimpleName()} and {@link Name#toString()}.
     */
    public static String getSimpleName(final Element element) {
        return element.getSimpleName().toString();
    }

}