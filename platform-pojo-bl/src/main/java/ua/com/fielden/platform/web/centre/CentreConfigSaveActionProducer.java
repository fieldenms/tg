package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLink;

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
    protected Map<String, Object> performProduce(final CentreConfigSaveAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final boolean isDefaultOrLinkOrInherited) {
        if (isDefaultOrLinkOrInherited) {
            final Optional<String> saveAsName = selectionCrit.saveAsName();
            if (isDefaultOrLink(saveAsName)) {
                makeTitleRequired(entity);
            } else {
                setTitleAndDesc(entity, saveAsName.get(), selectionCrit, COPY_ACTION_SUFFIX);
            }
            return getCustomObject(selectionCrit, appliedCriteriaEntity, empty(), empty()); // not yet transitioned to another config -- do not update configUuid / saveAsName / shareError on client-side
        } else { // owned configuration should be saved without opening 'Save As...' dialog
            entity.setSkipUi(true);
            selectionCrit.saveFreshCentre();
            return getCustomObject(selectionCrit, appliedCriteriaEntity, empty(), empty()); // config left the same (no transition occurred) -- do not update configUuid / saveAsName / shareError on client-side
        }
    }
    
}