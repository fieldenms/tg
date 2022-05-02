package ua.com.fielden.platform.processors.meta_model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import ua.com.fielden.platform.annotations.meta_model.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

public class EntityFinder {
    
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

   /**
     * The same as {@link ElementFinder#findDeclaredFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
    */
    public static Set<PropertyElement> findDeclaredProperties(EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findDeclaredFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }
    
    public static Set<PropertyElement> findInheritedProperties(EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findInheritedFields(typeElement, ROOT_ENTITY_CLASS).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
    }

    /**
     * Find all inherited properties of an entity that are distinct by a specified condition.
     * @param <T> - type of a mapped property
     * @param mapper - transformation applied to each property for determining its distinctness (e.g. {@link PropertyElement#getName})
     * @return
     */
    public static <T> Set<PropertyElement> findDistinctInheritedProperties(EntityElement entityElement, Function<PropertyElement, T> mapper) {
        Set<PropertyElement> properties = new LinkedHashSet<>();
        Set<T> mappedProperties = new HashSet<>();
        
        for (PropertyElement prop: findInheritedProperties(entityElement)) {
            T mappedProp = mapper.apply(prop);
            if (mappedProperties.contains(mappedProp))
                continue;

            properties.add(prop);
            mappedProperties.add(mappedProp);
        }
        
        return properties;
    }

    /**
     * The same as {@link ElementFinder#findFields(TypeElement)}, but for properties of an {@link AbstractEntity entity}.
     */
    public static Set<PropertyElement> findProperties(EntityElement entityElement) {
        Set<PropertyElement> properties = findDeclaredProperties(entityElement);
        properties.addAll(findInheritedProperties(entityElement));
        return properties;
    }

    /**
     * Find all properties of an entity that are distinct by a specified condition.
     * @param <T> - type of a mapped property
     * @param mapper - transformation applied to each property for determining its distinctness (e.g. {@link PropertyElement#getName})
     * @return
     */
    public static <T> Set<PropertyElement> findDistinctProperties(EntityElement entityElement, Function<PropertyElement, T> mapper) {
        Set<PropertyElement> properties = findDeclaredProperties(entityElement);
        Set<T> mappedProperties = properties.stream().map(mapper).collect(Collectors.toSet());
        
        for (PropertyElement prop: findDistinctInheritedProperties(entityElement, mapper)) {
            T mappedProp = mapper.apply(prop);
            if (mappedProperties.contains(mappedProp))
                continue;

            properties.add(prop);
            mappedProperties.add(mappedProp);
        }
        
        return properties;
    }
    
    public static Pair<String, String> getPropTitleAndDesc(PropertyElement propElement) {
        AnnotationMirror titleAnnotationMirror = ElementFinder.getElementAnnotationMirror(propElement.toVariableElement(), Title.class);
        
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

    public static Pair<String, String> getEntityTitleAndDesc(EntityElement entityElement) {
        final AnnotationMirror entityTitleAnnotMirror = ElementFinder.getElementAnnotationMirror(entityElement.getTypeElement(), EntityTitle.class);
        
        if (entityTitleAnnotMirror == null) {
            return null;
        }
        
        final Map<? extends ExecutableElement, ? extends AnnotationValue> elements = entityTitleAnnotMirror.getElementValues();

        final ExecutableElement valueKey = elements.keySet().stream()
                .filter(k -> k.getSimpleName().toString().equals("value"))
                .findFirst().orElse(null);
        final String title = (valueKey == null) ? "" : (String) elements.get(valueKey).getValue();

        final ExecutableElement descKey = elements.keySet().stream()
                .filter(k -> k.getSimpleName().toString().equals("desc"))
                .findFirst().orElse(null);
        final String desc = (descKey == null) ? "" : (String) elements.get(descKey).getValue();

        return Pair.pair(title, desc);
    }

    /**
     * Entity is any class that inherits from {@link AbstractEntity} (which itself is also considered an entity).
     * @param element
     * @return
     */
    public static boolean isEntity(TypeElement element) {
        if (ElementFinder.equals(element, ROOT_ENTITY_CLASS))
            return true;

        TypeElement superclass = element;
        while ((superclass = ElementFinder.getSuperclassOrNull(superclass)) != null) {
            if (ElementFinder.equals(superclass, ROOT_ENTITY_CLASS))
                return true;
        }
        
        return false;
    }
    
    public static boolean isPersistentEntity(EntityElement element) {
        return element.getTypeElement().getAnnotation(MapEntityTo.class) != null;
    }
    
    public static boolean doesExtendPersistentEntity(EntityElement element) {
        TypeElement superclass = ElementFinder.getSuperclassOrNull(element.getTypeElement(), ROOT_ENTITY_CLASS);
        while (isEntity(superclass)) {
            if (isPersistentEntity(EntityElement.wrapperFor(superclass)))
                return true;
        }
        
        return true;
    }

    public static boolean isDomainEntity(EntityElement element) {
        return element.getTypeElement().getAnnotation(DomainEntity.class) != null;
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

    public static List<? extends AnnotationMirror> getPropertyAnnotations(PropertyElement property) {
        return ElementFinder.getFieldAnnotations(property.toVariableElement());
    }
    
    public static EntityElement getParent(EntityElement element, Elements elementUtils) {
        // superclass can't be null, since every entity extends AbstractEntity
        TypeElement superclass = ElementFinder.getSuperclassOrNull(element.getTypeElement(), ROOT_ENTITY_CLASS);
        
        if (!isEntity(superclass))
            return null;

        return new EntityElement(superclass, elementUtils);
    }
    
    public static List<EntityElement> findParents(EntityElement entityElement, Elements elementUtils) {
        return ElementFinder.findSuperclasses(entityElement.getTypeElement(), ROOT_ENTITY_CLASS, true).stream()
                .map(typeEl -> new EntityElement(typeEl, elementUtils))
                .toList();
    }
}
