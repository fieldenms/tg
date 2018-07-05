package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefault;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrInherited;

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
public class CentreConfigSaveActionProducer extends AbstractCentreConfigEditActionProducer<CentreConfigSaveAction> {
    private static final String COPY_ACTION_SUFFIX = " (copy)";
    
    @Inject
    public CentreConfigSaveActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, companionFinder, CentreConfigSaveAction.class);
    }
    
    @Override
    protected Map<String, Object> performProduce(final CentreConfigSaveAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        final Optional<String> saveAsName = selectionCrit.saveAsNameSupplier().get();
        if (isDefaultOrInherited(saveAsName, selectionCrit)) {
            if (!isDefault(saveAsName)) {
                setTitleAndDesc(entity, saveAsName.get(), selectionCrit, COPY_ACTION_SUFFIX);
            } else {
                makeTitleAndDescRequired(entity);
            }
            return getCustomObject(selectionCrit, appliedCriteriaEntity);
        } else { // owned configuration should be saved without opening 'Save As...' dialog
            entity.setSkipUi(true);
            selectionCrit.freshCentreSaver().run();
            final Map<String, Object> customObj = getCustomObject(selectionCrit, appliedCriteriaEntity);
            customObj.remove("wasRun");
            return customObj;
        }
    }
    
}