package ua.com.fielden.platform.processors.utils;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.iterate;
import static ua.com.fielden.platform.utils.StreamUtils.stopAfter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
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

    protected final Elements elements;
    protected final Types types;

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

    public Elements getElements() {
        return this.elements;
    }

    public Types getTypes() {
        return this.types;
    }

    /**
     * A {@link TypeElement} instance is equal to a {@link Class} instance if both objects have the same qualified (canonical) name.
     * <p>
     * A local class, local interface, or anonymous class does not have a canonical name.
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-6.html#jls-6.7">Java SE16 Language Specification - Fully Qualified Names and Canonical Names</a>
     *
     * @param typeElement
     * @param type
     */
    public boolean equals(final TypeElement typeElement, final Class<?> type) {
        if (typeElement == null || type == null) {
            throw new EntityMetaModelException("Neither typeElement nor type arguments can be null.");
        }
        return typeElement.getQualifiedName().toString().equals(type.getCanonicalName());
    }

    /**
     * Returns the immediate parent class of {@code typeElement} or null in the following cases:
     * <ul>
     *  <li>{@code typeElement} is the {@link Object} type
     *  <li>{@code typeElement} is an interface type
     *  <li>The superclass of {@code typeElement} is an interface type
     * </ul>
     * 
     * @param typeElement
     * @return
     */
    public TypeElement findSuperclass(final TypeElement typeElement) {
        final TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        final TypeElement superclassTypeElement = toTypeElement(superclass);
        // ignore interfaces
        return superclassTypeElement.getKind() == ElementKind.INTERFACE ? null : superclassTypeElement;
    }

    /**
     * Returns an ordered list of all super-classes with respect to {@code typeElement}.
     * The type hierarchy is traversed until either {@code rootType} or an interface type is reached.
     * <p>
     * If {@code rootType} is not in the class hierarchy, then an empty list is returned.
     * <p>
     * {@code rootType} is included in the resulting list if it's a class type.
     *
     * @param typeElement
     * @param rootType
     * @return
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement, final Class<?> rootType) {
        if (!isSubtype(typeElement.asType(), rootType)) {
            return List.of();
        }
        return stopAfter(
                iterate(findSuperclass(typeElement), te -> findSuperclass(te)),
                te -> te == null || equals(te, rootType))
                .filter(te -> te != null)
                .toList();
    }

    /**
     * The same as {@link #findSuperclasses(TypeElement, Class)}, but with the {@code rootType} set as {@code Object}. 
     *
     * @param typeElement
     * @param includeRootClass
     * @return
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement) {
        return findSuperclasses(typeElement, DEFAULT_ROOT_CLASS);
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
     * Looks for variable elements, representing declared fields that match {@code predicate}.
     *
     * @param typeElement
     * @param predicate
     * @return
     */
    public Set<VariableElement> findDeclaredFields(final TypeElement typeElement, final Predicate<VariableElement> predicate) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .filter(predicate)
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Looks for variable elements, representing all explicitly declared fields.
     */
    public Set<VariableElement> findDeclaredFields(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, el -> true);
    }

    /**
     * Looks for variable elements, representing inherited fields.
     *
     * @param typeElement
     * @return
     */
    public Set<VariableElement> findInheritedFields(final TypeElement typeElement) {
        return findInheritedFields(typeElement, DEFAULT_ROOT_CLASS);
    }

    /**
     * Looks for variable elements, representing all declared and inherited fields.
     */
    public Set<VariableElement> findFields(TypeElement typeElement) {
        final Set<VariableElement> fields = findDeclaredFields(typeElement);
        fields.addAll(findInheritedFields(typeElement));
        return fields;
    }

    /**
     * The same as {@link #findInheritedFields(TypeElement)}, but with upper type constrained to <code>rootClass</code>.
     * <p>
     * If <code>rootClass</code> is absent in the class hierarchy, then an empty set is returned.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of a superclass of typeElement
     */
    public Set<VariableElement> findInheritedFields(final TypeElement typeElement, final Class<?> rootClass) {
        // use LinkedHashSet to store fields so that they appear in their hierarchical order,
        // that is, fields inherited from the root of the type hierarchy will be placed at the end of the set
        return findSuperclasses(typeElement, rootClass).stream()
                .flatMap(te -> findDeclaredFields(te).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * The same as {@link #findFields(TypeElement)}, but with upper type constrained to {@code rootClass}.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of a superclass of typeElement
     */
    public Set<VariableElement> findFields(final TypeElement typeElement, final Class<?> rootClass) {
        final Set<VariableElement> fields = findDeclaredFields(typeElement);
        fields.addAll(findInheritedFields(typeElement, rootClass));
        return fields;
    }

    public Set<VariableElement> findStaticFields(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, f -> isStatic(f));
    }

    public Set<VariableElement> findNonStaticFields(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, f -> !isStatic(f));
    }

    /**
     * Returns a variable element, representing a field with name {@code fieldName} and matching the given {@code predicate}, or else {@code null}.
     * <p>
     * The whole type hierarchy is processed, until a matching field is found.
     *
     * @param typeElement
     * @param fieldName
     * @param predicate
     * @return
     */
    public VariableElement findField(final TypeElement typeElement, final String fieldName, final Predicate<VariableElement> predicate) {
        // first search in declared and then inherited fields
        return findDeclaredFields(typeElement).stream()
               .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName) && predicate.test(varEl))
               .findFirst()
               .orElseGet(() -> findInheritedFields(typeElement).stream()
                                .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName))
                                .findFirst().orElse(null));
    }

    /**
     * Returns a variable element, representing a field with name {@code fieldName}, or else {@code null}.
     * <p>
     * The whole type hierarchy is processed, until a matching field is found.
     *
     * @param typeElement
     * @param fieldName
     * @return
     */
    public VariableElement findField(final TypeElement typeElement, final String fieldName) {
        return findField(typeElement, fieldName, (varEl) -> true);
    }

    /**
     * Returns a variable element, representing a declared field with name {@code fieldName} and matching the given {@code predicate}, or else {@code null}.
     *
     * @param typeElement
     * @param fieldName
     * @param predicate
     * @return
     */
    public VariableElement findDeclaredField(final TypeElement typeElement, final String fieldName, final Predicate<VariableElement> predicate) {
        return findDeclaredFields(typeElement).stream()
               .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName) && predicate.test(varEl))
               .findFirst()
               .orElse(null);
    }

    /**
     * Returns a variable element, representing a declared field with name {@code fieldName}, or else {@code null}.
     *
     * @param typeElement
     * @param fieldName
     * @return
     */
    public VariableElement findDeclaredField(final TypeElement typeElement, final String fieldName) {
        return findDeclaredField(typeElement, fieldName, (varEl) -> true);
    }
    
    public Set<VariableElement> findDeclaredFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return findDeclaredFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(toSet());
    }

    public Set<VariableElement> findInheritedFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return findInheritedFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(toSet());
    }

    public Set<VariableElement> findFieldsAnnotatedWith(final TypeElement typeElement, final Class<? extends Annotation> annotationClass) {
        return findFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(toSet());
    }

    /**
     * Returns a list of annotations associated with {@code vel}, if it represents a field.
     * An empty list is returned otherwise. 
     *
     * @param vel
     * @return
     */
    public List<? extends AnnotationMirror> getFieldAnnotations(final VariableElement vel) {
        // return an empty list for non-field elements
        if (vel.getKind() != ElementKind.FIELD) {
            return new ArrayList<>();
        }

        return vel.getAnnotationMirrors();
    }

    /**
     * Returns a set of executable elements, representing methods, declared in {@code typeElement} and matching {@code predicate}.
     *
     * @param typeElement
     * @param predicate
     * @return
     */
    public Set<ExecutableElement> findDeclaredMethods(final TypeElement typeElement, final Predicate<ExecutableElement> predicate) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.METHOD)
                .map(el -> (ExecutableElement) el)
                .filter(predicate)
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Find methods that are explicitly declared by this {@link TypeElement}.
     */
    public Set<ExecutableElement> findDeclaredMethods(TypeElement typeElement) {
        return findDeclaredMethods(typeElement, el -> true);
    }

    /**
     * Looks for executable elements, representing inherited methods.
     *
     * @param typeElement
     * @return
     */
    public Set<ExecutableElement> findInheritedMethods(final TypeElement typeElement) {
        return iterate(findSuperclass(typeElement), superType -> findSuperclass(superType))
                .takeWhile(el -> el != null)
                .flatMap(type -> findDeclaredMethods(type).stream()).collect(toCollection(LinkedHashSet::new));

    }

    /**
     * Looks for executable elements, representing all declared and inherited methods. The order is preserved, giving first priority to declared (important for overriden) methods.
     */
    public Set<ExecutableElement> findMethods(TypeElement typeElement) {
        final Set<ExecutableElement> fields = findDeclaredMethods(typeElement);
        fields.addAll(findInheritedMethods(typeElement));
        return fields;
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

    public AnnotationMirror getElementAnnotationMirror(final AnnotatedConstruct element, final Class<? extends Annotation> annotationClass) {
        for (final AnnotationMirror annotMirror: element.getAnnotationMirrors()) {
            final TypeElement annotTypeElement = (TypeElement) annotMirror.getAnnotationType().asElement();
            if (equals(annotTypeElement, annotationClass)) {
                return annotMirror;
            }
        }
        return null;
    }

    public String getFieldTypeSimpleName(final VariableElement field) {
        return ((DeclaredType) field.asType()).asElement().getSimpleName().toString();
    }

    public boolean isFieldOfType(final VariableElement field, final Class<?> type) {
        final TypeMirror fieldType = field.asType();
        if (fieldType.getKind().equals(TypeKind.DECLARED)) {
            return equals(((TypeElement) ((DeclaredType) fieldType).asElement()), type);
        } else {
            // TODO implement proper type checking for primitives and arrays
            return false;
        }
    }

    public boolean isFieldOfType(final VariableElement field, final TypeMirror typeMirror) {
        final TypeMirror fieldType = field.asType();
        final TypeKind fieldTypeKind = fieldType.getKind();

        if (fieldTypeKind.equals(TypeKind.DECLARED)) {
            return types.isSameType(fieldType, typeMirror);
        } else {
            // TODO implement proper type checking for primitives and arrays
            return false;
        }
    }

    /**
     * Tests whether a field is of one of types {@code typeElements}.
     *
     * @param field
     * @param typeElements
     * @return
     */
    public boolean isFieldOfType(final VariableElement field, final Collection<TypeMirror> typeMirrors) {
        final TypeMirror fieldType = field.asType();
        final TypeKind fieldTypeKind = fieldType.getKind();

        if (fieldTypeKind.equals(TypeKind.DECLARED)) {
            return typeMirrors.stream().anyMatch(tm -> types.isSameType(fieldType, tm));
        } else {
            // TODO implement proper type checking for primitives and arrays
            return false;
        }
    }

    public boolean isMethodReturnType(final ExecutableElement method, final Class<?> type) {
        final TypeMirror returnType = method.getReturnType();
        final TypeKind returnTypeKind = returnType.getKind();

        if (returnTypeKind.equals(TypeKind.DECLARED)) {
            return equals(((TypeElement) ((DeclaredType) returnType).asElement()), type);
        } else {
            // TODO implement proper type checking for primitives and arrays
            return false;
        }
    }

    public Name getAnnotationMirrorSimpleName(final AnnotationMirror annotMirror) {
        return annotMirror.getAnnotationType().asElement().getSimpleName();
    }

    public long countDeclaredFields(final TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .count();
    }

    public boolean isStatic(final VariableElement varElement) {
        return varElement.getModifiers().contains(Modifier.STATIC);
    }

    /**
     * Tests whether an instance of {@link TypeMirror} ({@code typeMirror}) is a subtype of a {@link Class} instance ({@code type}).
     * Any type is considered to be a subtype of itself.
     *
     * @param typeMirror  the child type
     * @param type  the parent type
     * @return {@code true} if and only if the first type is a subtype
     *          of the second
     */
    public boolean isSubtype(final TypeMirror typeMirror, final Class<?> type) {
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (equals(toTypeElement(typeMirror), type)) {
            return true;
        }
        return types.directSupertypes(typeMirror).stream().anyMatch(tm -> isSubtype(tm, type));
    }

    public boolean isTopLevelClass(final Element element) {
        return element.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    /**
     * Wraps {@link Elements#getPackageOf} in order to avoid ClassCastException, since Sun's internal implementation of {@link Elements} expects a {@link com.sun.tools.javac.code.Symbol} instance.
     * <p>
     * In order to enable support for {@link ForwardingElement} we have to dynamically check the type of <code>element</code>.
     * 
     * @param element
     * @return
     */
    public PackageElement getPackageOf(final Element element) {
        if (AbstractForwardingElement.class.isAssignableFrom(element.getClass())) {
            return elements.getPackageOf(((AbstractForwardingElement<Element>) element).element());
        }
        return elements.getPackageOf(element);
    }
    
    public String getPackageName(final Element element) {
        return Optional.ofNullable(getPackageOf(element))
                .map(pkgEl -> pkgEl.getQualifiedName().toString())
                .orElse(null);
    }

    /**
     * Converts {@link TypeMirror} to {@link TypeElement}.
     * @param typeMirror
     * @return a {@link TypeElement} if conversion was successful, otherwise null
     */
    public TypeElement toTypeElement(final TypeMirror typeMirror) {
        return typeMirror.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) typeMirror).asElement() : null;
    }

}
