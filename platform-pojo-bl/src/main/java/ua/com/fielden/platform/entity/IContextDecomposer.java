package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This interface represents an API for context decomposition in order to access its components such as master entity, selected entities and more.
 * <p>
 * There are two varieties of API methods -- predicates and accessors.
 * These methods come in pairs.
 * <p>
 * For example, predicate <code>masterEntityEmpty</code> returning <code>true</code> if the master entity is present in the context, has a corresponding accessor <code>masterEntity</code>, which returns the master entity.
 * As a convenience, most predicates have negated counterparts, such as <code>masterEntityNotEmpty</code>. 
 * <p>
 * Basically, predicates are intended to be used to ascertain the presence of some context part, conformity to types or kinds, various conditions on the number of selected entities etc.
 * <p>
 * If a positive predicate returns <code>true</code>, a corresponding accessor should be used to retrieve the context part in question.
 * <p>
 * There is no need to blindly follow the predicate/accessor rule. 
 * If there is an absolute certainty that some specific context part is present then simple use a corresponding accessor without any checking.
 * However, accessors return <code>null</code> if the requested context part is not present.
 * <p>
 * The context master entity (returned from {@link #masterEntity()}) may be context-aware.
 * Decomposition of the master entity's context should start with obtaining an instance of {@link ContextOfMasterEntity} using method {@link #ofMasterEntity()}.
 * All methods that contain <code>*OfMasterEntity*</code> in their name, represent API to work with the context of a master entity or its master entity.
 * 
 * @author TG Team
 *
 */
public interface IContextDecomposer {
    
    /**
     * A factory method to instantiate {@link IContextDecomposer} for decomposing <code>optionalContext</code>.
     * 
     * @param optionalContext
     * @return
     */
    public static <M extends AbstractEntity<?>> IContextDecomposer decompose(final Optional<CentreContext<M, ?>> optionalContext) {
        return decompose(optionalContext.orElse(null));
    }
    
    /**
     * A factory method to instantiate {@link IContextDecomposer} for decomposing <code>context</code>.
     * 
     * @param context
     * @return
     */
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
    /**
     * Returns <code>true</code> if context does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean contextEmpty() {
        return getContext() == null;
    }
    
    /**
     * Returns <code>true</code> if context exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean contextNotEmpty() {
        return !contextEmpty();
    }
    
    CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext();
    IContextDecomposer setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context);
    
    // MASTER ENTITY:
    /**
     * Returns <code>true</code> if master entity is not present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean masterEntityEmpty() {
        return masterEntity() == null;
    }
    
    /**
     * Returns <code>true</code> if master entity is present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean masterEntityNotEmpty() {
        return !masterEntityEmpty();
    }
    
    /**
     * Returns master entity or <code>null</code> if it is not present.
     * 
     * @return
     */
    default AbstractEntity<?> masterEntity() {
        return getContext() == null ? null : getContext().getMasterEntity();
    }
    
    /**
     * Returns <code>true</code> if the master entity is an instance of the specified <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
        return masterEntityNotEmpty() && type.isAssignableFrom(masterEntity().getClass());
    }
    
    /**
     * Returns a master entity, type-casted to the specified <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
        return (M) masterEntity();
    }
    
    // MASTER ENTITY'S KEY:
    /**
     * Returns <code>true</code> if masterEntity's key is an instance the specified <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean keyOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (masterEntity.get(KEY) != null) {
                return type.isAssignableFrom(masterEntity.get(KEY).getClass());
            }
        }
        return false;
    }
    
    /**
     * Returns masterEntity's key, type-casted to the specified <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M keyOfMasterEntity(final Class<M> type) {
        return (M) masterEntity().get(KEY);
    }
    
    // CHOSEN PROPERTY:
    /**
     * Returns <code>true</code> if the chosen property is not present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean chosenPropertyEmpty() {
        return chosenProperty() == null;
    }
    
    /**
     * Returns <code>true</code> if the chosen property is present (empty or non-empty string, but not <code>null</code>), <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean chosenPropertyNotEmpty() {
        return !chosenPropertyEmpty();
    }
    
    /**
     * Returns <code>true</code> if the chosen property equals to the specified non-null value, <code>false</code> otherwise.
     * Passing <code>null</code> throws an exception.
     * 
     * @param value
     * @return
     */
    default boolean chosenPropertyEqualsTo(final String value) {
        if (value == null) {
            throw new EntityProducingException("Chosen property should not be compared to null.");
        }
        return value.equals(chosenProperty());
    }
    
    /**
     * Returns <code>true</code> if the chosen property represents <code>this</code>, which is usually applicable for actions that are defined for EGI's column associated with an entity itself (i.e. "this").
     * Otherwise, <code>false</code> is returned.
     * 
     * @return
     */
    default boolean chosenPropertyRepresentsThisColumn() {
        return chosenPropertyEqualsTo("");
    }
    
    /**
     * Returns the chosen property value, which is relevant for master property editor actions and centre column actions:
     * <ul>
     * <li> <code>null</code> if the chosen property is not present
     * <li> empty string ("") if the chosen property represents <code>this</code>
     * <li> a non-empty string that corresponds to some property name
     * </ul>
     * @return
     */
    default String chosenProperty() {
        return getContext() == null ? null : getContext().getChosenProperty();
    }
    
    // CURRENT ENTITY:
    /**
     * Returns <code>true</code> if the current entity is not present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityEmpty() {
        return currentEntity() == null;
    }
    
    /**
     * Returns <code>true</code> if the current entity is present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityNotEmpty() {
        return !currentEntityEmpty();
    }
    
    /**
     * Returns the current entity, which could be <code>null</code>.
     * 
     * @return
     */
    default AbstractEntity<?> currentEntity() {
        return selectedEntitiesOnlyOne() ? getContext().getCurrEntity() : null;
    }
    
    /**
     * Returns <code>true</code> if the current entity is of the specified <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
        return currentEntityNotEmpty() && type.isAssignableFrom(currentEntity().getClass());
    }
    
    /**
     * Returns the current entity, type-casted to the specified <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
        return (M) currentEntity();
    }
    
    // CURRENT ENTITY'S KEY:
    /**
     * Returns <code>true</code> if currentEntity's key is of the specified <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean keyOfCurrentEntityInstanceOf(final Class<M> type) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currentEntity = currentEntity();
            if (currentEntity.get(KEY) != null) {
                return type.isAssignableFrom(currentEntity.get(KEY).getClass());
            }
        }
        return false;
    }
    
    /**
     * Returns currentEntity's key, type-casted to the specified <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M keyOfCurrentEntity(final Class<M> type) {
        return (M) currentEntity().get(KEY);
    }
    
    // SELECTION CRITERIA:
    /**
     * Returns <code>true</code> if the selection criteria entity is not present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectionCritEmpty() {
        return selectionCrit() == null;
    }
    
    /**
     * Returns <code>true</code> if the selection criteria entity is present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectionCritNotEmpty() {
        return !selectionCritEmpty();
    }
    
    /**
     * Returns the selection criteria entity, which could be <code>null</code>.
     * 
     * @return
     */
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit() {
        return getContext() == null ? null : getContext().getSelectionCrit();
    }
    
    // SELECTED ENTITIES:
    /**
     * Returns <code>true</code> if selected entities are not present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesEmpty() {
        return selectedEntities().isEmpty();
    }
    
    /**
     * Returns <code>true</code> if selected entities are present, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesNotEmpty() {
        return !selectedEntitiesEmpty();
    }
    
    /**
     * Returns selected entities. An empty list is return if selected entities are not present or even if they're not applicable.
     * 
     * @return
     */
    default List<AbstractEntity<?>> selectedEntities() {
        return getContext() == null ? Collections.unmodifiableList(new ArrayList<>()) : getContext().getSelectedEntities();
    }
    
    /**
     * Returns a set of IDs that correspond to selected entities.
     * An empty set is returned if selected entities are not present or even if they're not applicable.
     * 
     * @return
     */
    default Set<Long> selectedEntityIds() {
        return selectedEntities().stream().map(AbstractEntity::getId).collect(toCollection(LinkedHashSet::new));
    }
    
    /**
     * Returns <code>true</code> if there is only one selected entity, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesOnlyOne() {
        return selectedEntities().size() == 1;
    }
    
    /**
     * Returns <code>true</code> if there are two or more selected entities, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesMoreThanOne() {
        return selectedEntities().size() > 1;
    }
    
    // COMPUTATION:
    /**
     * Returns an optional computation aspect of the context.
     * 
     * @return
     */
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
        return getContext() == null ? Optional.empty() : getContext().getComputation();
    }
    
    ////////////////////////////////// CONTEXT DECOMPOSITION API [SECOND LEVEL] //////////////////////////////////
    
    default ContextOfMasterEntity ofMasterEntity() {
        return new ContextOfMasterEntity(this);
    }
    
    /**
     * Second level decomposition API where all calls pertain to the context of a master entity.
     *
     */
    static final class ContextOfMasterEntity {
    
        private final IContextDecomposer decomposer;
        
        private ContextOfMasterEntity(IContextDecomposer decomposer) {
            this.decomposer = decomposer;
        }
        
        // CONTEXT AS A WHOLE:
        /**
         * Returns <code>true</code> if the masterEntity's context is present, <code>false</code> otherwise.
         * 
         * @return
         */
        public boolean contextNotEmpty() {
            if (decomposer.masterEntityNotEmpty()) {
                final AbstractEntity<?> masterEntity = decomposer.masterEntity();
                if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                    return ((AbstractFunctionalEntityWithCentreContext<?>) masterEntity).context() != null;
                }
            }
            return false;
        }
        
        /**
         * Returns <code>true</code> if the masterEntity's context is not present, <code>false</code> otherwise.
         * 
         * @return
         */
        public boolean contextEntityEmpty() {
            return !contextNotEmpty();
        }
        
        /**
         * Returns the masterEntity's context. This call may result in value <code>null</code> or NPE if the master entity is not present.
         * 
         * @return
         */
        public CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context() {
            return ((AbstractFunctionalEntityWithCentreContext) decomposer.masterEntity()).context();
        }
        
        // MASTER ENTITY:
        /**
         * Returns <code>true</code> if the masterEntity's master entity is an instance of the specified <code>type</code>, <code>false</code> otherwise.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).masterEntityInstanceOf(type);
        }
        
        /**
         * Returns the masterEntity's master entity, which could be <code>null</code>.
         * 
         * @return
         */
        public AbstractEntity<?> masterEntity() {
            return decompose(context()).masterEntity();
        }
        
        /**
         * Returns the masterEntity's master entity, type casted to the specified <code>type</code>.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
            return decompose(context()).masterEntity(type);
        }
        
        // MASTER ENTITY'S KEY:
        /**
         * Returns <code>true</code> if the key of the masterEntity's master entity is an instance of the specified <code>type</code>, <code>false</code> otherwise.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> boolean keyOfMasterEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).keyOfMasterEntityInstanceOf(type);
        }
        
        /**
         * Returns the masterEntity's master entity key, type-casted to the specified <code>type</code>.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> M keyOfMasterEntity(final Class<M> type) {
            return decompose(context()).keyOfMasterEntity(type);
        }
        
        // MASTER ENTITY'S SELECTION CRIT:
        /**
         * Returns selection criteria of the masterEntity's master entity. Could return <code>null</code> and even throw NPE.
         * 
         * @return
         */
        public EnhancedCentreEntityQueryCriteria<?, ?> selectionCritOfMasterEntity() {
            return decompose(context()).ofMasterEntity().selectionCrit();
        }
        
        // CHOSEN PROPERTY:
        /**
         * Returns the value of chosen property of masterEntity, which is relevant for master property editor actions and centre column actions:
         * <ul>
         * <li> <code>null</code> if the chosen property is not present
         * <li> empty string ("") if the chosen property represents <code>this</code>
         * <li> a non-empty string that corresponds to some property name
         * </ul>
         * 
         * @return
         */
        public String chosenProperty() {
            return decompose(context()).chosenProperty();
        }
        
        // CURRENT ENTITY:
        /**
         * Returns <code>true</code> if the masterEntity's current entity is not present, <code>false</code> otherwise.
         * 
         * @return
         */
        public boolean currentEntityEmpty() {
            return contextEntityEmpty() || decompose(context()).currentEntityEmpty();
        }
        
        /**
         * Returns <code>true</code> if the masterEntity's current entity is present, <code>false</code> otherwise.
         * 
         * @return
         */
        public boolean currentEntityNotEmpty() {
            return !currentEntityEmpty();
        }
        
        /**
         * Returns the masterEntity's current entity. May return <code>null</code> and even throw NPE.
         * 
         * @return
         */
        public AbstractEntity<?> currentEntity() {
            return decompose(context()).currentEntity();
        }
        
        /**
         * Returns <code>true</code> if the masterEntity's current entity is an instance of the specified <code>type</code>, <code>false</code> otherwise.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
            return contextNotEmpty() && decompose(context()).currentEntityInstanceOf(type);
        }
        
        /**
         * Returns the masterEntity's current entity, type-casted to the specified <code>type</code>.
         * 
         * @param type
         * @return
         */
        public <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
            return decompose(context()).currentEntity(type);
        }
        
        // SELECTION CRITERIA:
        /**
         * Returns <code>true</code> if the masterEntity's selection criteria entity is present, <code>false</code> otherwise.
         * 
         * @return
         */
        public boolean selectionCritNotEmpty() {
            return contextNotEmpty() && decompose(context()).selectionCritNotEmpty();
        }
        
        /**
         * Returns the masterEntity's selection criteria entity. May return <code>null</code>
         * 
         * @return
         */
        public EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit() {
            return decompose(context()).selectionCrit();
        }
        
        // SELECTED ENTITIES:
        /**
         * Returns masterEntity's selected entities. An empty list is returned if there are no entities selected.
         * 
         * @return
         */
        public List<AbstractEntity<?>> selectedEntities() {
            return decompose(context()).selectedEntities();
        }
        
        // COMPUTATION:
        /**
         * Returns an optional computation aspect of the masterEntity's context.
         * 
         * @return
         */
        public Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
            return decompose(context()).computation();
        }
    }    
}
