package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.COPY;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.EDIT;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.SAVE;

import java.util.Map;
import java.util.Optional;

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
            if (EDIT.name().equals(entity.getEditKind())) {
                if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                    throw failure(ERR_CANNOT_BE_EDITED);
                } else {
                    // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
                    final Map<String, Object> freshModifHolder = selectionCrit.centreContextHolder().getModifHolder();
                    final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = selectionCrit.freshCentreApplier().apply(freshModifHolder);
                    
                    // configuration copy / edit will not be performed if current recently applied 'fresh' centre configuration is invalid
                    appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime); // TODO this should not throw any exception instead continue information flow and bind invalid criteria to tg-entity-centre; error toast must be preserved however
                    
                    final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
                    final String title = titleAndDesc._1;
                    final String desc = titleAndDesc._2;
                    
                    if (DEFAULT_CONFIG_TITLE.equals(title)) {
                        // remove brackets from title when copying / editing 'default' centre configuration; brackets are not allowed as per CentreConfigEditActionTitleValidator
                        entity.setTitle(title.replace("[", "").replace("]", ""));
                    } else {
                        entity.setTitle(title);
                    }
                    entity.setDesc(desc);
                }
            } else if (SAVE.name().equals(entity.getEditKind())) { // SAVE button at the bottom of selection criteria or EDIT button in top right corner
                if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                    final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = applyCriteria(selectionCrit);
                    final Map<String, Object> customObject = invalidCustomObject(selectionCrit, appliedCriteriaEntity)
                        .map(customObj -> {
                            entity.setSkipUi(true);
                            final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
                            entity.setTitle(titleAndDesc._1);
                            entity.setDesc(titleAndDesc._2);
                            return customObj;
                        }).orElseGet(() -> {
                            if (!isDefault(saveAsName)) {
                                final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
                                entity.setTitle(titleAndDesc._1 + COPY_ACTION_SUFFIX);
                                entity.setDesc(titleAndDesc._2 + COPY_ACTION_SUFFIX);
                            }
                            return getCustomObject(selectionCrit, appliedCriteriaEntity);
                        });
                    entity.setCustomObject(customObject);
                    
                    
                    
                    
                    
//                    // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
//                    final Map<String, Object> freshModifHolder = selectionCrit.centreContextHolder().getModifHolder();
//                    final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = selectionCrit.freshCentreApplier().apply(freshModifHolder);
//                    
//                    // configuration copy / edit will not be performed if current recently applied 'fresh' centre configuration is invalid
//                    appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime); // TODO this should not throw any exception instead continue information flow and bind invalid criteria to tg-entity-centre; error toast must be preserved however
//                    
//                    final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
//                    final String title = titleAndDesc._1;
//                    final String desc = titleAndDesc._2;
//                    
//                    final String actionKindSuffix = COPY_ACTION_SUFFIX;
//                    if (DEFAULT_CONFIG_TITLE.equals(title)) {
//                        // remove brackets from title when copying / editing 'default' centre configuration; brackets are not allowed as per CentreConfigEditActionTitleValidator
//                        entity.setTitle(title.replace("[", "").replace("]", "") + actionKindSuffix);
//                    } else {
//                        entity.setTitle(title + actionKindSuffix);
//                    }
//                    entity.setDesc(desc + actionKindSuffix);
                } else { // owned configuration should be saved without opening 'Save As...' dialog
                    entity.setSkipUi(true);
                    final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
                    entity.setTitle(titleAndDesc._1);
                    entity.setDesc(titleAndDesc._2);
                    
                    // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
                    final Map<String, Object> freshModifHolder = selectionCrit.centreContextHolder().getModifHolder();
                    final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = selectionCrit.freshCentreApplier().apply(freshModifHolder);
                    
                    
//                  // Validate criteria entity with the check for 'required' properties.
//                  final Result validationResult = appliedCriteriaEntity.isValid();
//                  final ICentreDomainTreeManagerAndEnhancer freshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
//                  if (validationResult.isSuccessful()) { // If applied criteria entity is valid then perform actual saving.
//                      initAndCommit(gdtm, miType, SAVED_CENTRE_NAME, saveAsName, device, freshCentre, null);
//                  }
                    
                    entity.setCustomObject(selectionCrit.centreCustomObjectGetter().apply(appliedCriteriaEntity));
                }
            } else if (COPY.name().equals(entity.getEditKind())) {
                // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
                final Map<String, Object> freshModifHolder = selectionCrit.centreContextHolder().getModifHolder();
                final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = selectionCrit.freshCentreApplier().apply(freshModifHolder);
                
                // configuration copy / edit will not be performed if current recently applied 'fresh' centre configuration is invalid
                appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime); // TODO this should not throw any exception instead continue information flow and bind invalid criteria to tg-entity-centre; error toast must be preserved however
                
                final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().get();
                final String title = titleAndDesc._1;
                final String desc = titleAndDesc._2;
                
                final String actionKindSuffix = COPY_ACTION_SUFFIX;
                if (DEFAULT_CONFIG_TITLE.equals(title)) {
                    // remove brackets from title when copying / editing 'default' centre configuration; brackets are not allowed as per CentreConfigEditActionTitleValidator
                    entity.setTitle(title.replace("[", "").replace("]", "") + actionKindSuffix);
                } else {
                    entity.setTitle(title + actionKindSuffix);
                }
                entity.setDesc(desc + actionKindSuffix);
            }
        }
        return entity;
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
    
    private static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        return selectionCrit.centreCustomObjectGetter().apply(appliedCriteriaEntity);
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
        return isDefault(saveAsName) // default configuration name and title cannot be edited
            ||
            selectionCrit.loadableCentresSupplier().get().stream() // inherited configuration name and title cannot be edited
            .filter(lcc -> lcc.getKey().equals(saveAsName.get()))
            .findAny().map(lcc -> lcc.isInherited()).orElseThrow(() -> new IllegalStateException("Configuration has been deleted."));
    }
    
}