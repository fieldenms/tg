package ua.com.fielden.platform.entity.query;

import com.google.common.collect.ImmutableList;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EntityRetrievalModelException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.utils.ToString;
import ua.com.fielden.platform.utils.ToString.IFormat;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.fetch.ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_ONLY;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.Wrapper.unwrap;
import static ua.com.fielden.platform.reflection.Finder.commonPropertiesForUnion;
import static ua.com.fielden.platform.reflection.Finder.unionProperties;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.ImmutableListUtils.prepend;
import static ua.com.fielden.platform.utils.ToString.separateLines;

/// Represents retrieval models specialised for entity types.
///
/// Retrieval model `RM` is constructed from [fetch model][fetch] `FM` as follows:
///
///   1. [FetchCategory] is used to construct the initial parts of `RM` (more details are provided below).
///   2. Any properties explicitly excluded by `FM` are excluded from `RM`.
///   3. Any properties explicitly included by `FM` are included into `RM`.
///      Any sub-fetch models among them are combined with those used during the previous steps.
///      For example, given entity-typed property `P`, if the fetch category resulted in a sub-retrieval model
///      `RM_P` (with an underlying fetch model `RMFM_P`) for `P`,
///      and `P` is explicitly included into `FM` with a sub-fetch model `FM_P`,
///      then `FM_P` is combined with `RMFM_P` to produce the final sub-retrieval model for `P`.
///   4. The set of proxied properties is constructed.
///
/// #### Processing of entity-typed properties
/// Property `P` with entity type `E` may be explored during construction of a retrieval model,
/// which will result in a richer sub-model for that property.
///
/// If `P` is explored, then:
///
/// 1. If `E` is a union entity, `FetchCategory#ALL` is used.
/// 2. Otherwise, `FetchCategory#DEFAULT` is used.
///
/// If `P` is not explored, then:
///
/// 1. If `E` is a persistent entity, `FetchCategory#ID_ONLY` is used.
/// 2. Otherwise, `P` is ignored and no sub-model is constructed for it.
///
/// #### Fetch categories
/// See the documentation [FetchCategory] for a description of each category.
///
/// Please note that [FetchCategory#ALL] is equivalent to [FetchCategory#DEFAULT], but without special handling of entity-typed keys and key members.
///
/// #### Processing of property `key`
///
/// * If an entity has a composite key, the retrieval model is expanded to include all the key members, which are always explored further.
///   Property `key` itself is never included in such cases as it is effective a "virtual" property, having different types at the domain model and EQL levels.
///   More specifically, it is [DynamicEntityKey] at the domain model level, and [String] in EQL, where it is implicitly calculated
///   (key members are converted to string and concatenated with a key member separator).
///   If such `key` were to be retrieved, then it would be required to parse its string value into a [DynamicEntityKey].
///   Instead, a composite `key` is never included in a retrieval model, and the constructor of [AbstractEntity] takes care of initialising its value.
/// * If an entity is a union, `key` is included together with all the union members, which are always explored further.
///
/// #### Implementation details
///
/// When inspecting property types for a potential entity type,
/// method [PropertyTypeMetadata.Wrapper#unwrap(PropertyTypeMetadata)] is used because of the way fetch models are constructed -- heuristically.
/// One case where unwrapping is needed is collectional properties where fetch models know only about the collectional element type.
///
public final class EntityRetrievalModel<T extends AbstractEntity<?>> implements IRetrievalModel<T>, ToString.IFormattable {

    public static final String ERR_NO_SUCH_PROPERTY_IN_MODEL = "No such property [%s] in retrieval model:%n%s",
                               ERR_EXPECTED_TO_FIND_ENTITY_TYPED_PROPERTY_EXCLUDED_FROM_FETCH = "Couldn't find entity-typed property [%s] to be excluded from fetched properties of entity type [%s].",
                               ERR_EXPECTED_TO_FIND_PROPERTY_EXCLUDED_FROM_FETCH = "Couldn't find property [%s] to be excluded from fetched properties of entity type [%s].",
                               ERR_NON_EXISTING_PROPERTY = "Trying to fetch entity [%s] with non-existing property [%s].";

    private static final String WARN_GRAPH_CYCLE = """
      Cycle detected in entity graph. Retrieval model will be truncated.
      Retrieval models stack (first element is the top):
      %s""";

