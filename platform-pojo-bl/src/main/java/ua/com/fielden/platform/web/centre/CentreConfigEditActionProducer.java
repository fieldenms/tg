package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_DESC;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.COPY;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.EDIT;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.SAVE;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * A producer for new instances of entity {@link CentreConfigEditAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigEditActionProducer extends DefaultEntityProducerWithContext<CentreConfigEditAction> {
    private static final String COPY_ACTION_SUFFIX = " (copy)";
    private static final String ERR_CANNOT_BE_EDITED = "Can not be edited.";
    
    @Inject
    public CentreConfigEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigEditAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigEditAction provideDefaultValues(final CentreConfigEditAction entity) {
        if (contextNotEmpty()) {
            // centre context holder is needed to restore criteria entity during saving and to perform 'centreCopier' closure
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = selectionCrit();
            entity.setEditKind(chosenProperty());
            final Optional<String> saveAsName = selectionCrit.saveAsNameSupplier().get();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = applyCriteria(selectionCrit);
            final Map<String, Object> customObject = invalidCustomObject(selectionCrit, appliedCriteriaEntity)
                .map(customObj -> {
                    setSkipUi(entity);
                    setTitleAndDesc(entity, saveAsName, selectionCrit);
                    return customObj;
                })
                .orElseGet(() -> {
                    if (EDIT.name().equals(entity.getEditKind())) {
                        if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                            throw failure(ERR_CANNOT_BE_EDITED);
                        } else {
                            setTitleAndDesc(entity, saveAsName, selectionCrit);
                        }
                    } else if (SAVE.name().equals(entity.getEditKind())) { // SAVE button at the bottom of selection criteria or EDIT button in top right corner
                        if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                            if (!isDefault(saveAsName)) {
                                setTitleAndDesc(entity, saveAsName, selectionCrit, COPY_ACTION_SUFFIX);
                            }
                        } else { // owned configuration should be saved without opening 'Save As...' dialog
                            setSkipUi(entity);
                            setTitleAndDesc(entity, saveAsName, selectionCrit);
                            selectionCrit.freshCentreSaver().run();
                            final Map<String, Object> customObj = getCustomObject(selectionCrit, appliedCriteriaEntity);
                            customObj.remove("wasRun");
                            return customObj;
                        }
                    } else if (COPY.name().equals(entity.getEditKind())) {
                        setSkipUi(entity);
                        entity.setTitle(DEFAULT_CONFIG_TITLE);
                        entity.setDesc(DEFAULT_CONFIG_DESC);
                        selectionCrit.freshCentreCopier().run();
                        // after copying of criteria values against default centre we need to compare it with SAVED version of default centre,
                        // which always holds empty Centre DSL-configured configuration
                        return getCustomObject(selectionCrit, appliedCriteriaEntity, empty());
                    }
                    // in all other situations customObject will hold the same values, applied originally (appliedCriteriaEntity)
                    return getCustomObject(selectionCrit, appliedCriteriaEntity);
                });
            entity.setCustomObject(customObject);
        }
        return entity;
    }
    
    private void setSkipUi(final CentreConfigEditAction entity) {
        entity.setSkipUi(true);
    }
    
    private void setTitleAndDesc(final CentreConfigEditAction entity, final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        setTitleAndDesc(entity, saveAsName, selectionCrit, "");
    }
    
    private void setTitleAndDesc(final CentreConfigEditAction entity, final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final String suffix) {
        final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().apply(saveAsName);
        entity.setTitle(titleAndDesc._1 + suffix);
        entity.setDesc(titleAndDesc._2 + suffix);
    }
    
    private static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> applyCriteria(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        // Get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
        return selectionCrit.freshCentreApplier().apply(selectionCrit.centreContextHolder().getModifHolder());
    }
    
    private static Optional<Map<String, Object>> invalidCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        // Validate criteriaEntity.
        final Result validationResult = appliedCriteriaEntity.isValid();
        if (!validationResult.isSuccessful()) { // If applied criteria entity is valid then perform actual saving.
            return of(getCustomObject(selectionCrit, appliedCriteriaEntity));
        }
        return empty();
    }
    
    public static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        return getCustomObject(selectionCrit, appliedCriteriaEntity, selectionCrit.saveAsNameSupplier().get());
    }
    
    public static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final Optional<String> saveAsNameToCompare) {
        return selectionCrit.centreCustomObjectGetter().apply(appliedCriteriaEntity, saveAsNameToCompare);
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents default configuration,
     * otherwise <code>false</code>.
     * 
     * @param saveAsName
     * @return
     */
    public static boolean isDefault(final Optional<String> saveAsName) {
        return !saveAsName.isPresent();
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents default configuration or inherited from base user configuration,
     * otherwise <code>false</code>.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isDefaultOrInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return isDefault(saveAsName) || isInherited(saveAsName, () -> selectionCrit.loadableCentresSupplier().get().stream());
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents inherited from base user configuration, <code>false</code> otherwise.
     * 
     * @param saveAsName
     * @param streamLoadableConfigurations -- a function to stream loadable configurations for current user
     * @return
     */
    public static boolean isInherited(final Optional<String> saveAsName, final Supplier<Stream<LoadableCentreConfig>> streamLoadableConfigurations) {
        return saveAsName.isPresent() &&
            streamLoadableConfigurations.get()
            .filter(lcc -> lcc.getKey().equals(saveAsName.get()))
            .findAny().map(lcc -> lcc.isInherited()).orElseThrow(() -> failure("Configuration has been deleted."));
    }
    
}