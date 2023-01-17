package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.iterate;
import static ua.com.fielden.platform.utils.StreamUtils.stopAfter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.metamodel.elements.AbstractForwardingElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;

/**
 * A collection of utility functions to finding various type elements. 
 *
 * @author TG Team
 *
 */
public class ElementFinder {
    public static final Class<?> DEFAULT_ROOT_CLASS = Object.class;

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
     * A shortcut for {@link Elements#getTypeElement(CharSequence)}.
     * <p>
     * If no corespodning type element was found, then a runtime exception is thrown.
     * Generally, this should never occur, since the {@link Class} argument guarantees the existence of that type. 
     * <p>
     * In case of multi-module application where there are multiple classes with the same canonical name, the first match is returned.
     * 
     * @param clazz
     * @return type element representing {@code clazz}
     * @throws ElementFinderException if no coresponding type element was found
     */
    public TypeElement getTypeElement(final Class<?> clazz) {
        return elements.getAllTypeElements(clazz.getCanonicalName()).stream().findFirst()
                .orElseThrow(() -> new ElementFinderException("No type element was found for class %s".formatted(clazz.getCanonicalName())));
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
    public boolean isSameType(final TypeElement element, final Class<?> clazz) {
        if (element == null || clazz == null) {
            throw new EntityMetaModelException("Neither typeElement nor type arguments can be null.");
        }
        return element.getQualifiedName().toString().equals(clazz.getCanonicalName());
    }

    /**
     * Returns the immediate superclass of a type element if there is one.
     * An empty optional is returned if the type element represents an interface type or the {@link Object} class.
     */
    public Optional<TypeElement> findSuperclass(final TypeElement element) {
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
                iterate(Optional.of(typeElement), Optional::isPresent, elt -> elt.flatMap(this::findSuperclass)).map(Optional::get),
                elt -> isSameType(elt, rootType))
                .skip(1); // drop the typeElement itself
    }

    /**
     * Like {@link #streamSuperclasses(TypeElement, Class)} with {@code rootType} equal to {@code Object}. 
     */
    public Stream<TypeElement> streamSuperclasses(final TypeElement typeElement) {
        return streamSuperclasses(typeElement, DEFAULT_ROOT_CLASS);
    }

    /**
     * Collects the elements of {@link #streamSuperclasses(TypeElement, Class)} into a list. 
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement, final Class<?> rootType) {
        return streamSuperclasses(typeElement, rootType).toList();
    }

    /**
     * Collects the elements of {@link #streamSuperclasses(TypeElement)} into a list. 
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement) {
        return streamSuperclasses(typeElement, DEFAULT_ROOT_CLASS).toList();
    }

    /**
     * Finds all super-types of a {@link TypeMirror}. No particular order is preserved.
     *
     * @param typeMirror
     * @return
     */
    public List<TypeMirror> findSupertypes(final TypeMirror typeMirror) {
        final List<TypeMirror> supertypes = (List<TypeMirror>) types.directSupertypes(typeMirror);
        return supertypes.stream().flatMap(tm -> findSupertypes(tm).stream()).toList();
    }

    /**
     * Returns a stream of variable elements, representing declared fields of the type element matching {@code predicate}.
     *
     * @param element
     * @param predicate
     * @return
     */
    public Stream<VariableElement> streamDeclaredFields(final TypeElement element, final Predicate<VariableElement> predicate) {
        return element.getEnclosedElements().stream()
                .filter(elt -> elt.getKind().isField())
                .map(elt -> (VariableElement) elt)
                .filter(predicate);
    }

    /**
     * Returns a stream of variable elements, representing all declared fields of a type element.
     *
     * @param element
     * @return
     */
    public Stream<VariableElement> streamDeclaredFields(final TypeElement element) {
        return streamDeclaredFields(element, elt -> true);
    }

    /**
     * Collects the elements of {@link #streamDeclaredFields(TypeElement, Predicate)} into a list.
     */
    public List<VariableElement> findDeclaredFields(final TypeElement element, final Predicate<VariableElement> predicate) {
        return streamDeclaredFields(element, predicate).toList();
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
        return streamInheritedFields(element, DEFAULT_ROOT_CLASS);
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

    public List<VariableElement> findStaticDeclaredFields(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, f -> isStatic(f));
    }

    public List<VariableElement> findNonStaticDeclaredFields(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, f -> !isStatic(f));
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
     * Streams declared methods of a type element that satisfy the predicate.
     * @see ElementKind#METHOD
     */
    public Stream<ExecutableElement> streamDeclaredMethods(final TypeElement typeElement, final Predicate<ExecutableElement> predicate) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.METHOD)
                .map(el -> (ExecutableElement) el)
                .filter(predicate);
    }

    /**
     * Streams declared methods of a type element.
     * @see ElementKind#METHOD
     */
    public Stream<ExecutableElement> streamDeclaredMethods(final TypeElement typeElement) {
        return streamDeclaredMethods(typeElement, el -> true);
    }

    /**
     * Finds declared methods of a type element that satisfy the predicate.
     * @see ElementKind#METHOD
     */
    public Set<ExecutableElement> findDeclaredMethods(final TypeElement typeElement, final Predicate<ExecutableElement> predicate) {
        return streamDeclaredMethods(typeElement, predicate).collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Finds declared methods of a type element.
     * @see ElementKind#METHOD
     */
    public Set<ExecutableElement> findDeclaredMethods(TypeElement typeElement) {
        return findDeclaredMethods(typeElement, el -> true);
    }

    /**
     * Finds inherited methods of a type element. Ignores superinterfaces.
     * @see ElementKind#METHOD
     */
    public Set<ExecutableElement> findInheritedMethods(final TypeElement typeElement) {
        return streamSuperclasses(typeElement)
                .flatMap(type -> findDeclaredMethods(type).stream()).collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Finds all methods of a type element (both declared and inherited). Ignores superinterfaces.
     * The results are ordered such that declared methods appear first.
     * @see ElementKind#METHOD
     */
    public LinkedHashSet<ExecutableElement> findMethods(TypeElement typeElement) {
        final LinkedHashSet<ExecutableElement> methods = streamDeclaredMethods(typeElement).collect(toCollection(LinkedHashSet::new));
        methods.addAll(findInheritedMethods(typeElement));
        return methods;
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
                .filter(mirror -> isSameType((TypeElement) mirror.getAnnotationType().asElement(), annotType))
                .findAny();
    }

    /**
     * Returns an optional containing the value of the annotation's element.
     * @param annotation
     * @param elementName
     * @return
     */
    public Optional<AnnotationValue> getAnnotationValue(final AnnotationMirror annotation, final String elementName) {
        return elements.getElementValuesWithDefaults(annotation).entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().toString().equals(elementName))
                .findAny().map(Entry::getValue);
    }

    /**
     * Returns an optional containing the value of the annotation's default element (i.e. the {@code value()} element).
     * @param annotation
     * @return
     */
    public Optional<AnnotationValue> getAnnotationValue(final AnnotationMirror annotation) {
        return getAnnotationValue(annotation, "value");
    }

    /**
     * Tests whether the element contains the {@code static} modifier.
     */
    public boolean isStatic(final Element element) {
        return element.getModifiers().contains(Modifier.STATIC);
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
        if (clazz.isPrimitive()) {
            if (clazz.equals(void.class)) {
                return types.getNoType(TypeKind.VOID);
            } else if (clazz.equals(int.class)) {
                return types.getPrimitiveType(TypeKind.INT);
            } else if (clazz.equals(boolean.class)) {
                return types.getPrimitiveType(TypeKind.BOOLEAN);
            } else if (clazz.equals(double.class)) {
                return types.getPrimitiveType(TypeKind.DOUBLE);
            } else if (clazz.equals(long.class)) {
                return types.getPrimitiveType(TypeKind.LONG);
            } else if (clazz.equals(short.class)) {
                return types.getPrimitiveType(TypeKind.SHORT);
            } else if (clazz.equals(byte.class)) {
                return types.getPrimitiveType(TypeKind.BYTE);
            } else if (clazz.equals(float.class)) {
                return types.getPrimitiveType(TypeKind.FLOAT);
            } else if (clazz.equals(char.class)) {
                return types.getPrimitiveType(TypeKind.CHAR);
            }
        }
        else if (clazz.isArray()) {
            return types.getArrayType(asType(clazz.getComponentType()));
        }
        // clazz is class or interface, so return a raw type mirror
        return types.getDeclaredType(getTypeElement(clazz));
    }

    /**
     * Converts a type mirror to a declared type iff the type mirror represents a declared type, otherwise an exception is thrown.
     * 
     * @param mirror
     * @return
     * @throws ElementFinderException
     */
    public DeclaredType asDeclaredType(final TypeMirror mirror) {
        if (mirror.getKind() != TypeKind.DECLARED) {
            throw new ElementFinderException("Illegal type kind [%s] of [%s]".formatted(mirror.getKind(), mirror.toString()));
        }
        return (DeclaredType) mirror;
    }

    /**
     * Tests whether the type mirror and class represent the same type. Such a comparison makes sense only if the type mirror represents one of:
     * primitive type, void type, array type, declared type (class/interface). Otherwise {@code false} is returned.
     * <p>
     * Comparison of generic types requires special care.
     * Since {@link Class} instances can represent only raw types, if a type mirror for a generic type does not represent its raw type, 
     * then {@code false} will be returned. One can obtain a raw type from a type mirror using {@link Types#erasure(TypeMirror)}.
     * 
     * @throws ElementFinderException if no coresponding type element was found
     */
    public boolean isSameType(final TypeMirror mirror, final Class<?> clazz) {
        return types.isSameType(mirror, asType(clazz));
    }

    /**
     * Similar to {@link Types#isSubtype(TypeMirror, TypeMirror)}, but accepts {@link Class} as the second type.
     *
     * @param typeMirror the first type
     * @param clazz the second type
     * @return {@code true} iff the first type is a subtype of the second
     * @throws ElementFinderException if no coresponding type element was found
     */
    public boolean isSubtype(final TypeMirror mirror, final Class<?> clazz) {
        return types.isSubtype(mirror, asType(clazz));
    }

    public boolean isTopLevelClass(final Element element) {
        return element.getKind() == ElementKind.CLASS && element.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    public boolean isAbstract(final Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    public boolean isGeneric(final TypeElement element) {
        return element != null && !element.getTypeParameters().isEmpty();
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
    public TypeElement asTypeElement(final DeclaredType type) {
        return (TypeElement) type.asElement();
    }

    /**
     * Returns the type element coresponding to the given type mirror iff it represents a declared type, otherwise an exception is thrown.
     * 
     * @param mirror
     * @return
     * @throws ElementFinderException
     */
    public TypeElement asTypeElementOfTypeMirror(final TypeMirror mirror) {
        return asTypeElement(asDeclaredType(mirror));
    }

}