    private static final Logger LOGGER = getLogger();

    private final fetch<T> originalFetch;
    /// Indicates whether this fetch is the top-most (graph root) or a nested one (subgraph).
    private final boolean topLevel;
    /// Association between an entity-typed property and its nested fetch model.
    private final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps;
    /// Primitive properties that should be retrieved.
    private final Set<String> primProps;
    /// Properties that should be proxied in the resulting entity proxy instance.
    private final Set<String> proxiedProps;

    public EntityRetrievalModel(final fetch<T> originalFetch, final IDomainMetadata domainMetadata, final QuerySourceInfoProvider qsip) {
        this(originalFetch, domainMetadata, qsip, true);
    }

    EntityRetrievalModel(
            final fetch<T> originalFetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip,
            final boolean topLevel)
    {
        this(originalFetch, domainMetadata, qsip, topLevel, ImmutableList.of(new StackElement(originalFetch)));
    }

    /// @param stack  a stack of elements representing retrieval model exploration to identify and handle cycles.
    ///
    private EntityRetrievalModel(
            final fetch<T> originalFetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip,
            final boolean topLevel,
            final List<StackElement> stack)
    {
        this.originalFetch = originalFetch;
        this.topLevel = topLevel;

        final var builder = buildModel(originalFetch, domainMetadata, qsip, stack);
        this.primProps = unmodifiableSet(builder.primProps);
        this.entityProps = unmodifiableMap(builder.entityProps);
        this.proxiedProps = unmodifiableSet(builder.proxiedProps);
    }

    private static <T extends AbstractEntity<?>> Builder buildModel(
            final fetch<T> originalFetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip,
            final List<StackElement> stack)
    {
        final var builder = new Builder(originalFetch.getEntityType(), domainMetadata, qsip, stack);

        switch (originalFetch.getFetchCategory()) {
            case ALL_INCL_CALC -> builder.includeAllFirstLevelPropsInclCalc();
            case ALL -> builder.includeAllFirstLevelProps();
            case DEFAULT -> builder.includeAllFirstLevelPrimPropsAndKey();
            case KEY_AND_DESC -> builder.includeKeyAndDescOnly();
            case ID_AND_VERSION -> builder.includeIdAndVersionOnly();
            case ID_ONLY -> builder.includeIdOnly();
            case NONE -> {}
        }

        for (final String propName : originalFetch.getExcludedProps()) {
            builder.without(propName);
        }

        for (final String propName : originalFetch.getIncludedProps()) {
            builder.with(propName);
        }

        originalFetch.getIncludedPropsWithModels().forEach(builder::with);

        builder.populateProxies();

        return builder;
    }

    public fetch<T> getOriginalFetch() {
        return originalFetch;
    }

    public boolean isFetchIdOnly() {
        return entityProps.isEmpty() && primProps.size() == 1 && primProps.contains(ID);
    }

    @Override
    public Class<T> getEntityType() {
        return originalFetch.getEntityType();
    }

    @Override
    public boolean isInstrumented() {
        return originalFetch.isInstrumented();
    }

    @Override
    public Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels() {
        return entityProps;
    }

    @Override
    public IRetrievalModel<? extends AbstractEntity<?>> getRetrievalModel(final CharSequence path) {
        final var model = getRetrievalModelOrNull(path);
        if (model == null) {
            throw new EntityRetrievalModelException(ERR_NO_SUCH_PROPERTY_IN_MODEL.formatted(path, this));
        }
        return model;
    }

    @Override
    public Optional<IRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModelOpt(final CharSequence path) {
        return Optional.ofNullable(getRetrievalModelOrNull(path));
    }

    private @Nullable EntityRetrievalModel<?> getRetrievalModelOrNull(final CharSequence path) {
        final var names = splitPropPathToArray(path);

        EntityRetrievalModel<?> model = this;
        for (int i = 0; i < names.length && model != null; i++) {
            model = model.entityProps.get(names[i]);
        }

        return model;
    }

    @Override
    public Set<String> getPrimProps() {
        return primProps;
    }

    @Override
    public Set<String> getProxiedProps() {
        return proxiedProps;
    }

