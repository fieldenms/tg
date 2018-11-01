package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.applyCriteria;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.invalidCustomObject;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigDuplicateAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigDuplicateActionProducer extends DefaultEntityProducerWithContext<CentreConfigDuplicateAction> {
    
    @Inject
    public CentreConfigDuplicateActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigDuplicateAction.class, companionFinder);
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     */
    @Override
    protected CentreConfigDuplicateAction provideDefaultValues(final CentreConfigDuplicateAction entity) {
        if (contextNotEmpty()) {
            // apply criteria entity
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = applyCriteria(selectionCrit());
            entity.setCustomObject(
                // and avoid configuration duplication if centre is invalid
                invalidCustomObject(selectionCrit(), appliedCriteriaEntity)
                .orElseGet(() -> {
                    // otherwise perform actual copy
                    selectionCrit().configDuplicateAction();
                    // and after copying of criteria values against default centre compare it with SAVED version of default centre,
                    // which always holds empty Centre DSL-configured configuration
                    return getCustomObject(selectionCrit(), appliedCriteriaEntity, empty());
                })
            );
        }
        return entity;
    }
    
}