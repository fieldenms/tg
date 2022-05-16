package ua.com.fielden.platform.processors.metamodel.elements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class ElementFinder {
    private static final Class<?> DEFAULT_ROOT_CLASS = Object.class;
    
    /**
     * A {@link TypeElement} instance is equal to a {@link Class} instance if both objects have the same qualified (canonical) name.
     * <p>
     * A local class, local interface, or anonymous class does not have a canonical name. 
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-6.html#jls-6.7">Java SE16 Language Specification - Fully Qualified Names and Canonical Names</a>
     */
    public static boolean equals(TypeElement typeElement, Class<?> clazz) {
        return typeElement.getQualifiedName().toString().equals(clazz.getCanonicalName());
    }
    
    public static boolean doesExtend(TypeElement typeElement, Class<?> clazz) {
        final List<TypeElement> superclasses = ElementFinder.findSuperclasses(typeElement, true);
        return superclasses.stream().anyMatch(sup -> equals(sup, clazz));
    }

    /**
     * Returns the immediate parent class of this {@code typeElement} or null if this {@code typeElement} is either an interface type or {@link Object} class or {@code rootClass}.
     * @param typeElement
     * @param rootClass
     * @return
     */
    public static TypeElement getSuperclassOrNull(TypeElement typeElement, Class<?> rootClass) {
        // if this is root class return null
        if (equals(typeElement, rootClass))
            return null;

        // with correct usage this code would never be reached
        // but if supplied root class is not in the type hierarchy then this condition might be reached
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE)
            return null;

        return (TypeElement) ((DeclaredType) superclass).asElement();
    }
    
    /**
     * Returns the immediate parent class of this {@code typeElement} or null if this {@code typeElement} is either an interface type or {@link Object} class.
     * @param typeElement
     * @param rootClass
     * @return
     */
    public static TypeElement getSuperclassOrNull(TypeElement typeElement) {
        return getSuperclassOrNull(typeElement, DEFAULT_ROOT_CLASS);
    }

    /**
     * Returns a list of all superclasses with respect to this {@code typeElement}. The type hierarchy is traversed until {@code rootType} or an interface type or the {@link Object} class is reached.
     * @param typeElement
     * @param rootType
     * @param includeRootType - controls whether the provided {@code rootType} is included in the list.
     * @return
     */
    public static List<TypeElement> findSuperclasses(TypeElement typeElement, Class<?> rootType, boolean includeRootType) {
        List<TypeElement> superclasses = new ArrayList<>();

        TypeMirror superclassTypeMirror = typeElement.getSuperclass();
        TypeElement superclass = null;
        while (superclassTypeMirror.getKind() != TypeKind.NONE) {
            superclass = (TypeElement) ((DeclaredType) superclassTypeMirror).asElement();

            if (equals(superclass, rootType)) {
                if (includeRootType)
                    superclasses.add(superclass);
                break;
            }

            superclasses.add(superclass);
            superclassTypeMirror = superclass.getSuperclass();
        }
        
        // if the last parent element was an interface type, remove it
        if (superclass != null && !superclasses.isEmpty() && (superclass.getKind() == ElementKind.INTERFACE))
            superclasses.remove(superclasses.size() - 1);
        
        return superclasses;
    }

    public static List<TypeElement> findSuperclasses(TypeElement typeElement, boolean includeRootClass) {
        return findSuperclasses(typeElement, DEFAULT_ROOT_CLASS, includeRootClass);
    }
    
    public static Set<VariableElement> findDeclaredFields(TypeElement typeElement, Predicate<VariableElement> predicate) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .filter(predicate)
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    /**
     * Find fields that are explicitly declared by this instance of {@link TypeElement}.
     */
    public static Set<VariableElement> findDeclaredFields(TypeElement typeElement) {
        final Predicate<VariableElement> alwaysTrue = el -> true;
        return findDeclaredFields(typeElement, alwaysTrue);
    }

    public static Set<VariableElement> findInheritedFields(TypeElement typeElement) {
        // use LinkedHashSet to store fields so that they appear in their hierarchical order,
        // that is, fields inherited from the root of the type hierarchy will be placed at the end of the set
        Set<VariableElement> fields = new LinkedHashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement);
        while (superclass != null) {
            fields.addAll(findDeclaredFields(superclass));
            superclass = getSuperclassOrNull(superclass);;
        }

        return fields;
    }

    /**
     * Find declared and inherited fields by this instance of {@link TypeElement}.
     */
    public static Set<VariableElement> findFields(TypeElement typeElement) {
        Set<VariableElement> fields = findDeclaredFields(typeElement);
        fields.addAll(findInheritedFields(typeElement));

        return fields;
    }
    
    /**
     * The same as {@link #findInheritedFields(TypeElement)}, but with limited superclass traversal.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of a superclass of typeElement
     */
    public static Set<VariableElement> findInheritedFields(TypeElement typeElement, Class<?> rootClass) {
        Set<VariableElement> fields = new LinkedHashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement, rootClass);
        while (superclass != null) {
            fields.addAll(findDeclaredFields(superclass));
            superclass = getSuperclassOrNull(superclass, rootClass);;
        }

        return fields;
    }

    /**
     * Find all inherited fields of a type that are distinct by a specified condition.
     * @param <T> - type of mapped field
     * @param mapper - transformation applied to each field for determining its distinctness (e.g. {@link VariableElement#getSimpleName})
     * @return
     */
    public static <T> Set<VariableElement> findDistinctInheritedFields(TypeElement typeElement, Function<VariableElement, T> mapper) {
        Set<VariableElement> fields = new LinkedHashSet<>();
        Set<T> mappedFields = new HashSet<>();

        for (VariableElement field: findInheritedFields(typeElement)) {
            T mappedField = mapper.apply(field);
            if (mappedFields.contains(mappedField))
                continue;

            fields.add(field);
            mappedFields.add(mappedField);
        }

        return fields;       
    }

    /**
     * The same as {@link #findFields(TypeElement)}, but with limited superclass traversal.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of a superclass of typeElement
     */
    public static Set<VariableElement> findFields(TypeElement typeElement, Class<?> rootClass) {
        Set<VariableElement> fields = findDeclaredFields(typeElement);
        fields.addAll(findInheritedFields(typeElement, rootClass));

        return fields;
    }

    /**
     * Find all fields of a type that are distinct by a specified condition.
     * @param <T> - type of mapped field
     * @param mapper - transformation applied to each field for determining its distinctness (e.g. {@link VariableElement#getSimpleName})
     * @return
     */
    public static <T> Set<VariableElement> findDistinctFields(TypeElement typeElement, Function<VariableElement, T> mapper) {
        Set<VariableElement> fields = findDeclaredFields(typeElement);
        Set<T> mappedFields = fields.stream().map(mapper).collect(Collectors.toSet());

        for (VariableElement field: findDistinctInheritedFields(typeElement, mapper)) {
            T mappedField = mapper.apply(field);
            if (mappedFields.contains(mappedField))
                continue;

            fields.add(field);
            mappedFields.add(mappedField);
        }

        return fields;
    }
    
    /**
     * Returns a field with the given name if found, else null. The field might be either declared or inherited.
     * @param typeElement
     * @param fieldName
     * @return
     */
    public static VariableElement findField(TypeElement typeElement, String fieldName) {
        // search in declared fields
        VariableElement field = findDeclaredFields(typeElement).stream()
                .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName))
                .findFirst().orElse(null);
        if (field != null)
            return field;
        
        // search in inherited fields
        field = findInheritedFields(typeElement).stream()
                .filter(varEl -> varEl.getSimpleName().toString().equals(fieldName))
                .findFirst().orElse(null);
        
        return field;
    }
    
    public static Set<VariableElement> findDeclaredFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotationClass) {
        return findDeclaredFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(Collectors.toSet());
    }

    public static Set<VariableElement> findInheritedFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotationClass) {
        return findInheritedFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(Collectors.toSet());
    }

    public static Set<VariableElement> findFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotationClass) {
        return findFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .collect(Collectors.toSet());
    }

    public static List<? extends AnnotationMirror> getFieldAnnotations(VariableElement field) {
        List<AnnotationMirror> annotations = new ArrayList<>();

        // guard against non-fields
        if (field.getKind() != ElementKind.FIELD) {
            return annotations;
        }

        annotations.addAll(field.getAnnotationMirrors());
        
        return annotations;
    }

    public static Set<ExecutableElement> findDeclaredMethods(TypeElement typeElement, Predicate<ExecutableElement> predicate) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.METHOD)
                .map(el -> (ExecutableElement) el)
                .filter(predicate)
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    /**
     * Find methods that are explicitly declared by this {@link TypeElement}.
     */
    public static Set<ExecutableElement> findDeclaredMethods(TypeElement typeElement) {
        final Predicate<ExecutableElement> alwaysTrue = el -> true;
        return findDeclaredMethods(typeElement, alwaysTrue);
    }
    
    public static List<? extends AnnotationMirror> getFieldAnnotationsExcept(VariableElement field, List<Class<? extends Annotation>> ignoredAnnotationsClasses) {
        List<? extends AnnotationMirror> annotations = getFieldAnnotations(field);

        List<String> ignoredAnnotationNames = ignoredAnnotationsClasses.stream()
                .map(annotClass -> annotClass.getCanonicalName())
                .toList();
        
        return annotations.stream()
                .filter(annotMirror -> {
                    String annotQualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
                    return !ignoredAnnotationNames.contains(annotQualifiedName);
                })
                .collect(Collectors.toList());
    }

    public static AnnotationMirror getElementAnnotationMirror(AnnotatedConstruct element, Class<? extends Annotation> annotationClass) {
        for (AnnotationMirror annotMirror: element.getAnnotationMirrors()) {
            TypeElement annotTypeElement = ((TypeElement) annotMirror.getAnnotationType().asElement());
            if (equals(annotTypeElement, annotationClass))
                return annotMirror;
        }

        return null;
    }

    public static String getFieldTypeSimpleName(VariableElement field) {
        return ((DeclaredType) field.asType()).asElement().getSimpleName().toString();
    }
    
    public static boolean isFieldOfType(final VariableElement field, final Class<?> type) {
        final TypeMirror fieldType = field.asType();
        final TypeKind fieldTypeKind = fieldType.getKind();

        if (fieldTypeKind.equals(TypeKind.DECLARED))
            return equals(((TypeElement) ((DeclaredType) fieldType).asElement()), type);
        // TODO implement proper type checking for primitives and arrays
        else
            return false;
    }
    
    public static boolean isFieldOfType(final VariableElement field, final TypeMirror typeMirror, final Types typeUtils) {
        final TypeMirror fieldType = field.asType();
        final TypeKind fieldTypeKind = fieldType.getKind();

        if (fieldTypeKind.equals(TypeKind.DECLARED))
            return typeUtils.isSameType(fieldType, typeMirror);
        // TODO implement proper type checking for primitives and arrays
        else
            return false;
    }

    /**
     * Tests whether a field is of one of the types that are represented by {@code typeElements}.
     * @param field
     * @param typeElements
     * @param typeUtils
     * @return
     */
    public static boolean isFieldOfType(final VariableElement field, final Collection<TypeMirror> typeMirrors, final Types typeUtils) {
        final TypeMirror fieldType = field.asType();
        final TypeKind fieldTypeKind = fieldType.getKind();

        if (fieldTypeKind.equals(TypeKind.DECLARED))
            return typeMirrors.stream().anyMatch(tm -> typeUtils.isSameType(fieldType, tm));
        // TODO implement proper type checking for primitives and arrays
        else
            return false;
    }

    public static boolean isMethodReturnType(final ExecutableElement method, final Class<?> type) {
        final TypeMirror returnType = method.getReturnType();
        final TypeKind returnTypeKind = returnType.getKind();

        if (returnTypeKind.equals(TypeKind.DECLARED))
            return equals(((TypeElement) ((DeclaredType) returnType).asElement()), type);
        // TODO implement proper type checking for primitives and arrays
        else
            return false;
    }

    public static Name getAnnotationMirrorSimpleName(AnnotationMirror annotMirror) {
        return annotMirror.getAnnotationType().asElement().getSimpleName();
    }

    public static long countDeclaredFields(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .count();
    }
    
    public static boolean isStatic(VariableElement varElement) {
        return varElement.getModifiers().contains(Modifier.STATIC);
    }
}