    @Override
    public boolean containsProp(final String propName) {
        return primProps.contains(propName) || entityProps.containsKey(propName);
    }

    @Override
    public boolean containsProxy(final String propName) {
        return proxiedProps.contains(propName);
    }

    @Override
    public boolean isTopLevel() {
        return topLevel;
    }

    @Override
    public String toString() {
        return toString(separateLines());
    }

    @Override
    public String toString(final IFormat format) {
        return format.toString(this)
                .add("category", originalFetch.getFetchCategory())
                .addIfNotEmpty("primitives", primProps)
                .addIfNotEmpty("proxied", proxiedProps)
                .addIfNotEmpty("subModels", entityProps)
                .$();
    }

   /// Mutable builder for initialisation of [EntityRetrievalModel].
   /// Its output is stored in [#primProps], [#entityProps], and [#proxiedProps].
   ///
   /// Both [IDomainMetadata] and [QuerySourceInfoProvider] are required to correctly build a retrieval model.
   ///
   /// * [IDomainMetadata] is required because of the richness of information it provides about entity types and their properties.
   /// * [QuerySourceInfoProvider] is required because it contains EQL-specific information not provided by [IDomainMetadata]
   ///   (e.g., properties that are present in the yields of synthetic entity models).
   ///
    private static final class Builder {
        final Class<? extends AbstractEntity<?>> entityType;
        final IDomainMetadata domainMetadata;
        final EntityMetadataUtils entityMetadataUtils;
        final PropertyMetadataUtils propMetadataUtils;
        final EntityMetadata entityMetadata;
        final QuerySourceInfoProvider qsip;
        final QuerySourceInfo<?> querySourceInfo;
        final List<StackElement> stack;

        // Mutable components that are being built

        final Set<String> primProps;
        final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps;
        final Set<String> proxiedProps;

        Builder(final Class<? extends AbstractEntity<?>> entityType,
                final IDomainMetadata domainMetadata,
                final QuerySourceInfoProvider qsip,
                final List<StackElement> stack)
        {
            this.domainMetadata = domainMetadata;
            this.qsip = qsip;
            this.stack = stack;
            this.querySourceInfo = qsip.getModelledQuerySourceInfo(entityType);
            this.entityType = entityType;
            this.entityMetadata = domainMetadata.forEntity(entityType);
            this.entityMetadataUtils = domainMetadata.entityMetadataUtils();
            this.propMetadataUtils = domainMetadata.propertyMetadataUtils();
            this.primProps = new HashSet<>();
            this.entityProps = new HashMap<>();
            this.proxiedProps = new HashSet<>();
        }

        private boolean containsProp(final String propName) {
            return primProps.contains(propName) || entityProps.containsKey(propName);
        }

       /// Performs an action for each _modelled_ property (see [QuerySourceInfoProvider]) of the entity type.
       ///
       /// The action is supplied with an optional [PropertyMetadata] that may be absent.
       /// Refer to [QuerySourceInfoProvider] for a description of when such cases may occur.
       ///
        private void forEachProperty(final BiConsumer<? super AbstractQuerySourceItem<?>, ? super Optional<PropertyMetadata>> fn) {
            querySourceInfo.getProps().values()
                    .forEach(prop -> fn.accept(prop, entityMetadata.propertyOpt(prop.name)));
        }

