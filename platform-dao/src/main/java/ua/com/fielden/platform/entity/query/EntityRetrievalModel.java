package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.utils.ToString;
import ua.com.fielden.platform.utils.ToString.IFormat;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.fetch.ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.Wrapper.unwrap;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.ToString.separateLines;


/**
 * Represents retrieval models specialised for entity types.
 * <p>
 * Retrieval model {@code RM} is constructed from {@linkplain fetch fetch model} {@code FM} as follows:
 * <ol>
 *   <li> {@link FetchCategory} is used to construct the initial parts of {@code RM}; more details are provided below.
 *   <li> Any properties explicitly excluded by {@code FM} are excluded from {@code RM}.
 *   <li> Any properties explicitly included by {@code FM} are included into {@code RM}.
 *        Any sub-fetch models among them are combined with those used during previous steps.
 *        For example, given entity-typed property {@code P}, if the fetch category resulted in a sub-retrieval model
 *        {@code RM_P} (with an underlying fetch model {@code RMFM_P}) for {@code P},
 *        and {@code P} is explicitly included into {@code FM} with a sub-fetch model {@code FM_P},
 *        then {@code FM_P} is combined with {@code RMFM_P} to produce the final sub-retrieval model for {@code P}.
 *   <li> The set of proxied properties is constructed.
 * </ol>
 *
 * <h4> Processing of entity-typed properties </h4>
 * Property {@code P} with entity type {@code E} may be explored during construction of a retrieval model,
 * which will result in a richer sub-model for that property.
 * <p>
 * If {@code P} is explored, then:
 * <ul>
 *   <li> if {@code E} is a union entity, {@link FetchCategory#ALL} is used;
 *   <li> otherwise {@link FetchCategory#DEFAULT} is used.
 * </ul>
 * Otherwise, if {@code P} is not explored, then:
 * <ul>
 *   <li> if {@code E} is a persistent entity, {@link FetchCategory#ID_ONLY} is used;
 *   <li> otherwise, {@code P} is ignored and no sub-model is constructed for it.
 * </ul>
 *
 * <h4> Fetch categories </h4>
 * See the documentation {@link FetchCategory} for a description of each category.
 * <p>
 * Some categories deserve additional clarification:
 * <ol>
 *   <li> {@link FetchCategory#KEY_AND_DESC}
 *     <ul>
 *       <li> includes {@code key} without exploring it further (however, see the section on processing of {@code key});
 *     </ul>
 *   <li> {@link FetchCategory#DEFAULT}
 *     <ul>
 *       <li> if an entity has a simple entity-typed (but not a union) {@code key}, then it is explored further;
 *       <li> if an entity has a composite key, then all entity-typed (but not a union) key members are explored further;
 *     </ul>
 *   <li> {@link FetchCategory#ALL} - equivalent to {@link FetchCategory#DEFAULT},
 *        but without special handling of entity-typed keys and key members.
 * </ol>
 *
 * <h4> Processing of property {@code key} </h4>
 * <ul>
 *   <li> If an entity has a composite key, the retrieval model is expanded to include all the key members, which are always explored further.
 *        Property {@code key} itself is never included in such cases as it is effective a "virtual" property, having different types at Java and EQL levels.
 *        More specifically, it is {@link DynamicEntityKey} in Java, and a string in EQL, because it is implicitly calculated
 *        (i.e. key members are converted to string and concatenated with a key value separator to produce the key value).
 *        If such {@code key} were to be retrieved, then it would be required to parse its string value into a {@link DynamicEntityKey}.
 *        Instead, a composite {@code key} is never included in a retrieval model, and the constructor of {@link AbstractEntity} takes care of initialising its value.
 *   <li> If an entity is a union, {@code key} is included together with all the union members, which are always explored further.
 * </ul>
 *
 * <h4> Implementation Details </h4>
 * When inspecting property types for a potential entity type,
 * method {@link PropertyTypeMetadata.Wrapper#unwrap(PropertyTypeMetadata)} is used because of the way fetch models are constructed -- heuristically.
 * One case where unwrapping is needed is collectional properties: fetch models know only about the collectional element type.
 */
