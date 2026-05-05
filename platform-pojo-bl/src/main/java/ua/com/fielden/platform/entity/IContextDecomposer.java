package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.annotation.EntityTypeCarrier;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.Finder.streamProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.traversePropPath;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

/// This interface represents an API for context decomposition in order to access its components such as master entity, selected entities and more.
///
/// There are two varieties of API methods — predicates and accessors.
/// These methods come in pairs.
///
/// For example, predicate `masterEntityEmpty` returning `true` if the master entity is present in the context, has a corresponding accessor `masterEntity`, which returns the master entity.
/// As a convenience, most predicates have negated counterparts, such as `masterEntityNotEmpty`.
///
/// Basically, predicates are intended to be used to ascertain the presence of some context part, conformity to types or kinds, various conditions on the number of selected entities etc.
///
/// If a positive predicate returns `true`, a corresponding accessor should be used to retrieve the context part in question.
///
/// There is no need to blindly follow the predicate/accessor rule.
/// If there is an absolute certainty that some specific context part is present then simple use a corresponding accessor without any checking.
/// However, accessors return `null` if the requested context part is not present.
///
/// The context master entity (returned from [#masterEntity()]) may be context-aware.
/// Decomposition of the master entity's context should start with obtaining an instance of [ContextOfMasterEntity] using method [#ofMasterEntity()].
/// All methods that contain `*OfMasterEntity*` in their name, represent API to work with the context of a master entity or its master entity.
///
public interface IContextDecomposer {
    String AUTOCOMPLETE_ACTIVE_ONLY_KEY = "@@activeOnly";
    String ERR_INVALID_TYPE_NAME_FOR_ENTITY_TYPE_CARRIER = "Invalid full type name [%s] for @%s-annotated property.";

    /// A factory method to instantiate [IContextDecomposer] for decomposing `optionalContext`.
    ///
    public static <M extends AbstractEntity<?>> IContextDecomposer decompose(final Optional<CentreContext<M, ?>> optionalContext) {
        return decompose(optionalContext.orElse(null));
    }