        private void populateProxies() {
            // Collect properties from both QSIP and metadata.
            // QSIP may include properties that are not present in metadata.
            // This is often the case for synthetic entities.
            // It would not be possible to proxy non-yielded properties of synthetic entities (absent from QSIP),
            // without considering properties from metadata.
            Stream.concat(querySourceInfo.getProps().values().stream().map(prop -> prop.name),
                          entityMetadata.properties().stream().map(PropertyMetadata::name))
                    .distinct()
                    // A common filter for all properties.
                    .filter(propName -> {
                        // id is never proxied.
                        if (ID.equals(propName)) {
                            return false;
                        }
                        // version is never proxied for synthetic-based-on-persistent entities.
                        else if (VERSION.equals(propName) && entityMetadata.isSynthetic() && isSyntheticBasedOnPersistentEntityType(entityMetadata.javaType())) {
                            return false;
                        }
                        else if (containsProp(propName)) {
                            return false;
                        }
                        else if (propName.contains(".")) {
                            return false;
                        }
                        else return true;
                    })
                    // Special filter for properties that have metadata.
                    .filter(propName -> entityMetadata.propertyOpt(propName)
                            .map(propMetadata -> {
                                if (propMetadata.isCritOnly()) {
                                    return false;
                                }
                                else if (propMetadata.type().isCollectional()) {
                                    return false;
                                }
                                // Proxying of plain properties makes sense only for synthetic entities.
                                else if (propMetadata.isPlain() && !entityMetadata.isSynthetic()) {
                                    return false;
                                }
                                // Non-persistent key is never proxied.
                                else if (KEY.equals(propMetadata.name()) && !propMetadata.isPersistent()) {
                                    return false;
                                }
                                // Do not proxy common properties and `desc` in union entities.
                                // Instead, illegal access to proxied properties should be detected when the underlying properties of the union members themselves are accessed.
                                else if (entityMetadata.isUnion()
                                         && (DESC.equals(propMetadata.name())
                                             || commonPropertiesForUnion((Class<? extends AbstractUnionEntity>) entityMetadata.javaType()).contains(propMetadata.name())))
                                {
                                    return false;
                                }
                                else return true;
                            })
                            .orElse(TRUE))
                    .forEach(proxiedProps::add);
        }

        private void includeAllCompositeKeyMembers() {
            entityMetadataUtils.compositeKeyMembers(entityMetadata)
                    .forEach(pm -> with(pm.name()));
        }

        private void includeAllUnionEntityKeyMembers() {
            final var unionType = entityMetadata.asUnion().orElseThrow().javaType();
            unionProperties(unionType).forEach(prop -> with(prop.getName()));
        }

        private void includeAllFirstLevelPrimPropsAndKey() {
            // Always include `desc`.
            // This category should be a superset of KEY_AND_DESC.
            if (entityMetadata.hasProperty(DESC)) {
                primProps.add(DESC);
            }

            forEachProperty((prop, optPropMetadata) -> {
                // Exclude all calculated properties except for components (legacy EQL2 behaviour).
                // TODO: Don't treat calculated components specially once TG applications no longer rely on this behaviour.
                if (optPropMetadata.filter(it -> it.isCalculated() && !it.type().isComponent()).isPresent()) {} // skip
                else {
                    // Recursive key structures are not supported, and will lead to non-termination (see #2452).

                    // Explore further if property is a union member or a key member.
                    final boolean exploreEntities = optPropMetadata
                            .map(propMetadata -> entityMetadata.isUnion() && propMetadata.type().isEntity()
                                                 || KEY.equals(propMetadata.name())
                                                 || propMetadata.has(KEY_MEMBER))
                            .orElse(TRUE);

                    with(prop.name, !exploreEntities);
                }
            });
        }

        private void includeLastUpdatedByGroupOfProperties() {
            if (AbstractPersistentEntity.class.isAssignableFrom(entityType)) {
                with(LAST_UPDATED_BY, true);
                primProps.add(LAST_UPDATED_DATE);
                primProps.add(LAST_UPDATED_TRANSACTION_GUID);
            }
        }

        private void includeKeyAndDescOnly() {
            includeIdAndVersionOnly();

            with(KEY, false);

            if (entityMetadata.hasProperty(DESC)) {
                primProps.add(DESC);
            }
        }

        private void includeAllFirstLevelProps() {
            // Always include `desc`.
            // This category should be a superset of KEY_AND_DESC.
            if (entityMetadata.hasProperty(DESC)) {
                primProps.add(DESC);
            }

            forEachProperty((prop, optPropMetadata) -> {
                // Exclude all calculated properties except for components (legacy EQL2 behaviour).
                // TODO: Don't treat calculated components specially once TG applications no longer rely on this behaviour.
                if (optPropMetadata.filter(it -> it.isCalculated() && !it.type().isComponent()).isPresent()) {} // skip
                else {
                    with(prop.name);
                }
            });
        }

        private void includeAllFirstLevelPropsInclCalc() {
            forEachProperty((prop, optPropMetadata) -> with(prop.name));
        }

        private void includeIdOnly() {
            primProps.add(ID);
        }

