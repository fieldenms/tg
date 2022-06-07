package ua.com.fielden.platform.processors.metamodel.elements;

import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
import ua.com.fielden.platform.utils.Pair;

/**
 * A collection of utility functions to finding various type elements in application to an entity abstraction of type {@link EntityElement}.
 *
 * @author TG Team
 */
public class EntityFinder {
    private EntityFinder() {}
    
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

   /**
    * Finds properties, which are explicitly declared in an entity, represented by {@code entityElement}.
    * <p>
    * Two properties represent an edge-case:
    * <ul>
    * <li> id – only included if declared as a property explicitly.
    * <li> desc – only included if re-declared as a property explicitly. 
    * </ul>
    *
    * @param entityElement
    */
    public static Set<PropertyElement> findDeclaredProperties(final EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        return ElementFinder.findDeclaredFields(typeElement).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Finds properties, which are inherited by entity, represented by {@code entityElement}.
     * <p>
     * Two properties represent an edge-case:
     * <ul>
     * <li>id – only included if this or any of the entities represented by supertypes, is persistent.
     * <li>desc – only included if this or any of the entities represented by supertypes, is annotated with {@code @DescTitle}.
     * </ul>
     *
     * @param entityElement
     * @return
     */
    public static Set<PropertyElement> findInheritedProperties(final EntityElement entityElement) {
        final TypeElement typeElement = entityElement.getTypeElement();
        final Set<PropertyElement> props = ElementFinder.findInheritedFields(typeElement, ROOT_ENTITY_CLASS).stream()
                .filter(EntityFinder::isProperty)
                .map(PropertyElement::new)
                .collect(toCollection(LinkedHashSet::new));
        // let's see if we need to include "id" as a property -- only persistent entities are of interest
        if (EntityFinder.isPersistentEntityType(entityElement) || EntityFinder.doesExtendPersistentEntity(entityElement)) {
            final var idProp = ElementFinder.findField(entityElement.getTypeElement(), AbstractEntity.ID);
            props.add(new PropertyElement(idProp));
        }
        // and now similar for property "desc", which may need to be removed
        if (findAnnotation(entityElement, DescTitle.class).isEmpty()) {
            final var descProp = ElementFinder.findField(entityElement.getTypeElement(), AbstractEntity.DESC);
            props.remove(new PropertyElement(descProp));
        }
        return props;
    }

    /**
     * Find all inherited properties of an entity that are distinct by a specified condition.
     *
     * @param <T> - type of a mapped property
     * @param mapper - transformation applied to each property for determining its distinctness (e.g. {@link PropertyElement#getName})
     * @return
     */
    public static <T> Set<PropertyElement> findDistinctInheritedProperties(final EntityElement entityElement, final Function<PropertyElement, T> mapper) {
        final Set<PropertyElement> properties = new LinkedHashSet<>();
        final Set<T> mappedProperties = new HashSet<>();
        
        for (final PropertyElement prop: findInheritedProperties(entityElement)) {
            final T mappedProp = mapper.apply(prop);
            if (!mappedProperties.contains(mappedProp)) {
                properties.add(prop);
                mappedProperties.add(mappedProp);
            }
        }

        return properties;
    }

    /**
     * Finds all properties for entity represented by {@code entityElement}.
     * The result is the union of result, returned by {@link #findDeclaredProperties(EntityElement)} and {@link #findInheritedProperties(EntityElement)}.
     */
    public static Set<PropertyElement> findProperties(final EntityElement entityElement) {
        final Set<PropertyElement> properties = findDeclaredProperties(entityElement);
        properties.addAll(findInheritedProperties(entityElement));
        return properties;
    }

    /**
     * Find all properties of an entity that are distinct by a specified condition.
     *
     * @param <T> - type of a mapped property
     * @param mapper - transformation applied to each property for determining its distinctness (e.g. {@link PropertyElement#getName})
     * @return
     */
    public static <T> Set<PropertyElement> findDistinctProperties(final EntityElement entityElement, final Function<PropertyElement, T> mapper) {
        final Set<PropertyElement> properties = findDeclaredProperties(entityElement);
        final Set<T> mappedProperties = properties.stream().map(mapper).collect(Collectors.toSet());
        
        for (final PropertyElement prop: findDistinctInheritedProperties(entityElement, mapper)) {
            final T mappedProp = mapper.apply(prop);
            if (!mappedProperties.contains(mappedProp)) {
                properties.add(prop);
                mappedProperties.add(mappedProp);
            }
        }

        return properties;
    }
    
    public static Pair<String, String> getPropTitleAndDesc(final PropertyElement propElement) {
        // TODO need to replicate the logic from TitlesDescsGetter in application to the Mirror types.
        final AnnotationMirror titleAnnotationMirror = ElementFinder.getElementAnnotationMirror(propElement.getVariableElement(), Title.class);

        if (titleAnnotationMirror == null) {
            return null;
        }

        return getTitleAndDesc(titleAnnotationMirror);
    }

    public static Pair<String, String> getEntityTitleAndDesc(final EntityElement entityElement) {
        // TODO need to replicate the logic from TitlesDescsGetter in application to the Mirror types.
        final AnnotationMirror entityTitleAnnotMirror = ElementFinder.getElementAnnotationMirror(entityElement.getTypeElement(), EntityTitle.class);

        if (entityTitleAnnotMirror == null) {
            return null;
        }

        return getTitleAndDesc(entityTitleAnnotMirror);
    }

    /**
     * Reads values for attributes {@code value} and {@code desc} using Mirror API and returns them as a pair.
     * Assumes an empty strings as the default.
     *
     * @param annotationMirror
     * @return
     */
    private static Pair<String, String> getTitleAndDesc(final AnnotationMirror annotationMirror) {
        final var entityTitleAnnotationElements = annotationMirror.getElementValues();
        final ExecutableElement valueKey = entityTitleAnnotationElements.keySet().stream()
                .filter(k -> "value".equals(k.getSimpleName().toString()))
                .findFirst().orElse(null);
        final String title = (valueKey == null) ? "" : (String) entityTitleAnnotationElements.get(valueKey).getValue();

        final ExecutableElement descKey = entityTitleAnnotationElements.keySet().stream()
                .filter(k -> "desc".equals(k.getSimpleName().toString()))
                .findFirst().orElse(null);
        final String desc = (descKey == null) ? "" : (String) entityTitleAnnotationElements.get(descKey).getValue();

        return pair(title, desc);
    }
    
    /**
     * An entity is any class that inherits from {@link AbstractEntity}, which itself is also considered to be an entity.
     *
     * @param element
     * @return
     */
    public static boolean isEntityType(final TypeElement element) {
        return Stream.iterate(element, el -> !ElementFinder.equals(el, Object.class) , el -> ElementFinder.getSuperclassOrNull(el))
               .filter(el -> ElementFinder.equals(el, ROOT_ENTITY_CLASS))
               .findFirst().isPresent();
    }

    /**
     * Any entity annotated with {@code @MapEntityTo} is considered to be a persistent entity.
     *
     * @param element
     * @return
     */
    public static boolean isPersistentEntityType(final EntityElement element) {
        return isEntityType(element.getTypeElement()) && element.getTypeElement().getAnnotation(MapEntityTo.class) != null;
    }
    
    /**
     * Determines whether any of the supertypes for entity represented by {@code element} is a persistent entity.
     * If {@code element} is persistent, but none of its supertypes are persistent, then {@code false} is returned.
     *
     * @param element
     * @return
     */
    public static boolean doesExtendPersistentEntity(final EntityElement element) {
        final TypeElement superclass = element.getTypeElement();
        return Stream.iterate(ElementFinder.getSuperclassOrNull(superclass), el -> !ElementFinder.equals(el, Object.class) , el -> ElementFinder.getSuperclassOrNull(el))
               .filter(el -> isPersistentEntityType(EntityElement.wrapperFor(el)))
               .findFirst().isPresent();
    }

    /**
     * Determines whether {@code element} represents an entity property.
     *
     * @param element
     * @return
     */
    public static boolean isProperty(final VariableElement element) {
        return element.getKind() == ElementKind.FIELD && element.getAnnotation(IsProperty.class) != null;
    }

    /**
     * Identifies whether {@code propElement} represents an entity-typed property. 
     *
     * @param propElement
     * @return
     */
    public static boolean isPropertyOfEntityType(final PropertyElement propElement) {
        try {
            return EntityFinder.isEntityType(propElement.getTypeAsTypeElement());
        } catch (final Exception ex) {
            return false;
        }
    }
    
    public static List<? extends AnnotationMirror> getPropertyAnnotations(final PropertyElement property) {
        return ElementFinder.getFieldAnnotations(property.getVariableElement());
    }
    
    public static EntityElement getParent(final EntityElement element, final Elements elementUtils) {
        // superclass should not be null, because every entity extends AbstractEntity
        final TypeElement superclass = ElementFinder.getSuperclassOrNull(element.getTypeElement(), ROOT_ENTITY_CLASS);
        
        if (!isEntityType(superclass)) {
            return null;
        }

        return new EntityElement(superclass, elementUtils);
    }
    
    /**
     * Looks for annotation of type {@code annotationClass}, declared for entity {@code element} or any of the supertypes for this entity.
     *
     * @param element
     * @param annotationClass
     * @return
     */
    public static <A extends Annotation> Optional<A> findAnnotation(final EntityElement element, final Class<A> annotationClass) {
        if (element.getTypeElement().getAnnotation(annotationClass) != null) {
            return of(element.getTypeElement().getAnnotation(annotationClass));
        }
        return ElementFinder.findSuperclasses(element.getTypeElement(), ROOT_ENTITY_CLASS, true).stream()
                .filter(superEl -> superEl.getAnnotation(annotationClass) != null)
                .map(superEl -> superEl.getAnnotation(annotationClass))
                .findFirst();
    }

    private static String getEntitySimpleName(final String metaModelSimpleName) {
        final int index = metaModelSimpleName.lastIndexOf(MetaModelConstants.META_MODEL_NAME_SUFFIX);
        return index == -1 ? null : metaModelSimpleName.substring(0, index);
    }

    private static String getEntityPackageName(final String metaModelPackageName) {
        final int index = metaModelPackageName.lastIndexOf(MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX);
        return index == -1 ? null : metaModelPackageName.substring(0, index);
    }

    private static String getEntityQualifiedName(final String metaModelQualName) {
        final int lastDot = metaModelQualName.lastIndexOf('.');
        final String metaModelSimpleName = metaModelQualName.substring(lastDot + 1);
        final String metaModelPackageName = metaModelQualName.substring(0, lastDot);

        final String entitySimpleName = getEntitySimpleName(metaModelSimpleName);
        if (entitySimpleName == null) {
            return null;
        }

        final String entityPackageName = getEntityPackageName(metaModelPackageName);
        if (entityPackageName == null) {
            return null;
        }

        return format("%s.%s", entityPackageName, entitySimpleName);
    }
    
    public static EntityElement findEntityForMetaModel(final MetaModelElement mme, final Elements elementUtils) {
        final String entityQualName = getEntityQualifiedName(mme.getQualifiedName().toString());
        if (entityQualName == null) {
            return null;
        }
        final TypeElement typeElement = elementUtils.getTypeElement(entityQualName);
        return typeElement == null ? null : new EntityElement(typeElement, elementUtils);
    }

    public static boolean hasPropertyOfType(final EntityElement entityElement, final TypeMirror type, final Types typeUtils) {
        return EntityFinder.findProperties(entityElement).stream()
               .anyMatch(prop -> typeUtils.isSameType(prop.getType(), type));
    }

    /**
     * A predicate that determines whether {@code element} represent a domain entity type.
     * An element is considered to be a domain entity iff it represents an entity type that is annotated with either {@code @MapEntityTo} or {@code @DomainEntity}.
     *
     * @param element
     * @return
     */
    public static boolean isDomainEntity(final TypeElement element) {
        return EntityFinder.isEntityType(element) && 
               (element.getAnnotation(MapEntityTo.class) != null || element.getAnnotation(DomainEntity.class) != null);
    }

}