public final class EntityRetrievalModel<T extends AbstractEntity<?>> implements IRetrievalModel<T>, ToString.IFormattable {

    public static final String ERR_UNKNOWN_FETCH_CATEGORY = "Unknown fetch category [%s].";
    public static final String ERR_UNEXPECTED_PROPERTY_IN_RETRIEVAL_MODEL = "No property [%s] in retrieval model:%n%s";
    public static final String ERR_EXPECTED_TO_FIND_ENTITY_TYPED_PROPERTY_EXCLUDED_FROM_FETCH = "Couldn't find entity-typed property [%s] to be excluded from fetched properties of entity type [%s].";
    public static final String ERR_EXPECTED_TO_FIND_PROPERTY_EXCLUDED_FROM_FETCH = "Couldn't find property [%s] to be excluded from fetched properties of entity type [%s].";
    public static final String ERR_NON_EXISTING_PROPERTY = "Trying to fetch entity [%s] with non-existing property [%s].";

    private final fetch<T> originalFetch;
    /** Indicates whether this fetch is the top-most (graph root) or a nested one (subgraph). */
    private final boolean topLevel;
    /** Association between an entity-typed property and its nested fetch model. */
    private final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps;
    /** Primitive properties that should be retrieved. */
    private final Set<String> primProps;
    /** Properties that should be proxied in the resulting entity proxy instance. */
    private final Set<String> proxiedProps;

    public EntityRetrievalModel(final fetch<T> originalFetch, final IDomainMetadata domainMetadata, final QuerySourceInfoProvider qsip) {
        this(originalFetch, domainMetadata, qsip, true);
    }

    EntityRetrievalModel(final fetch<T> originalFetch,
                         final IDomainMetadata domainMetadata,
                         final QuerySourceInfoProvider qsip,
                         final boolean topLevel) {
        this.originalFetch = originalFetch;
        this.topLevel = topLevel;

        final var builder = buildModel(originalFetch, domainMetadata, qsip);
        this.primProps = unmodifiableSet(builder.primProps);
        this.entityProps = unmodifiableMap(builder.entityProps);
        this.proxiedProps = unmodifiableSet(builder.proxiedProps);
    }