        private void includeIdAndVersionOnly() {
            // NOTE: Shouldn't this category produce a superset of ID_ONLY?
            //       It does not always include ID, unlike ID_ONLY.
            if (querySourceInfo.hasProp(ID)) {
                primProps.add(ID);
            }
            if (entityMetadata.isPersistent()) {
                primProps.add(VERSION);
                if (isActivatableEntityType(entityType)) {
                    primProps.add(ACTIVE);
                    primProps.add(REF_COUNT);
                }
                includeLastUpdatedByGroupOfProperties();
            }
        }

        /// Includes the property and explores it further, if it is entity-typed.
        ///
        private void with(final String propName) {
            with(propName, false);
        }

        private void with(final String propName, final boolean skipEntities) {
            getPropMetadata(propName).ifPresentOrElse(pm -> {
                if (pm.type().isCompositeKey()) {
                    // Do not include the key itself.
                    // See the documentation of this class.
                    includeAllCompositeKeyMembers();
                } else if (propName.equals(KEY) && entityMetadata.isUnion()) {
                    primProps.add(KEY);
                    includeAllUnionEntityKeyMembers();
                } else if (entityMetadata.isUnion() && commonPropertiesForUnion(entityMetadata.asUnion().orElseThrow().javaType()).contains(propName)) {
                    includeCommonUnionProperty(propName, skipEntities);
                } else {
                    final var propType = unwrap(pm.type());
                    // Treat PropertyDescriptor as primitive, it does not make sense to fetch its sub-properties.
                    // TODO: The commented out condition !optPm.isId() is a very old and its intent is not known.
                    //       It was migrated from the SVN repo on Jun 5, 2014.
                    //       https://github.com/fieldenms/tg/commit/40cc8e3bbe19b9100718c1476780dc9be46b0915#diff-5561eb2f0bc449a4430ec09dcf2b9f76d64fc8205771489eae8ba1472664233cR134
                    if (propType instanceof PropertyTypeMetadata.Entity et && !PropertyDescriptor.class.equals(et.javaType())/* && !optPm.isId()*/) {
                        if (!skipEntities) {
                            if (propMetadataUtils.isPropEntityType(propType, EntityMetadata::isUnion)) {
                                with(propName, fetchAll(et.javaType()));
                            }
                            // Simple entity-typed key, where entity-type represents the left side of a one-2-one relationship.
                            else if (propName.equals(KEY)) {
                                with(propName, fetchKeyAndDescOnly(et.javaType()));
                            }
                            else {
                                with(propName, EntityQueryUtils.fetch(et.javaType()));
                            }
                        } else if (pm.isPersistent()) {
                            with(propName, fetchIdOnly(et.javaType()));
                        }
                    } else {
                        getSinglePropertyOfComponentType(pm).ifPresent(prop -> primProps.add(propName + "." + prop));
                        primProps.add(propName);
                    }
                }
            }, () -> primProps.add(propName)); // if PropertyMetadata is missing and this is considered legal -- add it as primitive property
        }

       /// Includes common union property `commonPropName` by adding it to the retrieval model of each union member.
       /// This property will not be directly included in this retrieval model, but only indirectly -- through union members.
       ///
       /// @param skipEntities  whether to skip exploration of the common property if it is entity-typed
       ///
       private void includeCommonUnionProperty(final String commonPropName, final boolean skipEntities) {
           final var unionType = entityMetadata.asUnion().orElseThrow().javaType();
           unionProperties(unionType)
                   .stream()
                   .map(prop -> entityMetadata.property(prop.getName()))
                   .forEach(memberPropMetadata -> {
                       final var memberType = memberPropMetadata.type().asEntity().orElseThrow().javaType();
                       final var commonPropMetadata = domainMetadata.forProperty(memberType, commonPropName);
                       final var maybeCommonPropFetch = commonPropMetadata.type().asEntity().map(it -> skipEntities ? fetchIdOnly(it.javaType()) : null);
                       // Use id-only fetch for the union member.
                       // If anything else needs to be retrieved, it will be added by other methods and combined into one model.
                       final var memberPropFetch = maybeCommonPropFetch
                               .<fetch<? extends AbstractEntity<?>>>map(commonPropFetch -> fetchIdOnly(memberType).with(commonPropName, commonPropFetch))
                               .orElseGet(() -> fetchIdOnly(memberType).with(commonPropName));
                       with(memberPropMetadata.name(), memberPropFetch);
                   });
       }

