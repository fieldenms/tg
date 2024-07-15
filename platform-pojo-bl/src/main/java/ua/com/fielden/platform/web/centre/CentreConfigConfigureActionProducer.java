package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.applyCriteria;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isLinkOrInherited;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigConfigureAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigConfigureActionProducer extends DefaultEntityProducerWithContext<CentreConfigConfigureAction> {
    private static final String ERR_CANNOT_BE_CONFIGURED = "Only saved and default configurations can be configured.";
    
    @Inject
    public CentreConfigConfigureActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigConfigureAction.class, companionFinder);
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     */
    @Override
    protected CentreConfigConfigureAction provideDefaultValues(final CentreConfigConfigureAction entity) {
        if (contextNotEmpty()) {
            final Optional<String> saveAsName = selectionCrit().saveAsName();
            if (isLinkOrInherited(saveAsName, selectionCrit())) { // this also throws early failure in case where current configuration was deleted
                throw failure(ERR_CANNOT_BE_CONFIGURED);
            } else {
                // centre context holder is needed to restore criteria entity during saving and to perform appropriate closure
                entity.setCentreContextHolder(selectionCrit().centreContextHolder());
                // apply criteria entity
                final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = applyCriteria(selectionCrit());
                entity.setRunAutomatically(selectionCrit().centreRunAutomatically(saveAsName));
                entity.setCustomObject(getCustomObject(selectionCrit(), appliedCriteriaEntity, empty(), empty())); // no transitioning to another config occurs (neither here, in producer, nor in dao) -- do not update configUuid / saveAsName / shareError on client-side
            }
        }
        return entity;
    }
    
}