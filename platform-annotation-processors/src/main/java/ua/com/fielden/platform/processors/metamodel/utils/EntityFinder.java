package ua.com.fielden.platform.processors.metamodel.utils;

import ua.com.fielden.platform.annotations.metamodel.MetaModelForType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.TypeKindVisitor14;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;
import static ua.com.fielden.platform.utils.Pair.pair;

/**
 * A collection of utility functions to finding various elements in application to an entity abstraction of type {@link EntityElement}.
 *
 * @author TG Team
 */
public class EntityFinder extends ElementFinder {
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;
    public static final Class<?> UNION_ENTITY_CLASS = AbstractUnionEntity.class;

    public EntityFinder(final ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    /**
     * Returns the element representing the given entity type.
     *
     * @see ElementFinder#getTypeElement(Class)
     * @throws ElementFinderException if no corresponding type element was found
     */
    public EntityElement findEntity(final Class<? extends AbstractEntity> clazz) {
        return newEntityElement(getTypeElement(clazz));
    }

    /**
     * Returns the element representing the entity type with the specified canonical name.
     *
     * @see ElementFinder#getTypeElement(Class)
     * @throws ElementFinderException if no corresponding type element was found
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

    // TODO possible optimisation: move this method to EntityElement and memoize its result
   /**
    * Collects the elements of {@link #streamDeclaredFields(TypeElement)} into a list.
    *
    * @param entityElement
    */
    public List<PropertyElement> findDeclaredProperties(final EntityElement entityElement) {
        return streamDeclaredProperties(entityElement).toList();
    }

    /**
     * Returns an optional describing a property element with the given name that belongs to the given entity.
     *
     * @param propName  simple property path (limited by a single level of depth)
     */
    public Optional<PropertyElement> findDeclaredProperty(final EntityElement entityElement, final CharSequence propName) {
        return streamDeclaredProperties(entityElement)
                .filter(elt -> elt.getSimpleName().contentEquals(propName))
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
     * Returns an unmodifiable set of properties, which are inherited by an entity.
     * <p>
     * Property uniqueness is described by {@link PropertyElement#equals(Object)}.
     * Entity hierarchy is traversed in natural order.
     * <p>
     * A property is defined simply as a field annotated with {@link IsProperty}. For more detailed processing use
     * {@link #processProperties(Collection, EntityElement)}.
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
     * @param name  simple property path
     */
    public Optional<PropertyElement> findProperty(final EntityElement entityElement, final CharSequence name) {
        return streamProperties(entityElement)
                .filter(elt -> elt.getSimpleName().contentEquals(name))
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
     * Attempts to find an accessor method for the given property by name. The whole entity type hierarchy is searched.
     *
     * @param entity the entity element to be analysed
     * @param propertyName simple property name
     * @return
     */
    public Optional<ExecutableElement> findPropertyAccessor(final EntityElement entity, final CharSequence propertyName) {
        return doFindPropertyAccessor(streamMethods(entity.element()), propertyName);
    }

    /**
     * Attempts to find a declared accessor method for the given property by name.
     *
     * @param entity the entity element to be analysed
     * @param propertyName simple property name
     * @return
     */
    public Optional<ExecutableElement> findDeclaredPropertyAccessor(final EntityElement entity, final CharSequence propertyName) {
        return doFindPropertyAccessor(streamDeclaredMethods(entity.element()), propertyName);
    }

    private Optional<ExecutableElement> doFindPropertyAccessor(final Stream<ExecutableElement> methods, final CharSequence propertyName) {
        final String getName = Accessor.GET.getName(propertyName);
        final String isName = Accessor.IS.getName(propertyName);

        return methods.filter(m -> {
            final Name methodName = m.getSimpleName();
            return methodName.contentEquals(getName) || methodName.contentEquals(isName);
        }).findAny();
    }

    /**
     * Attempts to find a setter method for the given property by name. The whole entity type hierarchy is searched.
     *
     * @param entity the entity element to be analysed
     * @param propertyName simple property name
     * @return
     */
    public Optional<ExecutableElement> findPropertySetter(final EntityElement entity, final CharSequence propertyName) {
        return doFindPropertySetter(streamMethods(entity.element()), propertyName);
    }

    /**
     * Attempts to find a declared setter method for the given property by name.
     *
     * @param entity the entity element to be analysed
     * @param propertyName simple property name
     * @return
     */
    public Optional<ExecutableElement> findDeclaredPropertySetter(final EntityElement entity, final CharSequence propertyName) {
        return doFindPropertySetter(streamDeclaredMethods(entity.element()), propertyName);
    }

    private Optional<ExecutableElement> doFindPropertySetter(final Stream<ExecutableElement> methods, final CharSequence propertyName) {
        final String setterName = Mutator.SETTER.getName(propertyName);
        return methods.filter(m -> m.getSimpleName().contentEquals(setterName)).findAny();
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
     * Returns an annotation value representing the actual key type specified by the {@link KeyType} annotation.
     * <p>
     * A runtime exception is thrown in case {@link KeyType#value()} could not be obtained, which might happend only if
     * {@code annotMirror} does not represent {@link KeyType}.
     */
    public AnnotationValue getKeyTypeAnnotationValue(final AnnotationMirror annotMirror) {
        return findAnnotationValue(annotMirror, "value")
                .orElseThrow(() -> new ElementFinderException("Failed to obtain @KeyType.value() from annotation mirror."));
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
     * Tests whether the type mirror represents an entity type, which is defined as any subtype of {@link AbstractEntity}
     * (itself included).
     */
    public boolean isEntityType(final TypeMirror type) {
        return isSubtype(type, ROOT_ENTITY_CLASS);
    }

    /**
     * Tests whether the type mirror represents a union entity type, which is defined as any subtype of
     * {@link AbstractUnionEntity} (itself included).
     */
    public boolean isUnionEntityType(final TypeMirror type) {
        return isSubtype(type, UNION_ENTITY_CLASS);
    }

    public boolean isSyntheticEntityType(final EntityElement entity) {
        return streamDeclaredFields(entity.element())
                .filter(ElementFinder::isStatic)
                .anyMatch(varElt -> {
                    // static EntityResulQueryModel model_
                    if (varElt.getSimpleName().contentEquals("model_") && isSubtype(varElt.asType(), EntityResultQueryModel.class)) {
                        return true;
                    }
                    // static List<EntityResulQueryModel> models_
                    else if (varElt.getSimpleName().contentEquals("models_") && isSubtype(varElt.asType(), List.class)) {
                        final List<? extends TypeMirror> typeArgs = asDeclaredType(varElt.asType()).getTypeArguments();
                        if (!typeArgs.isEmpty()) {
                            final TypeMirror typeArg = typeArgs.get(0);
                            return isSubtype(typeArg, EntityResultQueryModel.class);
                        }
                    }
                    return false;
                });
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
        return propElement.getType().accept(IS_PROPERTY_OF_DOMAIN_ENTITY_TYPE_VISITOR, null);
    }

    private final TypeKindVisitor14<Boolean, Void> IS_PROPERTY_OF_DOMAIN_ENTITY_TYPE_VISITOR = new TypeKindVisitor14<>() {
        @Override
        public Boolean visitDeclared(DeclaredType t, Void unused) {
            return isEntityThatNeedsMetaModel(asTypeElement(t));
        }

        @Override
        protected Boolean defaultAction(TypeMirror e, Void unused) {
            return false;
        }
    };

    /**
     * Tests whether the property element represents a collectional property.
     * This method is similar to {@link EntityUtils#isCollectional(Class)}.
     */
    public boolean isCollectionalProperty(final PropertyElement property) {
        return isSubtype(property.getType(), Collection.class);
    }

    public boolean isKeyMember(final PropertyElement property) {
        return property.getAnnotation(CompositeKeyMember.class) != null;
    }

    /**
     * A predicate that determines whether {@code element} represent an entity that needs a meta model.
     *
     * @param element
     * @return
     */
    public boolean isEntityThatNeedsMetaModel(final TypeElement element) {
        return isEntityType(element.asType()) &&
               hasAnyPresentAnnotation(element, ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION) ||
               (!isAbstract(element) && (isUnionEntityType(element.asType()) || isSyntheticEntityType(newEntityElement(element))));
    }

    public boolean isEntityThatNeedsMetaModel(final EntityElement element) {
        return isEntityThatNeedsMetaModel(element.element());
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
     * Empty optional is returned if {@code element} represents {@link AbstractEntity} or the element's superclass could
     * not be resolved.
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
     * Returns an optional describing the underlying entity of a given meta-model by analysing its {@link MetaModelForType}
     * annotation. If this annotation is not present, then a warning is reported and an empty optional returned.
     * If the underlying entity is missing (e.g., due to renaming/removal), then an empty optional is returned.
     */
    public Optional<EntityElement> findEntityForMetaModel(final MetaModelElement mme) {
        final MetaModelForType annot = mme.getAnnotation(MetaModelForType.class);
        if (annot == null) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Meta-model [%s] is missing @[%s].".formatted(
                    mme.getQualifiedName(), MetaModelForType.class.getSimpleName()));
            return Optional.empty();
        }
        final TypeMirror entityType = getAnnotationElementValueOfClassType(annot, a -> a.value());
        // missing types have TypeKind.ERROR
        return entityType.getKind() == TypeKind.ERROR ? Optional.empty() : Optional.of(newEntityElement(asTypeElementOfTypeMirror(entityType)));
    }

    /**
     * Returns a new instance of {@link EntityElement} that is composed of the specified type element.
     *
     * @param typeElement
     * @return
     */
    public EntityElement newEntityElement(final TypeElement typeElement) {
        return new EntityElement(typeElement, getPackageOfTypeElement(typeElement).getQualifiedName().toString());
    }

}
