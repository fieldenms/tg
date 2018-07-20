package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;

import java.util.LinkedHashSet;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigLoadAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigLoadActionProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigLoadAction, String, LoadableCentreConfig> {
    private static final String ERR_NO_CONFIGURATIONS_TO_LOAD = "There are no configurations to load.";
    private final ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigLoadAction, String, LoadableCentreConfig> controller;
    
    @Inject
    public CentreConfigLoadActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigLoadAction.class, companionFinder);
        this.controller = new CentreConfigLoadActionController();
    }
    
    @Override
    protected ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigLoadAction, String, LoadableCentreConfig> controller() {
        return controller;
    }
    
    @Override
    protected CentreConfigLoadAction provideCurrentlyAssociatedValues(final CentreConfigLoadAction entity, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> masterEntity) {
        if (contextNotEmpty()) {
            // centre context holder is needed to restore criteria entity during saving and to perform 'nheritedCentreUpdater' closure
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            // provide loadable configurations into the action
            entity.setCentreConfigurations(new LinkedHashSet<>(selectionCrit().loadableCentreConfigs()));
            
            if (entity.getCentreConfigurations().isEmpty()) {
                throw failure(ERR_NO_CONFIGURATIONS_TO_LOAD);
            }
            
            final LinkedHashSet<String> chosenIds = new LinkedHashSet<>();
            selectionCrit().saveAsName().ifPresent(chosenIds::add);
            
            // provide chosenIds into the action
            entity.setChosenIds(chosenIds);
        }
        
        return entity;
    }
    
}