    private static <T extends AbstractEntity<?>> Builder buildModel(
            final fetch<T> originalFetch,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip)
    {
        final var builder = new Builder(originalFetch.getEntityType(), domainMetadata, qsip);

        switch (originalFetch.getFetchCategory()) {
            case ALL_INCL_CALC -> builder.includeAllFirstLevelPropsInclCalc();
            case ALL -> builder.includeAllFirstLevelProps();
            case DEFAULT -> builder.includeAllFirstLevelPrimPropsAndKey();
            case KEY_AND_DESC -> builder.includeKeyAndDescOnly();
            case ID_AND_VERSION -> builder.includeIdAndVersionOnly();
            case ID_ONLY -> builder.includeIdOnly();
            case NONE -> {}
            default -> throw new IllegalStateException(ERR_UNKNOWN_FETCH_CATEGORY.formatted(originalFetch.getFetchCategory()));
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
            throw new EqlException(ERR_UNEXPECTED_PROPERTY_IN_RETRIEVAL_MODEL.formatted(path, this));
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

    /**
     * Mutable builder for initialisation of {@link EntityRetrievalModel}.
     * Its output is stored in {@link #primProps}, {@link #entityProps}, and {@link #proxiedProps}.
     * </p>
     * Both {@link IDomainMetadata} and {@link QuerySourceInfoProvider} are required to correctly build a retrieval model.
     * <ul>
     * <li> {@link IDomainMetadata} is required because of the richness of information it provides about entity types and their properties.
     * <li> {@link QuerySourceInfoProvider} is required because it contains EQL-specific information that is not provided
     *      by {@link IDomainMetadata} (e.g., properties that are present in the yields of synthetic entity models).
     * </ul>
     */
    private static final class Builder {
        final Class<? extends AbstractEntity<?>> entityType;
        final IDomainMetadata domainMetadata;
        final EntityMetadataUtils entityMetadataUtils;
        final PropertyMetadataUtils propMetadataUtils;
        final EntityMetadata entityMetadata;
        final QuerySourceInfoProvider qsip;
        final QuerySourceInfo<?> querySourceInfo;

        // Mutable components that are being built

        final Set<String> primProps;
        final Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> entityProps;
        final Set<String> proxiedProps;

        Builder(final Class<? extends AbstractEntity<?>> entityType,
                final IDomainMetadata domainMetadata,
                final QuerySourceInfoProvider qsip)
        {
            this.domainMetadata = domainMetadata;
            this.qsip = qsip;
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

        /**
         * Performs an action for each <i>modelled</i> property (see {@link QuerySourceInfoProvider}) of the entity type.
         * </p>
         * The action is supplied with an optional {@link PropertyMetadata} that may be absent.
         * Refer to {@link QuerySourceInfoProvider} for a description of when such cases may occur.
         */
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
            entityMetadata.properties().stream()
                    .filter(pm -> propMetadataUtils.isPropEntityType(pm.type(), EntityMetadata::isPersistent))
                    .forEach(pm -> with(pm.name()));
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
                    // FIXME: Union-entity typed keys or key members are not supported at this stage.
                    //        However, there is nothing preventing such definitions, which leads to StackOverflowErrors during fetch model construction.
                    //        To support such definitions, it would be necessary to take into account recursive definitions,
                    //        where a key or key member that is of a union type may have a union-property of the same type as the enclosing entity.
                    //        We would need to ensure that such union-properties are not explored to prevent StackOverflowErrors during fetch model construction.
                    //        For now, let's simply skip the whole union-typed key and key-members from exploration.
                    final boolean exploreEntities = optPropMetadata.isEmpty() ||
                                                    optPropMetadata.filter(it -> it.type().isEntity()
                                                                                 && !propMetadataUtils.isPropEntityType(it, EntityMetadata::isUnion)
                                                                                 && (KEY.equals(it.name()) || it.has(KEY_MEMBER)))
                                                            .isPresent();
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

            with(KEY, true);

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

        /**
         * Includes the property and explores it further, if it is entity-typed.
         */
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
                            } else {
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

        private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
            final PropertyMetadata pm = entityMetadata.property(propName);

            final var propType = unwrap(pm.type());
            if (propType.javaType() != fetchModel.getEntityType()) {
                throw new EqlException(ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES.formatted(pm.type(), propName, entityType, fetchModel.getEntityType()));
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
            entityProps.put(propName, new EntityRetrievalModel<>(finalFetch, domainMetadata, qsip, false));
        }

        private void without(final String propName) {
            final Optional<PropertyMetadata> optPm = getPropMetadata(propName);

            if (optPm.filter(pm -> pm.type().isEntity()).isPresent()) {
                final var removalResult = entityProps.remove(propName);
                if (removalResult == null) {
                    throw new EqlException(ERR_EXPECTED_TO_FIND_ENTITY_TYPED_PROPERTY_EXCLUDED_FROM_FETCH.formatted(propName, entityType.getSimpleName()));
                }
            } else {
                final var removalResult = primProps.remove(propName);
                if (!removalResult) {
                    throw new EqlException(ERR_EXPECTED_TO_FIND_PROPERTY_EXCLUDED_FROM_FETCH.formatted(propName, entityType.getSimpleName()));
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
                throw new EqlException(ERR_NON_EXISTING_PROPERTY.formatted(entityType.getSimpleName(), propName));
            }
            return optPm;
        }

    }

}
