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

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

public class ElementFinder {

    public static Set<VariableElement> findFields(TypeElement typeElement) {
        Set<VariableElement> fields = new HashSet<>();

        List<VariableElement> enclosedFields = typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .toList();
        fields.addAll(enclosedFields);
        
        return fields;
    }

    public static Set<VariableElement> findInheritedFields(TypeElement typeElement) {
        TypeElement superclass = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        return ElementFinder.findFields(superclass);
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
    
    public static String getVariableTypeSimpleName(VariableElement varElement) {
        return ((DeclaredType) varElement.asType()).asElement().getSimpleName().toString();
    }
    
    public static Name getAnnotationMirrorSimpleName(AnnotationMirror annotMirror) {
        return annotMirror.getAnnotationType().asElement().getSimpleName();
    }

}
