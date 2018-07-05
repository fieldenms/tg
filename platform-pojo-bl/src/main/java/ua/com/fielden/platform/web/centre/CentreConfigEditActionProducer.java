package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.EDIT;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.SAVE;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.applyCriteria;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.invalidCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefault;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrInherited;

import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
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
                    return customObj;
                })
                .orElseGet(() -> {
                    if (EDIT.name().equals(entity.getEditKind())) { // EDIT button in top right corner
                        if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                            throw failure(ERR_CANNOT_BE_EDITED);
                        } else {
                            setTitleAndDesc(entity, saveAsName.get(), selectionCrit);
                        }
                    } else if (SAVE.name().equals(entity.getEditKind())) { // SAVE button at the bottom of selection criteria
                        if (isDefaultOrInherited(saveAsName, selectionCrit)) {
                            if (!isDefault(saveAsName)) {
                                setTitleAndDesc(entity, saveAsName.get(), selectionCrit, COPY_ACTION_SUFFIX);
                            } else {
                                makeTitleAndDescRequired(entity);
                            }
                        } else { // owned configuration should be saved without opening 'Save As...' dialog
                            setSkipUi(entity);
                            selectionCrit.freshCentreSaver().run();
                            final Map<String, Object> customObj = getCustomObject(selectionCrit, appliedCriteriaEntity);
                            customObj.remove("wasRun");
                            return customObj;
                        }
                    }
                    // in all other situations customObject will hold the same values, applied originally (appliedCriteriaEntity)
                    return getCustomObject(selectionCrit, appliedCriteriaEntity);
                });
            entity.setCustomObject(customObject);
        }
        return entity;
    }
    
    private static void setSkipUi(final CentreConfigEditAction entity) {
        entity.setSkipUi(true);
    }
    
    private static void setTitleAndDesc(final CentreConfigEditAction entity, final String saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        setTitleAndDesc(entity, saveAsName, selectionCrit, "");
    }
    
    private static void setTitleAndDesc(final CentreConfigEditAction entity, final String saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final String suffix) {
        makeTitleAndDescRequired(entity);
        
        // change values
        final T2<String, String> titleAndDesc = selectionCrit.centreTitleAndDescGetter().apply(of(saveAsName)).get();
        entity.setTitle(titleAndDesc._1 + suffix);
        entity.setDesc(titleAndDesc._2 + suffix);
    }
    
    private static void makeTitleAndDescRequired(final CentreConfigEditAction entity) {
        // make title and desc required
        entity.getProperty("title").setRequired(true);
        entity.getProperty("desc").setRequired(true);
    }
    
}