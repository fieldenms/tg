package ua.com.fielden.platform.processors.meta_model;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

public class EntityFinder {
    
    public static Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

    public static Set<VariableElement> findEntityProperties(TypeElement typeElement) {
        return ElementFinder.findFieldsAnnotatedWith(typeElement, IsProperty.class);
    }

    public static Set<VariableElement> findEntityInheritedProperties(TypeElement typeElement) {
        Set<VariableElement> properties = new HashSet<>();

        TypeElement superclass = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        while (!superclass.getQualifiedName().toString().equals(ROOT_ENTITY_CLASS.getCanonicalName())) {
            properties.addAll(findEntityProperties(superclass));
            superclass = (TypeElement) ((DeclaredType) superclass.getSuperclass()).asElement();
        }
        properties.addAll(findEntityProperties(superclass));

        return properties;
    }
    
    public static AnnotationMirror getPropAnnotationMirror(VariableElement prop, Class<? extends Annotation> annotationClass) {
        final String annotClassCanonicalName = annotationClass.getCanonicalName();
        
        for (AnnotationMirror annotMirror: prop.getAnnotationMirrors()) {
            String qualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
            if (qualifiedName.equals(annotClassCanonicalName)) {
                return annotMirror;
            }
        }
        
        return null;
    }
    
    public static Pair<String, String> getPropTitleAndDesc(VariableElement prop) {
        AnnotationMirror titleAnnotationMirror = getPropAnnotationMirror(prop, Title.class);
        
        if (titleAnnotationMirror == null) {
            return null;
        }
        
        List<Object> values = titleAnnotationMirror.getElementValues().values().stream()
                .map(v -> v.getValue())
                .toList();

        String title = "";
        String desc = "";
        
        try {
            title = (String) values.get(0);
        } catch (Exception e) {
        }

        try {
            desc = (String) values.get(1);
        } catch (Exception e) {
        }
        
        return Pair.pair(title, desc);
    }

    public static Pair<String, String> getEntityKeyTitleAndDesc() {
        return null;
    }
    
    public static Pair<String, String> getEntityDescTitleAndDesc() {
        return null;
    }
}
