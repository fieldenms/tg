package ua.com.fielden.platform.processors.meta_model;

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
    
    public static Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

   /**
     * The same as {@link ElementFinder#findDeclaredFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
    */
    public static Set<PropertyElement> findDeclaredProperties(TypeElement typeElement) {
        return ElementFinder.findDeclaredFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
    }
    
    public static Set<PropertyElement> findInheritedProperties(TypeElement typeElement) {
        return ElementFinder.findInheritedFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
    }

    /**
     * The same as {@link ElementFinder#findFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
     */
    public static Set<PropertyElement> findProperties(TypeElement typeElement) {
        return ElementFinder.findFields(typeElement, ROOT_ENTITY_CLASS).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toSet());
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
    
}
