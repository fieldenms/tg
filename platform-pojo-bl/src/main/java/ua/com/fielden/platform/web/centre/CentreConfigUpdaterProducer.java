package ua.com.fielden.platform.web.centre;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createCustomisableColumns;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createSortingVals;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/**
 * A producer for new instances of entity {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater, String, CustomisableColumn> {
    private final ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater, String, CustomisableColumn> controller;
    
    @Inject
    public CentreConfigUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigUpdater.class, companionFinder);
        this.controller = new CentreConfigUpdaterController(null);
    }
    
    @Override
    protected ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater, String, CustomisableColumn> controller() {
        return controller;
    }
    
    @Override
    protected CentreConfigUpdater provideCurrentlyAssociatedValues(final CentreConfigUpdater entity, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> masterEntity) {
        entity.setMasterEntityHolder(masterEntity.centreContextHolder());
        
        final Class<?> root = masterEntity.getEntityClass();
        
        // When opening Customise Columns dialog we need to show the order / sort / visibility that is currently present in EGI.
        // It however does not mean that this configuration is synchronised with 'fresh' centre configuration.
        // In such case 'Show selection criteria' has orange colour.
        // When APPLY is made in Customise Columns dialog the changes are applied against 'fresh' and 'previouslyRun' centres, but not against 'saved' one.
        // Thus Discard button on selection criteria can be used to Discard all centre changes including those made in Customise Columns dialog.
        // Very similar situation is with Change Columns Width action.
        final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = masterEntity.previouslyRunCentre();
        
        final List<String> previouslyRunCheckedProperties = previouslyRunCentre.getSecondTick().checkedProperties(root);
        final List<String> previouslyRunUsedProperties = previouslyRunCentre.getSecondTick().usedProperties(root);
        final List<Pair<String, Ordering>> previouslyRunSortedProperties = previouslyRunCentre.getSecondTick().orderedProperties(root);
        final Class<?> previouslyRunManagedType = previouslyRunCentre.getEnhancer().getManagedType(root);
        
        // provide chosenIds into the action
        entity.setChosenIds(
            previouslyRunUsedProperties.stream()
            .map(WebApiUtils::dslName)
            .collect(toCollection(LinkedHashSet::new))
        );
        
        // provide customisable columns into the action
        final Set<CustomisableColumn> customisableColumns = createCustomisableColumns(checkedPropertiesWithoutSummaries(previouslyRunCheckedProperties, previouslyRunManagedType), previouslyRunSortedProperties, previouslyRunManagedType, factory());
        entity.setCustomisableColumns(customisableColumns);
        
        // provide sorting values into the action
        entity.setSortingVals(createSortingVals(customisableColumns));
        return entity;
    }
    
}