package ua.com.fielden.platform.processors.meta_model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ElementFinder {
    
    /**
     * A {@link TypeElement} instance is equal to a {@link Class} instance if both objects have the same qualified (canonical) name.
     * <p>
     * A local class, local interface, or anonymous class does not have a canonical name. 
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-6.html#jls-6.7">Java SE16 Language Specification - Fully Qualified Names and Canonical Names</a>
     */
    public static boolean equals(TypeElement typeElement, Class<?> clazz) {
        return typeElement.getQualifiedName().toString().equals(clazz.getCanonicalName());
    }
    
    public static TypeElement getSuperclassOrNull(TypeElement typeElement) {
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE)
            return null;

        return (TypeElement) ((DeclaredType) superclass).asElement();
    }

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
     * Find fields that are explicitly declared by this instance of {@link TypeElement}.
     */
    public static Set<VariableElement> findDeclaredFields(TypeElement typeElement) {
        Set<VariableElement> fields = new HashSet<>();

        List<VariableElement> enclosedFields = typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .toList();
        fields.addAll(enclosedFields);
        
        return fields;
    }

    public static Set<VariableElement> findInheritedFields(TypeElement typeElement) {
        Set<VariableElement> fields = new HashSet<>();

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
        Set<VariableElement> fields = new HashSet<>();

        TypeElement superclass = getSuperclassOrNull(typeElement, rootClass);
        while (superclass != null) {
            fields.addAll(findDeclaredFields(superclass));
            superclass = getSuperclassOrNull(superclass, rootClass);;
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

    public static String getVariableTypeSimpleName(VariableElement varElement) {
        return ((DeclaredType) varElement.asType()).asElement().getSimpleName().toString();
    }

    public static Name getAnnotationMirrorSimpleName(AnnotationMirror annotMirror) {
        return annotMirror.getAnnotationType().asElement().getSimpleName();
    }

}
