package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
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
     * Returns the element representing the given entity type.
     * 
     * @see ElementFinder#getTypeElement(Class)
     * @throws ElementFinderException if no coresponding type element was found
     */
    public EntityElement findEntity(final Class<? extends AbstractEntity> clazz) {
        return newEntityElement(getTypeElement(clazz));
    }

    /**
     * Returns the element representing the entity type with the specified canonical name.
     * 
     * @see ElementFinder#getTypeElement(Class)
     * @throws ElementFinderException if no coresponding type element was found
     */
    public EntityElement findEntity(final String name) {
        return newEntityElement(getTypeElement(name));
    }

   /**
    * Returns a stream of declared properties.
    *
    * @param entityElement
    */
    public Stream<PropertyElement> streamDeclaredProperties(final EntityElement entityElement) {
        return streamDeclaredFields(entityElement)
                .filter(this::isProperty)
                .map(PropertyElement::new);
    }

   /**
    * Collects the elements of {@link #streamDeclaredFields(TypeElement)} into a list.
    *
    * @param entityElement
    */
    public List<PropertyElement> findDeclaredProperties(final EntityElement entityElement) {
        return streamDeclaredProperties(entityElement).toList();
    }

    /**
     * Returns an optional describing a property element that represents a property of {@code entityElement} named {@code propName}.
     *
     * @param entity
     * @param propName
     * @return
     */
    public Optional<PropertyElement> findDeclaredProperty(final EntityElement entityElement, final String propName) {
        return streamDeclaredProperties(entityElement)
                .filter(elt -> elt.getSimpleName().toString().equals(propName))
                .findFirst();
    }

    /**
     * Returns a stream of all inherited properties with no guarantees on element uniqueness.
     * 
     * @see PropertyElement#equals(Object)
     * @param entityElement
     * @return
     */
    public Stream<PropertyElement> streamInheritedProperties(final EntityElement entityElement) {
        return streamInheritedFields(entityElement, ROOT_ENTITY_CLASS)
                .filter(this::isProperty)
                .map(PropertyElement::new);
    }

    /**
     * Returns an unmodifiable set of properties, which are inherited by entity, represented by {@code entityElement}.
     * <p>
     * Property uniqueness is described by {@link PropertyElement#equals(Object)}.
     * Entity hierarchy is traversed in natural order.
     * <p>
     * A property is defined simply as a field annotated with {@link IsProperty}. For more detailed processing use {@link #processProperties(Set, EntityElement)}.
     *
     * @param entity
     * @return
     */
    public Set<PropertyElement> findInheritedProperties(final EntityElement entity) {
        return streamInheritedFields(entity, ROOT_ENTITY_CLASS)
                .filter(this::isProperty)
                .map(PropertyElement::new)
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Processes properties of the entity element, collecting them into an unmodifiable set with preserved order.
     * <p>
     * The following properties are processed:
     * <ul>
     *  <li>{@code id} – included if this or any of the entities represented by supertypes, is persistent.
     *  <li>{@code desc} – included if this or any of the entities represented by supertypes, declares {@code desc} or is annotated 
     *  with {@code @DescTitle}, excluded otherwise.
     * </ul>
     * 
     * @param properties
     * @param entity
     */
    public Set<PropertyElement> processProperties(final Collection<PropertyElement> properties, final EntityElement entity) {
        final Set<PropertyElement> processed = new LinkedHashSet<>(properties);
        processPropertyId(processed, entity);
        processPropertyDesc(processed, entity);
        return Collections.unmodifiableSet(processed);
    }
    // where
    private void processPropertyId(final Set<PropertyElement> properties, final EntityElement entity) {
        // include property "id" only for persistent entities
        if (isPersistentEntityType(entity) || doesExtendPersistentEntity(entity)) {
            // "id" must exist
            final VariableElement idElt = findField(entity, AbstractEntity.ID)
                    .orElseThrow(() -> new ElementFinderException("Field [%s] was not found in [%s].".formatted(AbstractEntity.ID, entity)));
            properties.add(new PropertyElement(idElt));
        }
    }
    // and
    private void processPropertyDesc(final Set<PropertyElement> properties, final EntityElement entity) {
        // include property "desc" in the following cases:
        // 1. property "desc" is declared by entity or one of its supertypes below AbstractEntity
        // 2. entity or any of its supertypes is annotated with @DescTitle
        final Optional<PropertyElement> maybeDesc = findPropertyBelow(entity, AbstractEntity.DESC, AbstractEntity.class);
        if (maybeDesc.isPresent()) {
            properties.add(maybeDesc.get());
        }
        else if (findAnnotation(entity, DescTitle.class).isPresent()) {
            // "desc" must exist
            final VariableElement descElt = findField(entity, AbstractEntity.DESC)
                    .orElseThrow(() -> new ElementFinderException("Field [%s] was not found in [%s].".formatted(AbstractEntity.DESC, entity)));
            properties.add(new PropertyElement(descElt));
        }
        // in other cases we need to exclude it
        else properties.removeIf(elt -> elt.getSimpleName().toString().equals(AbstractEntity.DESC));
    }

    /**
     * Returns a stream of all properties (both declared and inherited) with no guarantees on element uniqueness.
     *
     * @see PropertyElement#equals(Object)
     * @param entityElement
     */
    public Stream<PropertyElement> streamProperties(final EntityElement entityElement) {
        return Stream.concat(streamDeclaredProperties(entityElement), streamInheritedProperties(entityElement));
    }

    /**
     * Returns an unmodifiable set of all unique properties of the entity element: both declared an inherited.
     * <p>
     * Property uniqueness is described by {@link PropertyElement#equals(Object)}.
     * Entity hierarchy is traversed in natural order.
     */
    public Set<PropertyElement> findProperties(final EntityElement entityElement) {
        final Set<PropertyElement> properties = new LinkedHashSet<>(findDeclaredProperties(entityElement));
        properties.addAll(findInheritedProperties(entityElement));
        return Collections.unmodifiableSet(properties);
    }

    /**
     * Returns an optional describing a property of {@code entityElement} named {@code name}. 
     * <p>
     * Entity type hiearchy is traversed in natural order and the first matching property is returned.
     * 
     * @param entityElement
     * @param name
     * @return
     */
    public Optional<PropertyElement> findProperty(final EntityElement entityElement, final String name) {
        return streamProperties(entityElement)
                .filter(elt -> elt.getSimpleName().toString().equals(name))
                .findFirst();
    }

    /**
     * Finds a property of an entity by traversing it and its hierarchy below {@code rootType}, which is not searched.
     * 
     * @param entity
     * @param name
     * @param rootType the type at which traversal stops
     * @return
     */
    public Optional<PropertyElement> findPropertyBelow(final EntityElement entity, final String name, final Class<?> rootType) {
        if (!isSubtype(entity.asType(), rootType)) {
            return Optional.empty();
        }

        final Stream<EntityElement> parents = streamSuperclassesBelow(entity.element(), rootType).map(this::newEntityElement);
        return Stream.concat(Stream.of(entity), parents)
            .map(elt -> findDeclaredProperty(elt, name))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    /**
     * Returns a pair of property title and description as specified by the annotation {@link Title}.
     * 
     * @param propElement
     * @return
     * @throws ElementFinderException if {@link Title} does not have element {@code desc} 
     *                                or values of elements {@code value} and {@code desc} cannot be type casted to String
     */
    public Pair<String, String> getPropTitleAndDesc(final PropertyElement propElement) {
        return findAnnotationMirror(propElement, Title.class)
                .map(annot -> pair(this.<String> getAnnotationElementValue(annot, "value"), this.<String> getAnnotationElementValue(annot, "desc")))
                .orElse(TitlesDescsGetter.EMPTY_TITLE_AND_DESC);
    }

    /**
     * Returns a pair of entity title and description as specified by the annotation {@link EntityTitle}.
     * 
     * @param propElement
     * @return
     * @throws ElementFinderException if {@link EntityTitle} does not have element {@code desc} 
     *                                or values of elements {@code value} and {@code desc} cannot be type casted to String
     */
    public Pair<String, String> getEntityTitleAndDesc(final EntityElement entityElement) {
        return findAnnotationMirror(entityElement, EntityTitle.class)
                .map(annot -> pair(this.<String> getAnnotationElementValue(annot, "value"), this.<String> getAnnotationElementValue(annot, "desc")))
                .orElseGet(() -> {
                    final String title = TitlesDescsGetter.breakClassName(entityElement.getSimpleName().toString());
                    return pair(title, title + " entity");
                });
    }

    /**
     * Returns the actual key type specified by the {@link KeyType} annotation's value.
     * 
     * @param atKeyType
     * @return
     */
    public TypeMirror getKeyType(final KeyType atKeyType) {
        return getAnnotationElementValueOfClassType(atKeyType, a -> a.value());
    }

    /**
     * Determines the type of key for an entity element by looking for a {@link KeyType} declaration in its type hierarchy.
     * @param entity
     * @return
     */
    public Optional<TypeMirror> determineKeyType(final EntityElement entity) {
        return findAnnotation(entity, KeyType.class).map(this::getKeyType);
    }

    /**
     * Tests whether the type mirror represents an entity type, which is defined as any class that extends {@link AbstractEntity} (itself included).
     *
     * @param element
     * @return
     */
    public boolean isEntityType(final TypeMirror type) {
        return isSubtype(type, ROOT_ENTITY_CLASS);
    }

    /**
     * Any entity annotated with {@code @MapEntityTo} is considered to be a persistent entity.
     *
     * @param element
     * @return
     */
    public boolean isPersistentEntityType(final EntityElement element) {
        return isEntityType(element.asType()) && element.getAnnotation(MapEntityTo.class) != null;
    }

    /**
     * Determines whether any of the supertypes for entity represented by {@code element} is a persistent entity.
     * If {@code element} is persistent, but none of its supertypes are persistent, then {@code false} is returned.
     *
     * @param element
     * @return
     */
    public boolean doesExtendPersistentEntity(final EntityElement element) {
        return streamSuperclasses(element).anyMatch(elt -> isPersistentEntityType(EntityElement.wrapperFor(elt)));
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
     * A predicate to determine if {@code propElement} represents an entity-typed property, which is a domain entity.
     *
     * @param propElement
     * @return
     */
    public boolean isPropertyOfDomainEntityType(final PropertyElement propElement) {
        final TypeMirror type = propElement.getType();
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        return isEntityThatNeedsMetaModel(asTypeElementOfTypeMirror(type));
    }

    /**
     * A predicate that determines whether {@code element} represent an entity that needs a meta model.
     *
     * @param element
     * @return
     */
    public boolean isEntityThatNeedsMetaModel(final TypeElement element) {
        if (!isEntityType(element.asType())) {
            return false;
        }
        return ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION.stream()
                .anyMatch(annotClass -> element.getAnnotation(annotClass) != null);
    }

    /**
     * Returns a stream of entity elements, which are superclasses of the given entity element, up to and including {@link AbstractEntity}.
     * 
     * @param element
     * @return
     */
    public Stream<EntityElement> streamParents(final EntityElement element) {
        return streamSuperclasses(element, ROOT_ENTITY_CLASS).map(this::newEntityElement);
    }

    /**
     * Returns the immediate parent entity type of the entity element. 
     * Empty optional is returned if {@code element} represents {@link AbstractEntity}.
     * 
     * @param element
     * @return
     */
    public Optional<EntityElement> getParent(final EntityElement element) {
        return streamParents(element).findFirst();
    }
    
    /**
     * Looks for annotation of type {@code annotationClass}, declared for entity {@code element} or any of the supertypes for this entity.
     *
     * @param element
     * @param annotationClass
     * @return
     */
    public <A extends Annotation> Optional<A> findAnnotation(final EntityElement element, final Class<A> annotationClass) {
        return Stream.concat(Stream.of(element), streamParents(element))
                .map(elt -> elt.getAnnotation(annotationClass))
                .filter(annot -> annot != null)
                .findFirst();
    }

    /**
     * Returns an optional describing the entity element on which a given meta-model is based by looking at its {@link MetaModelForType} annotation.
     * <p>
     * Entity type might be missing due to renaming/removal. In such cases an empty optional is returned.
     *
     * @param mme
     * @return
     * @throws ElementFinderException if the meta-model element is missing {@link MetaModelForType}
     */
    public Optional<EntityElement> findEntityForMetaModel(final MetaModelElement mme) {
        final MetaModelForType annot = mme.getAnnotation(MetaModelForType.class);
        if (annot == null) {
            throw new ElementFinderException("Meta-model [%s] is missing [%s] annotation.".formatted(
                    mme.getSimpleName(), MetaModelForType.class.getSimpleName()));
        }
        final TypeMirror entityType = getAnnotationElementValueOfClassType(annot, a -> a.value());
        // missing types have TypeKind.ERROR
        return entityType.getKind() == TypeKind.ERROR ? Optional.empty() : Optional.of(newEntityElement(asTypeElementOfTypeMirror(entityType)));
    }

    public EntityElement newEntityElement(final TypeElement typeElement) {
        return new EntityElement(typeElement, getPackageOfTypeElement(typeElement).getQualifiedName().toString());
    }

}