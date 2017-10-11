package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This interface represents a set of utilities for context decomposition.
 * <p>
 * There are two types of methods: accessors and predicates.
 * <p>
 * Predicates are used to query existence, conformity to types or kinds, conditions on count etc.
 * <p>
 * After concrete predicate(s) has been succeeded the API accessor should be used to actually retrieve the part that conforms to the predicate(s).
 * <p>
 * If there is a certainty that concrete single-cased context part exists -- please use corresponding accessor only.
 * If there are multiple choices -- please use predicates and their corresponding follow-up accessors.
 * <p>
 * Besides first level API there also exist the API for second level context, aka the context of {@link #masterEntity()}.
 * All of second-level methods contain 'OfMasterEntity' suffix (in the end of method name, except for those that end with predicate suffixes like 'InstanceOf' or 'NotEmpty').
 * 
 * @author TG Team
 *
 */
public interface IContextDecomposer {
    
    /**
     * Creates {@link IContextDecomposer} instance for decomposing <code>optionalContext</code>.
     * 
     * @param optionalContext
     * @return
     */
    public static <M extends AbstractEntity<?>> IContextDecomposer decompose(final Optional<CentreContext<M, ?>> optionalContext) {
        return decompose(optionalContext.orElse(null));
    }
    
    /**
     * Creates {@link IContextDecomposer} instance for decomposing <code>context</code>.
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
            public void setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context) {
                this.context = context;
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
    void setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context);
    
    // MASTER ENTITY:
    /**
     * Returns <code>true</code> if master entity does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean masterEntityEmpty() {
        return masterEntity() == null;
    }
    
    /**
     * Returns <code>true</code> if master entity exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean masterEntityNotEmpty() {
        return !masterEntityEmpty();
    }
    
    /**
     * Returns master entity.
     * 
     * @return
     */
    default AbstractEntity<?> masterEntity() {
        return getContext() == null ? null : getContext().getMasterEntity();
    }
    
    /**
     * Returns <code>true</code> if master entity exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
        return masterEntityNotEmpty() && type.isAssignableFrom(masterEntity().getClass());
    }
    
    /**
     * Returns master entity of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
        return (M) masterEntity();
    }
    
    // MASTER ENTITY'S KEY:
    /**
     * Returns <code>true</code> if masterEntity's key exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
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
     * Returns masterEntity's key of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M keyOfMasterEntity(final Class<M> type) {
        return (M) masterEntity().get(KEY);
    }
    
    // CHOSEN PROPERTY:
    /**
     * Returns <code>true</code> if chosen property is not applicable, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean chosenPropertyEmpty() {
        return chosenProperty() == null;
    }
    
    /**
     * Returns <code>true</code> if chosen property is applicable ("" or non-empty string), <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean chosenPropertyNotEmpty() {
        return !chosenPropertyEmpty();
    }
    
    /**
     * Returns <code>true</code> if chosen property equals to concrete non-null value, <code>false</code> otherwise.
     * 
     * @param value
     * @return
     */
    default boolean chosenPropertyEqualsTo(final String value) {
        if (value == null) {
            throw new EntityProducingException("Null value is not permitted.");
        }
        return value.equals(chosenProperty());
    }
    
    /**
     * Returns <code>true</code> if action for 'this' column has been clicked, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean chosenPropertyRepresentsThisColumn() {
        return chosenPropertyEqualsTo("");
    }
    
    /**
     * Returns chosen property value: <code>null</code> if chosen property is not applicable,
     * "" if property action for 'this' column was actioned, and non-empty property name otherwise
     * (master property editor actions and centre column actions).
     * 
     * @return
     */
    default String chosenProperty() {
        return getContext() == null ? null : getContext().getChosenProperty();
    }
    
    // CURRENT ENTITY:
    /**
     * Returns <code>true</code> if current entity does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityEmpty() {
        return currentEntity() == null;
    }
    
    /**
     * Returns <code>true</code> if current entity exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityNotEmpty() {
        return !currentEntityEmpty();
    }
    
    /**
     * Returns current entity.
     * 
     * @return
     */
    default AbstractEntity<?> currentEntity() {
        return selectedEntitiesOnlyOne() ? getContext().getCurrEntity() : null;
    }
    
    /**
     * Returns <code>true</code> if current entity exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
        return currentEntityNotEmpty() && type.isAssignableFrom(currentEntity().getClass());
    }
    
    /**
     * Returns current entity of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
        return (M) currentEntity();
    }
    
    // CURRENT ENTITY'S KEY:
    /**
     * Returns <code>true</code> if currentEntity's key exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
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
     * Returns currentEntity's key of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M keyOfCurrentEntity(final Class<M> type) {
        return (M) currentEntity().get(KEY);
    }
    
    // SELECTION CRITERIA:
    /**
     * Returns <code>true</code> if selection criteria entity does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectionCritEmpty() {
        return selectionCrit() == null;
    }
    
    /**
     * Returns <code>true</code> if selection criteria entity exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectionCritNotEmpty() {
        return !selectionCritEmpty();
    }
    
    /**
     * Returns selection criteria entity.
     * 
     * @return
     */
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit() {
        return getContext() == null ? null : getContext().getSelectionCrit();
    }
    
    // SELECTED ENTITIES:
    /**
     * Returns <code>true</code> if selected entities do not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesEmpty() {
        return selectedEntities().isEmpty();
    }
    
    /**
     * Returns <code>true</code> if selected entities exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesNotEmpty() {
        return !selectedEntitiesEmpty();
    }
    
    /**
     * Returns selected entities.
     * 
     * @return
     */
    default List<AbstractEntity<?>> selectedEntities() {
        return getContext() == null ? Collections.unmodifiableList(new ArrayList<>()) : getContext().getSelectedEntities();
    }
    
    /**
     * Returns selected entity ids as a set.
     * Iterator over this set does not imply any particular order.
     * 
     * @return
     */
    default Set<Long> selectedEntityIds() {
        return selectedEntities().stream().map(ent -> ent.getId()).collect(toSet());
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
     * Returns <code>true</code> if there is two or more selected entities, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectedEntitiesMoreThanOne() {
        return selectedEntities().size() > 1;
    }
    
    // COMPUTATION:
    /**
     * Returns optional computation.
     * 
     * @return
     */
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
        return getContext() == null ? Optional.empty() : getContext().getComputation();
    }
    
    ////////////////////////////////// CONTEXT DECOMPOSITION API [SECOND LEVEL] //////////////////////////////////
    
    // CONTEXT AS A WHOLE:
    /**
     * Returns <code>true</code> if masterEntity's context exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean contextOfMasterEntityNotEmpty() {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                return ((AbstractFunctionalEntityWithCentreContext<?>) masterEntity).context() != null;
            }
        }
        return false;
    }
    
    /**
     * Returns <code>true</code> if masterEntity's context does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean contextOfMasterEntityEmpty() {
        return !contextOfMasterEntityNotEmpty();
    }
    
    default CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContextOfMasterEntity() {
        return ((AbstractFunctionalEntityWithCentreContext) masterEntity()).context();
    }
    
    // MASTER ENTITY:
    /**
     * Returns <code>true</code> if masterEntity's master entity exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean masterEntityOfMasterEntityInstanceOf(final Class<M> type) {
        return contextOfMasterEntityNotEmpty() && decompose(getContextOfMasterEntity()).masterEntityInstanceOf(type);
    }
    
    /**
     * Returns masterEntity's master entity.
     * 
     * @return
     */
    default AbstractEntity<?> masterEntityOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).masterEntity();
    }
    
    /**
     * Returns masterEntity's master entity of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M masterEntityOfMasterEntity(final Class<M> type) {
        return decompose(getContextOfMasterEntity()).masterEntity(type);
    }
    
    // MASTER ENTITY'S KEY:
    /**
     * Returns <code>true</code> if masterEntity's master entity key exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean keyOfMasterEntityOfMasterEntityInstanceOf(final Class<M> type) {
        return contextOfMasterEntityNotEmpty() && decompose(getContextOfMasterEntity()).keyOfMasterEntityInstanceOf(type);
    }
    
    /**
     * Returns masterEntity's master entity key of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M keyOfMasterEntityOfMasterEntity(final Class<M> type) {
        return decompose(getContextOfMasterEntity()).keyOfMasterEntity(type);
    }
    
    // MASTER ENTITY'S SELECTION CRIT:
    /**
     * Returns masterEntity's master entity selectionCrit.
     * 
     * @return
     */
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCritOfMasterEntityOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).selectionCritOfMasterEntity();
    }
    
    // CHOSEN PROPERTY:
    /**
     * Returns masterEntity's chosen property value: <code>null</code> if chosen property is not applicable,
     * "" if property action for 'this' column was actioned, and non-empty property name otherwise
     * (master property editor actions and centre column actions).
     * 
     * @return
     */
    default String chosenPropertyOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).chosenProperty();
    }
    
    // CURRENT ENTITY:
    /**
     * Returns <code>true</code> if masterEntity's current entity does not exist, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityOfMasterEntityEmpty() {
        return contextOfMasterEntityEmpty() || decompose(getContextOfMasterEntity()).currentEntityEmpty();
    }
    
    /**
     * Returns <code>true</code> if masterEntity's current entity exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean currentEntityOfMasterEntityNotEmpty() {
        return !currentEntityOfMasterEntityEmpty();
    }
    
    /**
     * Returns masterEntity's current entity.
     * 
     * @return
     */
    default AbstractEntity<?> currentEntityOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).currentEntity();
    }
    
    /**
     * Returns <code>true</code> if masterEntity's current entity exists and is instance of concrete <code>type</code>, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> boolean currentEntityOfMasterEntityInstanceOf(final Class<M> type) {
        return contextOfMasterEntityNotEmpty() && decompose(getContextOfMasterEntity()).currentEntityInstanceOf(type);
    }
    
    /**
     * Returns masterEntity's current entity of concrete <code>type</code>.
     * 
     * @param type
     * @return
     */
    default <M extends AbstractEntity<?>> M currentEntityOfMasterEntity(final Class<M> type) {
        return decompose(getContextOfMasterEntity()).currentEntity(type);
    }
    
    // SELECTION CRITERIA:
    /**
     * Returns <code>true</code> if masterEntity's selection criteria entity exists, <code>false</code> otherwise.
     * 
     * @return
     */
    default boolean selectionCritOfMasterEntityNotEmpty() {
        return contextOfMasterEntityNotEmpty() && decompose(getContextOfMasterEntity()).selectionCritNotEmpty();
    }
    
    /**
     * Returns masterEntity's selection criteria entity.
     * 
     * @return
     */
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCritOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).selectionCrit();
    }
    
    // SELECTED ENTITIES:
    /**
     * Returns masterEntity's selected entities.
     * 
     * @return
     */
    default List<AbstractEntity<?>> selectedEntitiesOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).selectedEntities();
    }
    
    // COMPUTATION:
    /**
     * Returns masterEntity's optional computation.
     * 
     * @return
     */
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computationOfMasterEntity() {
        return decompose(getContextOfMasterEntity()).computation();
    }
    
}
