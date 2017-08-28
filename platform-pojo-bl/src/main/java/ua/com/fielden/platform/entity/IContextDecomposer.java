package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    
    CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> getContext();
    void setContext(final CentreContext<? extends AbstractEntity<?>, AbstractEntity<?>> context);
    
    ////////////////////////////////////////////
    
    default String getChosenProperty() {
        return getContext() == null ? null : getContext().getChosenProperty();
    }
    
    default AbstractEntity<?> getCurrEntity() {
        return getContext() == null ? null :
               getContext().getSelectedEntities().size() == 1 ? getContext().getCurrEntity() : null;
    }
    
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> getComputation() {
        return getContext() == null ? Optional.empty() : getContext().getComputation();
    }
    
    default List<AbstractEntity<?>> getSelectedEntities() {
        return getContext() == null ? Collections.unmodifiableList(new ArrayList<>()) : getContext().getSelectedEntities();
    }
    
    ////////////////////////////////// CONTEXT DECOMPOSITION API //////////////////////////////////
    
    // MASTER ENTITY:
    default AbstractEntity<?> masterEntity() {
        return getContext() == null ? null : getContext().getMasterEntity();
    }
    
    default <M extends AbstractEntity<?>> M masterEntity(final Class<M> type) {
        return (M) masterEntity();
    }
    
    default boolean masterEntityEmpty() {
        return masterEntity() == null;
    }
    
    default boolean masterEntityNotEmpty() {
        return !masterEntityEmpty();
    }
    
    default <M extends AbstractEntity<?>> boolean masterEntityInstanceOf(final Class<M> type) {
        return masterEntityNotEmpty() && type.isAssignableFrom(masterEntity().getClass());
    }
    
    default <M extends AbstractEntity<?>> boolean masterEntityKeyInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (masterEntity.get("key") != null) {
                return type.isAssignableFrom(masterEntity.get("key").getClass());
            }
        }
        return false;
    }
    
    default <M extends AbstractEntity<?>> boolean masterEntityKeyOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
                if (masterFuncEntity.context() != null && masterFuncEntity.context().getMasterEntity() != null) {
                    final AbstractEntity<?> masterEntityOfMasterEntity = masterFuncEntity.context().getMasterEntity();
                    if (masterEntityOfMasterEntity.get("key") != null) {
                        return type.isAssignableFrom(masterEntityOfMasterEntity.get("key").getClass());
                    }
                }
            }
        }
        return false;
    }
    
    
    default <M extends AbstractEntity<?>> boolean masterEntityOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
                return decompose(masterFuncEntity.context()).masterEntityInstanceOf(type);
            }
        }
        return false;
    }
    
    default boolean selectionCritOfMasterEntityNotEmpty() {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
                return masterFuncEntity.context() != null && masterFuncEntity.context().getSelectionCrit() != null;
            }
        }
        return false;
    }
    
    default EnhancedCentreEntityQueryCriteria<?, ?> selectionCritOfMasterEntity() {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        return masterFuncEntity.context().getSelectionCrit();
    }
    
    default <M extends AbstractEntity<?>> M masterEntityKey(final Class<M> type) {
        final AbstractEntity<?> masterEntity = masterEntity();
        return (M) masterEntity.get("key");
    }
    
    default <M extends AbstractEntity<?>> M masterEntityKeyOfMasterEntity(final Class<M> type) {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        final AbstractEntity<?> masterEntityOfMasterEntity = masterFuncEntity.context().getMasterEntity();
        return (M) masterEntityOfMasterEntity.get("key");
    }
    
    default <M extends AbstractEntity<?>> M masterEntityOfMasterEntity(final Class<M> type) {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        final AbstractEntity<?> masterEntityOfMasterEntity = masterFuncEntity.context().getMasterEntity();
        return (M) masterEntityOfMasterEntity;
    }
    
    // CHOSEN PROPERTY:
    /**
     * Returns chosen property value: <code>null</code> if chosen property is not applicable,
     * "" if property action for 'this' column was actioned, and non-empty property name otherwise
     * (master property editor actions and centre column actions).
     * 
     * @return
     */
    default String chosenProperty() {
        return getChosenProperty();
    }
    
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
    
    // CURRENT ENTITY:
    default AbstractEntity<?> currentEntity() {
        return getCurrEntity();
    }
    
    default <M extends AbstractEntity<?>> M currentEntity(final Class<M> type) {
        return (M) currentEntity();
    }
    
    default boolean currentEntityEmpty() {
        return currentEntity() == null;
    }
    
    default boolean currentEntityNotEmpty() {
        return !currentEntityEmpty();
    }
    
    default <M extends AbstractEntity<?>> boolean currentEntityInstanceOf(final Class<M> type) {
        return currentEntityNotEmpty() && type.isAssignableFrom(currentEntity().getClass());
    }
    
    default <M extends AbstractEntity<?>> boolean currentEntityOfMasterEntityInstanceOf(final Class<M> type) {
        if (masterEntityNotEmpty()) {
            final AbstractEntity<?> masterEntity = masterEntity();
            if (AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(masterEntity.getClass())) {
                final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
                return decompose(masterFuncEntity.context()).currentEntityInstanceOf(type);
            }
        }
        return false;
    }
    
    default <M extends AbstractEntity<?>> M currentEntityOfMasterEntity(final Class<M> type) {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        return decompose(masterFuncEntity.context()).currentEntity(type);
    }
    
    // COMPUTATION:
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation() {
        return getComputation();
    }
    
    default Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computationOfMasterEntity() {
        final AbstractEntity<?> masterEntity = masterEntity();
        final AbstractFunctionalEntityWithCentreContext masterFuncEntity = (AbstractFunctionalEntityWithCentreContext) masterEntity;
        return decompose(masterFuncEntity.context()).computation();
    }
    
    // SELECTED ENTITIES:
    default List<AbstractEntity<?>> selectedEntities() {
        return getSelectedEntities();
    }
    
    default boolean selectedEntitiesEmpty() {
        return selectedEntities().isEmpty();
    }
    
    default boolean selectedEntitiesNonEmpty() {
        return !selectedEntitiesEmpty();
    }
    
    default boolean selectedEntitiesOnlyOne() {
        return selectedEntities().size() == 1;
    }
    
    default boolean selectedEntitiesMoreThanOne() {
        return selectedEntities().size() > 1;
    }
    
}