       private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
            final var stackElement = new StackElement(propName, fetchModel);

            // If a cycle is detected, override `fetchModel` with an ID_ONLY one.
            // This is a form of partial support for cycles.
            if (stack.stream().anyMatch(elt -> elt.fetch().equals(fetchModel))) {
                LOGGER.warn(() -> format(WARN_GRAPH_CYCLE,
                                         StreamUtils.prepend(stackElement, stack.stream())
                                                 .map(elt -> format("%s: (%s, %s)",
                                                                    elt.property(),
                                                                    elt.fetch().getEntityType().getSimpleName(),
                                                                    elt.fetch().getFetchCategory()))
                                                 .collect(joining("\n"))));
                with(propName, new fetch<>(fetchModel.getEntityType(), ID_ONLY));
                return;
            }

            final PropertyMetadata pm = entityMetadata.property(propName);

            final var propType = unwrap(pm.type());
            if (propType.javaType() != fetchModel.getEntityType()) {
                throw new EntityRetrievalModelException(ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES.formatted(pm.type(), propName, entityType, fetchModel.getEntityType()));
            }

            // TODO: The following code to extend a fetch model for union-typed properties to include their union-properties appears to be irrelevant.
            //       Test EntityQuery3ExecutionTest.union_members_can_be_yielded_with_dot_expression demonstrates this.
            //       For now let's comment this code out with the intent to eventually remove it.
            //if (propType instanceof PropertyTypeMetadata.Entity et) {
            //    final EntityMetadata em = domainMetadata.forEntity(et.javaType());
            //    em.asUnion().ifPresent(uem -> entityMetadataUtils.unionMembers(uem).stream()
            //            .map(PropertyMetadata::name)
            //            .map(s -> pm.name() + "." + s)
            //            //  is added here as primitive prop only to avoid its removal in EntQuery.adjustAccordingToFetchModel
            //            .forEach(primProps::add));
            //}

            final var existingFetch = entityProps.get(propName);
            final var finalFetch = existingFetch != null ? existingFetch.originalFetch.unionWith(fetchModel) : fetchModel;
            final var model = new EntityRetrievalModel<>(finalFetch, domainMetadata, qsip, false, prepend(stackElement, stack));

            entityProps.put(propName, model);
        }

        private void without(final String propName) {
            final Optional<PropertyMetadata> optPm = getPropMetadata(propName);

            if (optPm.filter(pm -> pm.type().isEntity()).isPresent()) {
                final var removalResult = entityProps.remove(propName);
                if (removalResult == null) {
                    throw new EntityRetrievalModelException(ERR_EXPECTED_TO_FIND_ENTITY_TYPED_PROPERTY_EXCLUDED_FROM_FETCH.formatted(propName, entityType.getSimpleName()));
                }
            } else {
                final var removalResult = primProps.remove(propName);
                if (!removalResult) {
                    throw new EntityRetrievalModelException(ERR_EXPECTED_TO_FIND_PROPERTY_EXCLUDED_FROM_FETCH.formatted(propName, entityType.getSimpleName()));
                }
            }
        }

        private Optional<String> getSinglePropertyOfComponentType(final PropertyMetadata pm) {
            if (pm.hibType() instanceof ICompositeUserTypeInstantiate hibUserType) {
                final String[] propNames = hibUserType.getPropertyNames();
                if (propNames.length == 1) {
                    return Optional.of(propNames[0]);
                }
            }
            return Optional.empty();
        }

        private Optional<PropertyMetadata> getPropMetadata(final String propName) {
            final var optPm = entityMetadata.propertyOpt(propName);
            if (optPm.isEmpty()) {
                // The case of a modelled property with absent metadata.
                if (querySourceInfo.hasProp(propName)) {
                    return Optional.empty();
                }
                throw new EntityRetrievalModelException(ERR_NON_EXISTING_PROPERTY.formatted(entityType.getSimpleName(), propName));
            }
            return optPm;
        }

    }

    private record StackElement(String property, fetch<?> fetch) {
        StackElement(fetch<?> fetch) {
            this("#root", fetch);
        }
    }

}
