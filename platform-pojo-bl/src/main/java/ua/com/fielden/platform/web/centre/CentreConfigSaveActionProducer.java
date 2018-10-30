package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.AbstractCentreConfigAction.WAS_RUN_NAME;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefault;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigSaveAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigSaveActionProducer extends AbstractCentreConfigCommitActionProducer<CentreConfigSaveAction> {
    private static final String COPY_ACTION_SUFFIX = " (copy)";
    
    @Inject
    public CentreConfigSaveActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, companionFinder, CentreConfigSaveAction.class);
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     */
    @Override
    protected Map<String, Object> performProduce(final CentreConfigSaveAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final boolean isDefaultOrInherited) {
        if (isDefaultOrInherited) {
            final Optional<String> saveAsName = selectionCrit.saveAsName();
            if (!isDefault(saveAsName)) {
                setTitleAndDesc(entity, saveAsName.get(), selectionCrit, COPY_ACTION_SUFFIX);
            } else {
                makeTitleRequired(entity);
            }
            return getCustomObject(selectionCrit, appliedCriteriaEntity);
        } else { // owned configuration should be saved without opening 'Save As...' dialog
            entity.setSkipUi(true);
            selectionCrit.saveFreshCentre();
            final Map<String, Object> customObj = getCustomObject(selectionCrit, appliedCriteriaEntity);
            customObj.remove(WAS_RUN_NAME); // avoid making VIEW button disabled if it is enabled
            return customObj;
        }
    }
    
}