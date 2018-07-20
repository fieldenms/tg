package ua.com.fielden.platform.web.centre;

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
    protected Map<String, Object> performProduce(final CentreConfigEditAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final boolean isDefaultOrInherited) {
        if (isDefaultOrInherited) {
            throw failure(ERR_CANNOT_BE_EDITED);
        } else {
            setTitleAndDesc(entity, selectionCrit.saveAsNameSupplier().get().get(), selectionCrit);
            return getCustomObject(selectionCrit, appliedCriteriaEntity);
        }
    }
    
}