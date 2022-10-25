package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.annotations.metamodel.MetaModelForType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.AnomalousStateException;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A collection of utility functions to finding various type elements in application to an entity abstraction of type {@link EntityElement}.
 *
 * @author TG Team
 */
public class EntityFinder extends ElementFinder {
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;

    public EntityFinder(final Elements elements, final Types types) {
        super(elements, types);
    }

    /**
     * Finds an entity described by {@code entityClass}.
     *
     * @param entityClass
     * @return {@link EntityElement} wrapped in an {@link Optional} if found, else an empty optional
     */
    public Optional<EntityElement> findEntity(final Class<? extends AbstractEntity<?>> entityClass) {
        return Optional.ofNullable(elements.getTypeElement(entityClass.getCanonicalName()))
                .map(te -> newEntityElement(te));
    }

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
    public Set<PropertyElement> findDeclaredProperties(final EntityElement entityElement) {
        return findDeclaredFields(entityElement).stream()
                .filter(this::isProperty)
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
    public Set<PropertyElement> findInheritedProperties(final EntityElement entityElement) {
        final Set<PropertyElement> props = findInheritedFields(entityElement, ROOT_ENTITY_CLASS).stream()
                .filter(this::isProperty)
                .map(PropertyElement::new)
                .collect(toCollection(LinkedHashSet::new));
        // let's see if we need to include "id" as a property -- only persistent entities are of interest
        if (isPersistentEntityType(entityElement) || doesExtendPersistentEntity(entityElement)) {
            final var idProp = findField(entityElement, AbstractEntity.ID);
            props.add(new PropertyElement(idProp));
        }
        // and now similar for property "desc", which may need to be removed
        if (findAnnotation(entityElement, DescTitle.class).isEmpty()) {
            final var descProp = findField(entityElement, AbstractEntity.DESC);
            props.remove(new PropertyElement(descProp));
        }
        return props;
    }

    /**
     * Finds all properties for entity represented by {@code entityElement}.
     * The result is the union of result, returned by {@link #findDeclaredProperties(EntityElement)} and {@link #findInheritedProperties(EntityElement)}.
     */
    public Set<PropertyElement> findProperties(final EntityElement entityElement) {
        final Set<PropertyElement> properties = findDeclaredProperties(entityElement);
        properties.addAll(findInheritedProperties(entityElement));
        return properties;
    }
    
    /**
     * Finds a property named {@code name} for entity represented by {@code entityElement}. The whole entity type hierarchy is traversed.
     * @param entityElement
     * @param name
     * @return the property element that was found if any, otherwise {@code null}
     */
    public PropertyElement findProperty(final EntityElement entityElement, final String name) {
        return findProperties(entityElement).stream().filter(propEl -> propEl.getSimpleName().toString().equals(name)).findFirst().orElse(null);
    }

    public Pair<String, String> getPropTitleAndDesc(final PropertyElement propElement) {
        // TODO need to replicate the logic from TitlesDescsGetter in application to the Mirror types.
        final AnnotationMirror titleAnnotationMirror = getElementAnnotationMirror(propElement, Title.class);

        if (titleAnnotationMirror == null) {
            return null;
        }

        return getTitleAndDesc(titleAnnotationMirror);
    }

    public Pair<String, String> getEntityTitleAndDesc(final EntityElement entityElement) {
        final AnnotationMirror entityTitleAnnotMirror = getElementAnnotationMirror(entityElement, EntityTitle.class);

        if (entityTitleAnnotMirror == null) {
            final var title = TitlesDescsGetter.breakClassName(entityElement.getSimpleName().toString());
            return pair(title, title + " entity");
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
     * Retrieves the type of entity key from {@link KeyType} annotation at compile time.
     * @see {@link javax.lang.model.element.Element#getAnnotation(Class)}
     * @param atKeyType
     * @return
     */
    public TypeMirror getKeyType(final KeyType atKeyType) {
        try {
            // should ALWAYS throw, since value() is of type Class, which is unavailable at compile time
            final Class<?> keyType = atKeyType.value();

            // if it somehow was available, then construct a TypeMirror from it
            final TypeElement keyTypeElement = elements.getTypeElement(keyType.getCanonicalName());
            if (keyTypeElement == null) {
                // keyType Class object was available at compile time but could not be found in the processing environment,
                // thus TypeMirror can't be constructed
                throw new AnomalousStateException(
                        "Key type from %s was loaded at compile time, but was not found in the processing environment."
                        .formatted(atKeyType.toString()));
            }
            return keyTypeElement.asType();
        } catch (final MirroredTypeException e) {
//            messager.printMessage(Kind.NOTE, "key type: %s. %s was thrown.".formatted(atKeyType.toString(), e.toString()));
            return e.getTypeMirror();
        }
    }
    
    /**
     * An entity is any class that inherits from {@link AbstractEntity}, which itself is also considered to be an entity.
     *
     * @param element
     * @return
     */
    public boolean isEntityType(final TypeElement element) {
        return Stream.iterate(element, el -> !equals(el, Object.class) , el -> findSuperclass(el))
               .filter(el -> equals(el, ROOT_ENTITY_CLASS))
               .findFirst().isPresent();
    }

    /**
     * Any entity annotated with {@code @MapEntityTo} is considered to be a persistent entity.
     *
     * @param element
     * @return
     */
    public boolean isPersistentEntityType(final EntityElement element) {
        return isEntityType(element) && element.getAnnotation(MapEntityTo.class) != null;
    }
    
    /**
     * Determines whether any of the supertypes for entity represented by {@code element} is a persistent entity.
     * If {@code element} is persistent, but none of its supertypes are persistent, then {@code false} is returned.
     *
     * @param element
     * @return
     */
    public boolean doesExtendPersistentEntity(final EntityElement element) {
        final TypeElement superclass = element;
        return Stream.iterate(findSuperclass(superclass), el -> !equals(el, Object.class) , el -> findSuperclass(el))
               .filter(el -> isPersistentEntityType(EntityElement.wrapperFor(el)))
               .findFirst().isPresent();
    }

    /**
     * Determines whether {@code element} represents an entity property.
     *
     * @param element
     * @return
     */
    public boolean isProperty(final VariableElement element) {
        return element.getKind() == ElementKind.FIELD && element.getAnnotation(IsProperty.class) != null;
    }

    /**
     * Identifies whether {@code propElement} represents an entity-typed property, which may not necessarily be a domain entity. 
     *
     * @param propElement
     * @return
     */
    public boolean isPropertyOfEntityType(final PropertyElement propElement) {
        try {
            return isEntityType(propElement.getTypeAsTypeElement());
        } catch (final Exception ex) {
            // an exception may be thrown is property type is not a DECLARED type (i.e., not a class or an interface)
            return false;
        }
    }

    /**
     * A predicate to determine if {@code propElement} represents an entity-typed property, which is a domain entity.
     *
     * @param propElement
     * @return
     */
    public boolean isPropertyOfDomainEntityType(final PropertyElement propElement) {
        try {
            return isEntityThatNeedsMetaModel(propElement.getTypeAsTypeElement());
        } catch (final Exception ex) {
            // an exception may be thrown is property type is not a DECLARED type (i.e., not a class or an interface)
            return false;
        }
    }

    /**
     * A predicate that determines whether {@code element} represent an entity that needs a meta model.
     *
     * @param element
     * @return
     */
    public boolean isEntityThatNeedsMetaModel(final TypeElement element) {
        if (!isEntityType(element)) {
            return false;
        }
        return ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION.stream()
                .anyMatch(annotClass -> element.getAnnotation(annotClass) != null);
    }

    public EntityElement getParent(final EntityElement element) {
        // superclass should not be null, because every entity extends AbstractEntity
        final TypeElement superclass = findSuperclass(element);
        return isEntityType(superclass) ? newEntityElement(superclass) : null;
    }
    
    /**
     * Looks for annotation of type {@code annotationClass}, declared for entity {@code element} or any of the supertypes for this entity.
     *
     * @param element
     * @param annotationClass
     * @return
     */
    public <A extends Annotation> Optional<A> findAnnotation(final EntityElement element, final Class<A> annotationClass) {
        if (element.getAnnotation(annotationClass) != null) {
            return of(element.getAnnotation(annotationClass));
        }
        return findSuperclasses(element, ROOT_ENTITY_CLASS).stream()
                .filter(superEl -> superEl.getAnnotation(annotationClass) != null)
                .map(superEl -> superEl.getAnnotation(annotationClass))
                .findFirst();
    }

    /**
     * Finds the entity on which a given meta-model is based by looking at its {@link MetaModelForType} annotation.
     *
     * @param mme
     * @return
     */
    public EntityElement findEntityForMetaModel(final MetaModelElement mme) {
        final TypeMirror entityType = mme.getEntityType();
        return entityType == null || entityType.getKind() == TypeKind.ERROR ? null : newEntityElement(toTypeElement(entityType));
    }

    public boolean hasPropertyOfType(final EntityElement entityElement, final TypeMirror type, final Types typeUtils) {
        return findProperties(entityElement).stream()
               .anyMatch(prop -> typeUtils.isSameType(prop.getType(), type));
    }

    public EntityElement newEntityElement(final TypeElement typeElement) {
        return new EntityElement(typeElement, getPackageName(typeElement));
    }

}