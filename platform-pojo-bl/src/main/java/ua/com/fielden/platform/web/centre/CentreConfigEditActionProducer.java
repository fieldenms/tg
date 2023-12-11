package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigEditAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigEditActionProducer extends AbstractCentreConfigCommitActionProducer<CentreConfigEditAction> {
    private static final String ERR_CANNOT_BE_EDITED = "Only saved configurations can be edited.";
    
    @Inject
    public CentreConfigEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, companionFinder, CentreConfigEditAction.class);
    }
    
    @Override
    protected Map<String, Object> performProduce(final CentreConfigEditAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final boolean isDefaultOrLinkOrInherited) {
        if (isDefaultOrLinkOrInherited) {
            throw failure(ERR_CANNOT_BE_EDITED);
        } else {
            final String saveAsName = selectionCrit.saveAsName().get();
            setTitleAndDesc(entity, saveAsName, selectionCrit);
            entity.setDashboardable(selectionCrit.centreDashboardable(of(saveAsName)));
            entity.setDashboardRefreshFrequency(selectionCrit.centreDashboardRefreshFrequency(of(saveAsName)));
            return getCustomObject(selectionCrit, appliedCriteriaEntity, empty(), empty()); // not yet transitioned to another config -- do not update configUuid / saveAsName / shareError on client-side
        }
    }
    
}