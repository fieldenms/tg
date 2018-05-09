package ua.com.fielden.platform.web.centre;

import java.util.LinkedHashSet;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;

//extends DefaultEntityProducerWithContext<CentreConfigLoadAction> {
//    
//    @Inject
//    public CentreConfigLoadActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
//        super(factory, CentreConfigLoadAction.class, companionFinder);
//    }
//    
//    @Override
//    protected CentreConfigLoadAction provideDefaultValues(final CentreConfigLoadAction entity) {
//        if (contextNotEmpty()) {
//            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
//            
//            final EnhancedCentreEntityQueryCriteria<?, ?> previouslyRunSelectionCrit = selectionCrit();
//            
//            // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
//            final Map<String, Object> freshModifHolder = previouslyRunSelectionCrit.centreContextHolder().getModifHolder();
//            
//            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = previouslyRunSelectionCrit.freshCentreApplier().apply(freshModifHolder);
//            
//            // 'saveAs' will not be created if current recently applied 'fresh' centre configuration is invalid
//            appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime);
//            
//            entity.setKey("default" + chosenProperty());
//            if (chosenPropertyRepresentsThisColumn()) {
//                entity.setTitle("Default config (copy)"); // TODO perhaps actual default centre name is needed (like 'Work Activities (copy)' ?)
//            } else {
//                entity.setTitle(chosenProperty() + " (copy)");
//            }
//        }
//        // TODO perhaps desc copying is also needed
//        return entity;
//    }
//    
//}

/**
 * A producer for new instances of entity {@link CentreConfigLoadAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigLoadActionProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigLoadAction, String, LoadableCentreConfig> {
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
        System.out.println(selectionCrit());
        System.out.println(selectionCrit());
        
//        entity.setMasterEntityHolder(masterEntity.centreContextHolder());
//        
//        final Class<?> root = masterEntity.getEntityClass();
//        final ICentreDomainTreeManagerAndEnhancer freshCentre = masterEntity.freshCentreSupplier().get();
//        
//        final List<String> freshCheckedProperties = freshCentre.getSecondTick().checkedProperties(root);
//        final List<String> freshUsedProperties = freshCentre.getSecondTick().usedProperties(root);
//        final List<Pair<String, Ordering>> freshSortedProperties = freshCentre.getSecondTick().orderedProperties(root);
//        final Class<?> freshManagedType = freshCentre.getEnhancer().getManagedType(root);
//        

        
        final T2<LinkedHashSet<LoadableCentreConfig>, Optional<String>> configsAndSaveAsName = selectionCrit().loadableCentresSupplier().get();
        
        // provide loadable configurations into the action
        entity.setCentreConfigurations(configsAndSaveAsName._1);
        
        final LinkedHashSet<String> chosenIds = new LinkedHashSet<>();
        chosenIds.add(configsAndSaveAsName._2.orElse("DEFAULT!"));
        // provide chosenIds into the action
        entity.setChosenIds(chosenIds);
        
//        // provide sorting values into the action
//        entity.setSortingVals(createSortingVals(customisableColumns));
        return entity;
    }
    
}