    /// A factory method to instantiate [IContextDecomposer] for decomposing `context`.
    ///
    public static <M extends AbstractEntity<?>> IContextDecomposer decompose(final CentreContext<M, ?> context) {
        final IContextDecomposer contextDecomposer = new IContextDecomposer() {
            private CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context;

            @Override
            public CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext() {
                return context;
            }

            @Override
            public IContextDecomposer setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context) {
                this.context = context;
                return this;
            }
        };
        contextDecomposer.setContext((CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>>) context);
        return contextDecomposer;
    }

    ////////////////////////////////// CONTEXT DECOMPOSITION API [FIRST LEVEL] //////////////////////////////////

    // CONTEXT AS A WHOLE:
    /// Returns `true` if context does not exist, `false` otherwise.
    ///
    default boolean contextEmpty() {
        return getContext() == null;
    }

    /// Returns `true` if context exists, `false` otherwise.
    ///
    default boolean contextNotEmpty() {
        return !contextEmpty();
    }

    CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext();
    IContextDecomposer setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context);

    // MASTER ENTITY:
    /// Returns `true` if master entity is not present, `false` otherwise.
    ///
    default boolean masterEntityEmpty() {
        return masterEntity() == null;
    }

    /// Returns `true` if master entity is present, `false` otherwise.
    ///
    default boolean masterEntityNotEmpty() {
        return !masterEntityEmpty();
    }

    /// Returns master entity or `null` if it is not present.
    ///
    default AbstractEntity<?> masterEntity() {
        return getContext() == null ? null : getContext().getMasterEntity();
    }

    /// Returns `true` if the master entity is an instance of the specified `type`, `false` otherwise.
    ///
    default <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
        return masterEntityNotEmpty() && type.isAssignableFrom(masterEntity().getClass());
    }

    /// Returns a master entity, type-casted to the specified `type`.
    ///
    default <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
        return (M) masterEntity();
    }

    // MASTER ENTITY'S KEY:
    /// Returns `true` if masterEntity's key is an instance the specified `type`, `false` otherwise.
    ///
    default <M extends AbstractEntity<?>> boolean keyOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (masterEntity.get(KEY) != null) {
                return type.isAssignableFrom(masterEntity.get(KEY).getClass());
            }
        }
        return false;
    }

    /// Returns masterEntity's key, type-casted to the specified `type`.
    ///
    default <M extends AbstractEntity<?>> M keyOfMasterEntity(final Class<M> type) {
        return (M) masterEntity().get(KEY);
    }

    // CHOSEN PROPERTY:
    /// Returns `true` if the chosen property is not present, `false` otherwise.
    ///
    default boolean chosenPropertyEmpty() {
        return chosenProperty() == null;
    }

    /// Returns `true` if the chosen property is present (empty or non-empty string, but not `null`), `false` otherwise.
    ///
    default boolean chosenPropertyNotEmpty() {
        return !chosenPropertyEmpty();
    }

    /// Returns `true` if the chosen property equals to the specified non-null value, `false` otherwise.
    /// Passing `null` throws an exception.
    ///
    default boolean chosenPropertyEqualsTo(final CharSequence value) {
        if (value == null) {
            throw new EntityProducingException("Chosen property should not be compared to null.");
        }
        return value.toString().equals(chosenProperty());
    }

    /// Returns `true` if the chosen property represents `this`, which is usually applicable for actions that are defined for EGI's column associated with an entity itself (i.e. "this").
    /// Otherwise, `false` is returned.
    ///
    default boolean chosenPropertyRepresentsThisColumn() {
        return chosenPropertyEqualsTo("");
    }

    /// Returns the chosen property value, which is relevant for master property editor actions and centre column actions:
    ///
    /// - `null` if the chosen property is not present
    /// - empty string ("") if the chosen property represents `this`
    /// - a non-empty string that corresponds to some property name
    ///
    default String chosenProperty() {
        return getContext() == null ? null : getContext().getChosenProperty();
    }

    // CURRENT ENTITY:
    /// Returns `true` if the current entity is not present, `false` otherwise.
    ///
    default boolean currentEntityEmpty() {
        return currentEntity() == null;
    }

    /// Returns `true` if the current entity is present, `false` otherwise.
    ///
    default boolean currentEntityNotEmpty() {
        return !currentEntityEmpty();
    }

    /// Determines actual (i.e. not generated / synthetic) type from entity-typed property path (`entityTypedPropPath`) in root entity type (`rootType`).
    ///
    /// @param rootType root entity type
    /// @param entityTypedPropPath dot-notated entity-typed property path defined in `rootType`; "" is supported meaning root type itself; the path can be taken from [EntityUtils#traversePropPath(AbstractEntity,String)]
    ///
    default Class<AbstractEntity<?>> determineActualEntityType(final Class<? extends AbstractEntity<?>> rootType, final String entityTypedPropPath) {
        return determineBaseEntityType(getOriginalType(determinePropertyType(rootType, dslName(entityTypedPropPath))));
    }

    /// Returns the base type of `entityType` if it is a synthetic entity based on a persistent entity.
    /// Otherwise, returns `entityType`.
    ///
    @SuppressWarnings("unchecked")
    default Class<AbstractEntity<?>> determineBaseEntityType(final Class<AbstractEntity<?>> entityType) {
        if (isSyntheticBasedOnPersistentEntityType(entityType)) {
            // for the cases where EntityEditAction is used for opening SyntheticBasedOnPersistentEntity we explicitly use base type;
            // however this is not the case for StandardActions.EDIT_ACTION because of computation existence that returns entityType.
            return (Class<AbstractEntity<?>>) entityType.getSuperclass();
        }
        return entityType;
    }

    /// Returns optional type for chosen entity defined by [currentEntity; chosenProperty].
    ///
    default Optional<Class<AbstractEntity<?>>> chosenEntityType() {
        return traversePropPath(currentEntity(), chosenProperty()) // traverse entity-typed paths and values
            .findFirst() // find first (most full) pair, if any
            .map(pathAndValueOpt -> determineEntityTypeFrom(currentEntity(), pathAndValueOpt)); // take the path only and determine actual entity type from that path
    }

    /// Returns optional ID for type-compatible chosen entity defined by [currentEntity; chosenProperty].
    ///
    /// @param compatibilityType the entity type with which chosen entity should be compatible
    ///
    default Optional<Long> chosenEntityId(final Class<? extends AbstractEntity<?>> compatibilityType) {
        if (currentEntityEmpty()) {
            return empty();
        } else if (chosenPropertyEmpty() && !currentEntityInstanceOf(compatibilityType)) { // for non-compatible currentEntity without chosenProperty (edge-case)
            return ofNullable(currentEntity().getId()); // we still try to use ID of that current entity (e.g. WaCostDetails to be opened for WorkActivity in primary action -- one-2-one association, the same ID shared by both entities)
        }
        // there are couple of possible legitimate cases here:
        // 1. either currentEntity().get(chosenProperty()) is of type for Entity Master and all is good, or
        // 2. chosenProperty is a sub property of a property of type for Entity Master, where that "parent" property belongs to the current entity, or
        // 3. currentEntity() itself is of type for Entity Master (chosenProperty() is "" aka "this" or chosenProperty() is not defined in context configuration)
        return traversePropPath(currentEntity(), chosenProperty()) // traverse entity-typed paths and values
            .filter(pathAndValueOpt -> compatibilityType.isAssignableFrom(determineEntityTypeFrom(currentEntity(), pathAndValueOpt))) // find only type-compatible paths
            .findFirst() // find first (most full) type-compatible pair, if any
            .flatMap(pathAndValueOpt -> pathAndValueOpt._2) // get optional entity value, if any
            .map(AbstractEntity::getId); // get ID from it, if any
    }

    /// Determines entity type from fullest `pathAndValueOpt` by looking into entity type carrier first.
    /// If there is no such property (or its value is empty / invalid), falls back to standard [#determineActualEntityType(Class, String)].
    default Class<AbstractEntity<?>> determineEntityTypeFrom(final AbstractEntity<?> currentEntity, final T2<String, Optional<? extends AbstractEntity<?>>> pathAndValueOpt) {
        return pathAndValueOpt._2
            .flatMap(this::determineCarriedEntityTypeFrom)
            .orElseGet(() -> determineActualEntityType(currentEntity.getType(), pathAndValueOpt._1));
    }

    /// Finds `@EntityTypeCarrier` property in `entity` and corresponding type.
    /// Returns empty [Optional] in case if there is no such property or its value is empty / invalid.
    default Optional<Class<AbstractEntity<?>>> determineCarriedEntityTypeFrom(final AbstractEntity<?> entity) {
        return streamProperties(entity.getClass(), EntityTypeCarrier.class)
            .findFirst()
            .map(field -> (String) entity.get(field.getName()))
            .map(carrierValue -> {
                try {
                    return (Class<AbstractEntity<?>>) Class.forName(carrierValue);
                } catch (ClassNotFoundException e) {
                    throw new EntityProducingException(ERR_INVALID_TYPE_NAME_FOR_ENTITY_TYPE_CARRIER.formatted(carrierValue, EntityTypeCarrier.class.getSimpleName()), e);
                }
            });
    }

    /// Returns the current entity, which could be `null`.
    ///
    default AbstractEntity<?> currentEntity() {
        return selectedEntitiesOnlyOne() ? getContext().getCurrEntity() : null;
    }

    /// Returns `true` if the current entity is of the specified `type`, `false` otherwise.
    ///
    default <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
        return currentEntityNotEmpty() && type.isAssignableFrom(currentEntity().getClass());
    }

    /// Returns the current entity, type-casted to the specified `type`.
    ///
    default <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
        return (M) currentEntity();
    }

    // CHOSEN ENTITY:
    /// Returns `true` if the chosen entity is not present, `false` otherwise.
    /// `chosenEntity` is populated only when the action's context configuration opts in via `withChosenEntity()`.
    ///
    default boolean chosenEntityEmpty() {
        return chosenEntity() == null;
    }

    /// Returns `true` if the chosen entity is present, `false` otherwise.
    ///
    default boolean chosenEntityNotEmpty() {
        return !chosenEntityEmpty();
    }

    /// Returns the chosen entity carried directly on [CentreContext], or `null` if not present.
    /// Resolved on the client according to the column shape — entity-typed leaf, union active member, holder of a simple-typed leaf, or collectional item for a dynamic column.
    ///
    default AbstractEntity<?> chosenEntity() {
        return getContext() == null ? null : getContext().getChosenEntity();
    }

    /// Returns `true` if the chosen entity is an instance of the specified `type`, `false` otherwise.
    ///
    default <M extends AbstractEntity<?>> boolean chosenEntityInstanceOf(final Class<M> type) {
        return chosenEntityNotEmpty() && type.isAssignableFrom(chosenEntity().getClass());
    }

    /// Returns the chosen entity, type-casted to the specified `type`.
    ///
    @SuppressWarnings("unchecked")
    default <M extends AbstractEntity<?>> M chosenEntity(final Class<M> type) {
        return (M) chosenEntity();
    }

    // CURRENT ENTITY'S KEY:
    /// Returns `true` if currentEntity's key is of the specified `type`, `false` otherwise.
    ///
    default <M extends AbstractEntity<?>> boolean keyOfCurrentEntityInstanceOf(final Class<M> type) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currentEntity = currentEntity();
            if (currentEntity.get(KEY) != null) {
                return type.isAssignableFrom(currentEntity.get(KEY).getClass());
            }
        }
        return false;
    }

    /// Returns currentEntity's key, type-casted to the specified `type`.
    ///
    default <M extends AbstractEntity<?>> M keyOfCurrentEntity(final Class<M> type) {
        return (M) currentEntity().get(KEY);
    }

    // SELECTION CRITERIA:
    /// Returns `true` if the selection criteria entity is not present, `false` otherwise.
    ///
    default boolean selectionCritEmpty() {
        return selectionCrit() == null;
    }

    /// Returns `true` if the selection criteria entity is present, `false` otherwise.
    ///
    default boolean selectionCritNotEmpty() {
        return !selectionCritEmpty();
    }

    /// Returns the selection criteria entity, which could be `null`.
    ///
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit() {
        return getContext() == null ? null : getContext().getSelectionCrit();
    }

    // SELECTED ENTITIES:
    /// Returns `true` if selected entities are not present, `false` otherwise.
    ///
    default boolean selectedEntitiesEmpty() {
        return selectedEntities().isEmpty();
    }

    /// Returns `true` if selected entities are present, `false` otherwise.
    ///
    default boolean selectedEntitiesNotEmpty() {
        return !selectedEntitiesEmpty();
    }

    /// Returns selected entities.
    /// An empty list is return if selected entities are not present or even if they're not applicable.
    ///
    default List<AbstractEntity<?>> selectedEntities() {
        return getContext() == null ? Collections.unmodifiableList(new ArrayList<>()) : getContext().getSelectedEntities();
    }

    /// Returns a set of IDs that correspond to selected entities.
    /// An empty set is returned if selected entities are not present or even if they're not applicable.
    ///
    default Set<Long> selectedEntityIds() {
        return selectedEntities().stream().map(AbstractEntity::getId).collect(toCollection(LinkedHashSet::new));
    }

    /// Returns `true` if there is only one selected entity, `false` otherwise.
    ///
    default boolean selectedEntitiesOnlyOne() {
        return selectedEntities().size() == 1;
    }

    /// Returns `true` if there are two or more selected entities, `false` otherwise.
    ///
    default boolean selectedEntitiesMoreThanOne() {
        return selectedEntities().size() > 1;
    }

    // COMPUTATION:
    /// Returns an optional computation aspect of the context.
    ///
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
        return getContext() == null ? Optional.empty() : getContext().getComputation();
    }

    // CUSTOM OBJECT:

    /// Checks whether the `context` represents [IValueMatcherWithCentreContext] context with indication that only active entities should be used for autocompletion.
    ///
    default boolean autocompleteActiveOnly() {
        return getContext() != null
                && getContext().getCustomObject() != null
                && getContext().getCustomObject().containsKey(AUTOCOMPLETE_ACTIVE_ONLY_KEY)
                && (boolean) getContext().getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_KEY);
    }

    ////////////////////////////////// CONTEXT DECOMPOSITION API [SECOND LEVEL] //////////////////////////////////

    default ContextOfMasterEntity ofMasterEntity() {
        return new ContextOfMasterEntity(this);
    }

    /// Second level decomposition API where all calls pertain to the context of a master entity.
    ///
    static final class ContextOfMasterEntity {

        private final IContextDecomposer decomposer;

        private ContextOfMasterEntity(final IContextDecomposer decomposer) {
            this.decomposer = decomposer;
        }

        // CONTEXT AS A WHOLE:
        /// Returns `true` if the masterEntity's context is present, `false` otherwise.
        ///
        public boolean contextNotEmpty() {
            if (decomposer.masterEntityNotEmpty()) {
                final AbstractEntity<?> masterEntity = decomposer.masterEntity();
                if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                    return ((AbstractFunctionalEntityWithCentreContext<?>) masterEntity).context() != null;
                }
            }
            return false;
        }

        /// Returns `true` if the masterEntity's context is not present, `false` otherwise.
        ///
        public boolean contextEntityEmpty() {
            return !contextNotEmpty();
        }

        /// Returns the masterEntity's context.
        /// This call may result in value `null` or NPE if the master entity is not present.
        ///
        public CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context() {
            return ((AbstractFunctionalEntityWithCentreContext) decomposer.masterEntity()).context();
        }

        // MASTER ENTITY:
        /// Returns `true` if the masterEntity's master entity is an instance of the specified `type`, `false` otherwise.
        ///
        public <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).masterEntityInstanceOf(type);
        }

        /// Returns the masterEntity's master entity, which could be `null`.
        ///
        public AbstractEntity<?> masterEntity() {
            return decompose(context()).masterEntity();
        }

        /// Returns the masterEntity's master entity, type casted to the specified `type`.
        ///
        public <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
            return decompose(context()).masterEntity(type);
        }

        // MASTER ENTITY'S KEY:
        /// Returns `true` if the key of the masterEntity's master entity is an instance of the specified `type`, `false` otherwise.
        ///
        public <M extends AbstractEntity<?>> boolean keyOfMasterEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).keyOfMasterEntityInstanceOf(type);
        }

        /// Returns the masterEntity's master entity key, type-casted to the specified `type`.
        ///
        public <M extends AbstractEntity<?>> M keyOfMasterEntity(final Class<M> type) {
            return decompose(context()).keyOfMasterEntity(type);
        }

        // MASTER ENTITY'S SELECTION CRIT:
        /// Returns selection criteria of the masterEntity's master entity.
        /// Could return `null` and even throw NPE.
        ///
        public EnhancedCentreEntityQueryCriteria<?, ?> selectionCritOfMasterEntity() {
            return decompose(context()).ofMasterEntity().selectionCrit();
        }

        // CHOSEN PROPERTY:
        /// Returns the value of chosen property of masterEntity, which is relevant for master property editor actions and centre column actions:
        ///
        /// - `null` if the chosen property is not present
        /// - empty string ("") if the chosen property represents `this`
        /// - a non-empty string that corresponds to some property name
        ///
        public String chosenProperty() {
            return decompose(context()).chosenProperty();
        }

        public boolean chosenPropertyEmpty() {
            return chosenProperty() == null;
        }

        public boolean chosenPropertyNotEmpty() {
            return !chosenPropertyEmpty();
        }

        // CURRENT ENTITY:
        /// Returns `true` if the masterEntity's current entity is not present, `false` otherwise.
        ///
        public boolean currentEntityEmpty() {
            return contextEntityEmpty() || decompose(context()).currentEntityEmpty();
        }

        /// Returns `true` if the masterEntity's current entity is present, `false` otherwise.
        ///
        public boolean currentEntityNotEmpty() {
            return !currentEntityEmpty();
        }

        /// Returns the masterEntity's current entity.
        /// May return `null` and even throw NPE.
        ///
        public AbstractEntity<?> currentEntity() {
            return decompose(context()).currentEntity();
        }

        /// Returns `true` if the masterEntity's current entity is an instance of the specified `type`, `false` otherwise.
        ///
        public <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).currentEntityInstanceOf(type);
        }

        /// Returns the masterEntity's current entity, type-casted to the specified `type`.
        ///
        public <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
            return decompose(context()).currentEntity(type);
        }

        // CHOSEN ENTITY:
        /// Returns `true` if the masterEntity's chosen entity is not present, `false` otherwise.
        ///
        public boolean chosenEntityEmpty() {
            return chosenEntity() == null;
        }

        /// Returns `true` if the masterEntity's chosen entity is present, `false` otherwise.
        ///
        public boolean chosenEntityNotEmpty() {
            return !chosenEntityEmpty();
        }

        /// Returns the masterEntity's chosen entity, or `null` if not present.
        ///
        public AbstractEntity<?> chosenEntity() {
            return contextNotEmpty() ? decompose(context()).chosenEntity() : null;
        }

        /// Returns `true` if the masterEntity's chosen entity is an instance of the specified `type`, `false` otherwise.
        ///
        public <M extends AbstractEntity<?>> boolean chosenEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).chosenEntityInstanceOf(type);
        }

        /// Returns the masterEntity's chosen entity, type-casted to the specified `type`.
        ///
        public <M extends AbstractEntity<?>> M chosenEntity(final Class<M> type) {
            return contextNotEmpty() ? decompose(context()).chosenEntity(type) : null;
        }

        // SELECTION CRITERIA:
        /// Returns `true` if the masterEntity's selection criteria entity is present, `false` otherwise.
        ///
        public boolean selectionCritNotEmpty() {
            return contextNotEmpty() && decompose(context()).selectionCritNotEmpty();
        }

        /// Returns the masterEntity's selection criteria entity.
        /// May return `null`.
        ///
        public EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit() {
            return decompose(context()).selectionCrit();
        }

        // SELECTED ENTITIES:
        /// Returns masterEntity's selected entities.
        /// An empty list is returned if there are no entities selected.
        ///
        public List<AbstractEntity<?>> selectedEntities() {
            return decompose(context()).selectedEntities();
        }

        // COMPUTATION:
        /// Returns an optional computation aspect of the masterEntity's context.
        ///
        public Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
            return decompose(context()).computation();
        }
    }

}
