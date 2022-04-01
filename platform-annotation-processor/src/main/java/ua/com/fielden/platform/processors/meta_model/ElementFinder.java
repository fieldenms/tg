package ua.com.fielden.platform.processors.meta_model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Find declared and inherited fields by this instance of {@link TypeElement}.
     */
    public static Set<VariableElement> findFields(TypeElement typeElement) {
        Set<VariableElement> fields = findDeclaredFields(typeElement);

        TypeMirror superclass = typeElement.getSuperclass();
        while (superclass.getKind() != TypeKind.NONE) {
            TypeElement superclassTypeElement = (TypeElement) ((DeclaredType) superclass).asElement();
            fields.addAll(findDeclaredFields(superclassTypeElement));
            superclass = superclassTypeElement.getSuperclass();
        }

        return fields;
    }
    
    public static Set<VariableElement> findInheritedFields(TypeElement typeElement) {
        TypeElement superclassTypeElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        return findFields(superclassTypeElement);
    }

    /**
     * The same as {@link #findFields(TypeElement)}, but with limited superclass traversal.
     * 
     * @param typeElement - target element which fields are to be found
     * @param rootClass - upper limit (included) of superclasses to typeElement
     */
    public static Set<VariableElement> findFields(TypeElement typeElement, Class<?> rootClass) {
        Set<VariableElement> fields = findDeclaredFields(typeElement);

        TypeElement superclassTypeElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        while (!superclassTypeElement.getQualifiedName().toString().equals(rootClass.getCanonicalName())) {
            fields.addAll(findDeclaredFields(superclassTypeElement));
            superclassTypeElement = (TypeElement) ((DeclaredType) superclassTypeElement.getSuperclass()).asElement();
        }
        fields.addAll(findDeclaredFields(superclassTypeElement));

        return fields;
    }

    public static Set<VariableElement> findFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotationClass) {
        return findDeclaredFields(typeElement).stream()
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

    public static AnnotationMirror getFieldAnnotationMirror(VariableElement varElement, Class<? extends Annotation> annotationClass) {
        final String annotClassCanonicalName = annotationClass.getCanonicalName();
        
        for (AnnotationMirror annotMirror: varElement.getAnnotationMirrors()) {
            String qualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
            if (qualifiedName.equals(annotClassCanonicalName)) {
                return annotMirror;
            }
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
