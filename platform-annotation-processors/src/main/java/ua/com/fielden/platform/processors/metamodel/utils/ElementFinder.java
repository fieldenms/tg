package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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
     * Determines whether {@code typElement} represent a type that extends {@code type}.
     *
     * @param typeElement
     * @param type
     * @return
     */
    public boolean doesExtend(final TypeElement typeElement, final Class<?> type) {
        final List<TypeElement> superclasses = findSuperclasses(typeElement, true);
        return superclasses.stream().anyMatch(sup -> equals(sup, type));
    }

    /**
     * Returns the immediate parent class of {@code typeElement} or null if {@code typeElement} is either an interface type or {@link Object}, or a {@code rootClass}.
     * 
     * @param typeElement
     * @param rootClass
     * @return
     */
    public TypeElement getSuperclassOrNull(final TypeElement typeElement, final Class<?> rootClass) {
        // if this is a root class then return null
        if (equals(typeElement, rootClass)) {
            return null;
        }

        // with correct usage this code would never be reached
        // but if supplied root class is not in the type hierarchy then this condition could be reached
        final TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }

        return (TypeElement) ((DeclaredType) superclass).asElement();
    }

    /**
     * Returns the immediate parent class of {@code typeElement} or null if {@code typeElement} is either an interface type or {@link Object}.
     *
     * @param typeElement
     * @param rootClass
     * @return
     */
    public TypeElement getSuperclassOrNull(final TypeElement typeElement) {
        return getSuperclassOrNull(typeElement, DEFAULT_ROOT_CLASS);
    }

    /**
     * Returns a list of all super-classes with respect to {@code typeElement}.
     * The type hierarchy is traversed until either {@code rootType} or an interface type or {@link Object} is reached.
     *
     * @param typeElement
     * @param rootType
     * @param includeRootType - controls whether the provided {@code rootType} is included in the list.
     * @return
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement, final Class<?> rootType, final boolean includeRootType) {
        final List<TypeElement> superclasses = new ArrayList<>();

        TypeMirror superclassTypeMirror = typeElement.getSuperclass();
        TypeElement superclass = null;
        while (superclassTypeMirror.getKind() != TypeKind.NONE) {
            superclass = (TypeElement) ((DeclaredType) superclassTypeMirror).asElement();

            if (equals(superclass, rootType)) {
                if (includeRootType) {
                    superclasses.add(superclass);
                }
                break;
            }

            superclasses.add(superclass);
            superclassTypeMirror = superclass.getSuperclass();
        }

        // if the last parent element was an interface type, remove it
        if (superclass != null && !superclasses.isEmpty() && (superclass.getKind() == ElementKind.INTERFACE)) {
            superclasses.remove(superclasses.size() - 1);
        }
        
        return superclasses;
    }

    /**
     * The same as {@link #findSuperclasses(TypeElement, Class, boolean)}, but with the {@code rootType} set as {@code Object}. 
     *
     * @param typeElement
     * @param includeRootClass
     * @return
     */
    public List<TypeElement> findSuperclasses(final TypeElement typeElement, final boolean includeRootClass) {
        return findSuperclasses(typeElement, DEFAULT_ROOT_CLASS, includeRootClass);
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
        // use LinkedHashSet to store fields so that they appear in their hierarchical order,
        // that is, fields inherited from the root of the type hierarchy will be placed at the end of the set
        final Set<VariableElement> fields = new LinkedHashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement);
        while (superclass != null) {
            fields.addAll(findDeclaredFields(superclass));
            superclass = getSuperclassOrNull(superclass);
        }

        return fields;
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
     * The same as {@link #findInheritedFields(TypeElement)}, but with upper type constrained to {@code rootClass}.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of a superclass of typeElement
     */
    public Set<VariableElement> findInheritedFields(TypeElement typeElement, Class<?> rootClass) {
        final Set<VariableElement> fields = new LinkedHashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement, rootClass);
        while (superclass != null) {
            fields.addAll(findDeclaredFields(superclass));
            superclass = getSuperclassOrNull(superclass, rootClass);
        }

        return fields;
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
        final Set<ExecutableElement> methods = new LinkedHashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement);
        while (superclass != null) {
            methods.addAll(findDeclaredMethods(superclass));
            superclass = getSuperclassOrNull(superclass);
        }

        return methods;
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
        if (equals((TypeElement) ((DeclaredType) typeMirror).asElement(), type)) {
            return true;
        }
        final List<? extends TypeMirror> directSupertypes = types.directSupertypes(typeMirror);
        if (directSupertypes.isEmpty()) {
            return false;
        }
        return directSupertypes.stream().anyMatch(tm -> isSubtype(tm, type));
    }

    public boolean isTopLevelClass(final Element element) {
        return element.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    public String getPackageName(final TypeElement typeElement) {
        return Optional.ofNullable(elements.getPackageOf(typeElement))
                .map(pkgEl -> pkgEl.getQualifiedName().toString())
                .orElse(null);
    }
}