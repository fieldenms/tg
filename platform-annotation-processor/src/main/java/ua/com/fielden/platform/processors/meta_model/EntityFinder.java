package ua.com.fielden.platform.processors.meta_model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

public class EntityFinder {
    
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

    public static final List<Class<? extends Annotation>> ignoredPropertyAnnotations() {
        return new ArrayList<>(List.of(IsProperty.class));
    }

   /**
     * The same as {@link ElementFinder#findDeclaredFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
    */
    public static Set<PropertyElement> findDeclaredProperties(EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findDeclaredFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
    }
    
    public static Set<PropertyElement> findInheritedProperties(EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findInheritedFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
    }

    /**
     * The same as {@link ElementFinder#findFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
     */
    public static Set<PropertyElement> findProperties(EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findFields(typeElement, ROOT_ENTITY_CLASS).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
    }

    /**
     * Get all properties of this entity.
     * 
     * @param inheritedPropertiesNames - names of inherited properties that need to be retrieved
     */
    public static Set<PropertyElement> findProperties(EntityElement entityElement, List<String> inheritedPropertiesNames) {
        final Set<PropertyElement> properties = EntityFinder.findDeclaredProperties(entityElement);
        final List<String> declaredPropertiesNames = properties.stream().map(prop -> prop.getName()).toList();

        final List<PropertyElement> inheritedProperties = EntityFinder.findInheritedProperties(entityElement).stream()
                .filter(prop -> {
                    String propName = prop.getName();
                    return inheritedPropertiesNames.contains(propName) &&
                            !(declaredPropertiesNames.contains(propName));
                })
                .toList();
        properties.addAll(inheritedProperties);

        return properties;
    }
    
    public static Pair<String, String> getPropTitleAndDesc(PropertyElement propElement) {
        AnnotationMirror titleAnnotationMirror = ElementFinder.getFieldAnnotationMirror(propElement.toVariableElement(), Title.class);
        
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
    
    public static boolean isEntity(TypeElement element) {
        return element.getAnnotation(MapEntityTo.class) != null;
    }
    
    public static boolean isProperty(VariableElement element) {
        return element.getAnnotation(IsProperty.class) != null;
    }

    public static boolean isPropertyEntityType(PropertyElement propElement) {
        try {
            return EntityFinder.isEntity(propElement.getTypeAsTypeElement());
        } catch (Exception e) {
            return false;
        }
    }

    public static Set<? extends AnnotationMirror> getPropertyAnnotations(PropertyElement property) {
        List<? extends AnnotationMirror> annotations = ElementFinder.getFieldAnnotations(property.toVariableElement());

        List<String> ignoredAnnotationNames = ignoredPropertyAnnotations().stream()
                .map(annotClass -> annotClass.getCanonicalName())
                .toList();

        return annotations.stream()
                .filter(annotMirror -> {
                    String annotQualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
                    return !ignoredAnnotationNames.contains(annotQualifiedName);
                })
                .collect(Collectors.toSet());
    }
}
