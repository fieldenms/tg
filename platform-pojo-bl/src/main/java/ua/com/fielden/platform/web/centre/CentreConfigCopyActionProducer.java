package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;

/**
 * A producer for new instances of entity {@link CentreConfigCopyAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigCopyActionProducer extends DefaultEntityProducerWithContext<CentreConfigCopyAction> {
    
    @Inject
    public CentreConfigCopyActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigCopyAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigCopyAction provideDefaultValues(final CentreConfigCopyAction entity) {
        if (contextNotEmpty()) {
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            final EnhancedCentreEntityQueryCriteria<?, ?> previouslyRunSelectionCrit = selectionCrit();
            
            // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
            final Map<String, Object> freshModifHolder = previouslyRunSelectionCrit.centreContextHolder().getModifHolder();
            
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = previouslyRunSelectionCrit.freshCentreApplier().apply(freshModifHolder);
            
            // 'saveAs' will not be created if current recently applied 'fresh' centre configuration is invalid
            appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime);
            
            entity.setKey("default" + chosenProperty());
            if (chosenPropertyRepresentsThisColumn()) {
                entity.setTitle(DEFAULT_CONFIG_TITLE + " (copy)");
            } else {
                entity.setTitle(chosenProperty() + " (copy)");
            }
        }
        return entity;
    }
    
}