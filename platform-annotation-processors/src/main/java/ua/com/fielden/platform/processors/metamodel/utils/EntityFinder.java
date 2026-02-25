package ua.com.fielden.platform.processors.metamodel.utils;

import ua.com.fielden.platform.annotations.metamodel.MetaModelForType;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.StreamUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.TypeKindVisitor14;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.Pair.pair;

/// A collection of utility functions to finding various elements in application to an entity abstraction of type [EntityElement].
///
public class EntityFinder extends ElementFinder {
    public static final Class<?> ROOT_ENTITY_CLASS = AbstractEntity.class;
    public static final Class<?> UNION_ENTITY_CLASS = AbstractUnionEntity.class;

    public EntityFinder(final ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    /// Returns the element representing the given entity type.
    ///
    /// @see ElementFinder#getTypeElement(Class)
    /// @throws ElementFinderException if no corresponding type element was found
    ///
    public EntityElement findEntity(final Class<? extends AbstractEntity> clazz) {
        return newEntityElement(getTypeElement(clazz));
    }

    /// Returns the element representing the entity type with the specified canonical name.
    ///
    /// @see ElementFinder#getTypeElement(Class)
    /// @throws ElementFinderException if no corresponding type element was found
    ///
    public EntityElement findEntity(final String name) {
        return newEntityElement(getTypeElement(name));
    }

   /// Returns a stream of declared properties.
   ///
   /// * If `entityElement` is a union entity, the stream will contain only the union members.
   ///   In most cases, it makes more sense to use [#streamProperties(EntityElement)] for union entities.
   /// * Otherwise, a property is defined simply as a field annotated with [IsProperty].
   ///   For more detailed processing use [#processProperties(Collection,EntityElement)].
   ///
   /// @see #findDeclaredProperties(EntityElement)
   ///
   public Stream<PropertyElement> streamDeclaredProperties(final EntityElement entityElement) {
        return streamDeclaredFields(entityElement)
                .filter(this::isProperty)
                .map(PropertyElement::new);
    }

    /// Returns a stream of entity elements that form the union `entityElement`.
    ///
    public Stream<EntityElement> streamUnionMembers(final EntityElement entityElement) {
        if (!isUnionEntityType(entityElement)) {
            throw new InvalidArgumentException("Expected a union entity type: [%s].".formatted(entityElement.getQualifiedName()));
        }

        return streamDeclaredProperties(entityElement)
                .filter(prop -> isEntityType(prop.getType()))
                .map(prop -> asTypeElementOfTypeMirror(prop.getType()))
                .map(this::newEntityElement);
    }

    public Stream<PropertyElement> streamCommonPropertiesForUnion(final EntityElement entityElement) {
        if (!isUnionEntityType(entityElement)) {
            throw new InvalidArgumentException("Expected a union entity type: [%s].".formatted(entityElement.getQualifiedName()));
        }

        final List<EntityElement> unionMembers = streamUnionMembers(entityElement).toList();

        // (EntityElement, Properties)
        final var pairs = unionMembers.stream().map(m -> t2(m, streamProperties(m).distinct().toList())).toList();

        return first(pairs).map(fstPair -> {
                    final var restPairs = pairs.subList(1, pairs.size());
                    return fstPair.map((fstType, fstProps) -> fstProps.stream()
                            .filter(fstProp -> restPairs.stream()
                                    .allMatch(pair -> pair.map((otherType, otherProps) -> isPropertyPresent(fstProp, fstType, otherProps, otherType)))));
                })
                .orElseGet(Stream::of);
    }

    /// Returns `true` if `prop` is present among `otherProps`.
    /// In the case of property `key`, special care is taken to determine its type.
    ///
    /// @param propOwner  the type where `prop` is present, required to correctly identify the type of property `key`.
    /// @param otherPropsOwner  the type where `otherProps` are present, required to correctly identify the type of property `key`.
    ///
    private boolean isPropertyPresent(final PropertyElement prop, final EntityElement propOwner, final List<PropertyElement> otherProps, final EntityElement otherPropsOwner) {
        return otherProps.stream()
                .filter(otherProp -> prop.getSimpleName().contentEquals(otherProp.getSimpleName()))
                .anyMatch(otherProp -> {
                    final boolean isKey = KEY.contentEquals(prop.getSimpleName());
                    final var propType = isKey ? determineKeyType(propOwner).orElse(null) : prop.getType();
                    final var otherPropType = isKey ? determineKeyType(otherPropsOwner).orElse(null) : otherProp.getType();
                    return propType != null && otherPropType != null && types.isSameType(propType, otherPropType);
                });
    }

    // TODO possible optimisation: move this method to EntityElement and memoize its result
   /// Collects the elements of [#streamDeclaredFields(TypeElement)] into a list.
   ///
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

    /// Returns a stream of all properties inherited by `entityElement`.
    /// The stream will include hidden properties if they are inherited by `entityElement`.
    /// It is up to the caller to filter them out.
    ///
    /// * If `entityElement` is a union entity, the contents of the stream are unspecified.
    ///   In most cases, it makes more sense to use [#streamProperties(EntityElement)] for union entities.
    /// * Otherwise, a property is defined simply as a field annotated with [IsProperty].
    ///   For more detailed processing use [#processProperties(Collection,EntityElement)].
    /// 
    /// @see #findInheritedProperties(EntityElement)
    ///
    public Stream<PropertyElement> streamInheritedProperties(final EntityElement entityElement) {
        return streamInheritedFields(entityElement, ROOT_ENTITY_CLASS)
                .filter(this::isProperty)
                .map(PropertyElement::new);
    }

    /// Collects the results of [#streamProperties(EntityElement)] into a sequenced set.
    /// Property uniqueness is based on [PropertyElement#equals(Object)].
    /// Hidden properties will be excluded.
    ///
    public SequencedSet<PropertyElement> findInheritedProperties(final EntityElement entity) {
        return streamInheritedProperties(entity).collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSequencedSet));
    }

    /**
     * Processes properties of the entity element, collecting them into an unmodifiable set with preserved order.
     * <p>
     * The following properties are processed:
     * <ul>
     *  <li>{@code id} – included if this or any of the entities represented by supertypes, is persistent,
     *                   or {@code entity} represents a synthetic type that declares {@code id}.
     *  <li>{@code desc} – included if this or any of the entities represented by supertypes, declares {@code desc} or is annotated
     *  with {@code @DescTitle}, excluded otherwise.
     * </ul>
     *
     * @param properties
     * @param entity
     */
    public Set<PropertyElement> processProperties(final Collection<PropertyElement> properties, final EntityElement entity) {
        final Set<PropertyElement> processed = new LinkedHashSet<>(properties);
        maybePropId(entity).ifPresent(processed::add);
        maybePropDesc(entity).ifPresentOrElse(processed::add,
                                              () -> processed.removeIf(elt -> elt.getSimpleName().toString().equals(AbstractEntity.DESC)));
        return Collections.unmodifiableSet(processed);
    }

    /// If property `id` is present in `entity`, returns an optional describing it.
    /// Otherwise, returns an empty optional.
    ///
    /// `id` is considered to be present in:
    /// * persistent entity types and their subtypes;
    /// * union entity types;
    /// * synthetic entity types that explicitly declare `id` or inherit `id` annotated with [IsProperty] (this annotation is absent on [AbstractEntity#id]).
    ///
    public Optional<PropertyElement> maybePropId(final EntityElement entity) {
        if (isPersistentEntityType(entity) || doesExtendPersistentEntity(entity) || isUnionEntityType(entity)) {
            // `id` must exist.
            return Optional.of(new PropertyElement(getField(entity, AbstractEntity.ID)));
        }
        else if (isSyntheticEntityType(entity)) {
            // Will be found iff annotated with `@IsProperty` (i.e., redeclared).
            return findProperty(entity, AbstractEntity.ID);
        }
        else return Optional.empty();
    }

    /// If property `desc` is present in `entity`, returns an optional describing it.
    /// Otherwise, returns an empty optional.
    ///
    /// `desc` is considered to be present if:
    /// * `entity` is a union type, or;
    /// * [DescTitle] is present on the enclosing entity type (directly or indirectly), or;
    /// * property `desc` is explicitly redeclared.
    ///
    public Optional<PropertyElement> maybePropDesc(final EntityElement entity) {
        if (isUnionEntityType(entity)) {
            // `desc` must exist.
            return Optional.of(new PropertyElement(getField(entity, AbstractEntity.DESC)));
        }
        else {
            return findPropertyBelow(entity, AbstractEntity.DESC, AbstractEntity.class)
                    .or(() -> {
                        if (findAnnotation(entity, DescTitle.class).isPresent()) {
                            // `desc` must exist.
                            return Optional.of(new PropertyElement(getField(entity, AbstractEntity.DESC)));
                        }
                        else {
                            return Optional.empty();
                        }
                    });
        }
    }

    /// Returns a stream of all properties present in `entityElement`, starting from declared properties up to those inherited from [AbstractEntity].
    /// The stream will include hidden properties if they exist in `entityElement`.
    /// It is up to the caller to filter them out.
    ///
    /// * If `entityElement` is a union type, the result will include exactly: union members, common properties, `id`, `key`, `desc`.
    /// * Otherwise, a property is defined simply as a field annotated with [IsProperty].
    ///   For more detailed processing use [#processProperties(Collection,EntityElement)].
    ///
    /// @see #findProperties(EntityElement)
    ///
    public Stream<PropertyElement> streamProperties(final EntityElement entityElement) {
        if (isUnionEntityType(entityElement)) {
            // AbstractUnionEntity.key : String
            final var keyPropElt = new PropertyElement(getField(entityElement, KEY)).changeType(asType(String.class));
            return StreamUtils.concat(streamDeclaredProperties(entityElement),
                                      streamCommonPropertiesForUnion(entityElement),
                                      maybePropId(entityElement).stream(),
                                      maybePropDesc(entityElement).stream(),
                                      Stream.of(keyPropElt));
        }
        else {
            return Stream.concat(streamDeclaredProperties(entityElement), streamInheritedProperties(entityElement));
        }
    }

    /// Collects the results of [#streamProperties(EntityElement)] into a sequenced set.
    /// Property uniqueness is based on [PropertyElement#equals(Object)].
    /// Hidden properties will be excluded.
    ///
    public SequencedSet<PropertyElement> findProperties(final EntityElement entityElement) {
        return streamProperties(entityElement).distinct().collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSequencedSet));
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
        return getAnnotationElementValueOfClassType(atKeyType, KeyType::value);
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

    /**
     * Tests whether the entity element represents a union entity type, which is defined as any subtype of
     * {@link AbstractUnionEntity} (itself included).
     */
    public boolean isUnionEntityType(final EntityElement element) {
        return isUnionEntityType(element.asType());
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

    /// Tests whether the property element represents a persistent property.
    ///
    public boolean isPersistentProperty(final PropertyElement prop) {
        return prop.getAnnotation(MapTo.class) != null;
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
               !hasAnnotation(element, WithoutMetaModel.class) &&
               (hasAnyPresentAnnotation(element, ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION) ||
                (!isAbstract(element) && (isUnionEntityType(element.asType()) || isSyntheticEntityType(newEntityElement(element)))));
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

    /// Assuming that `daoElt` represents a DAO companion class, tries to find its corresponding entity type.
    ///
    public Optional<TypeElement> findEntityForDao(final TypeElement daoElt) {
        final var atEntityType = daoElt.getAnnotation(EntityType.class);
        if (atEntityType == null) {
            return Optional.empty();
        }
        else {
            final TypeMirror entityType = getAnnotationElementValueOfClassType(atEntityType, a -> a.value());
            // missing types have TypeKind.ERROR
            return entityType.getKind() == TypeKind.ERROR ? Optional.empty() : Optional.of(asTypeElementOfTypeMirror(entityType));
        }
    }

}
