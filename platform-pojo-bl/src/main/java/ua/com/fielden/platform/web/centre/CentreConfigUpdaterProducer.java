package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createCustomisableColumns;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createSortingVals;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        final ICentreDomainTreeManagerAndEnhancer freshCentre = masterEntity.freshCentreSupplier().get(); // TODO maybe it would be more correct to use PREVIOUSLY_RUN centre here
        
        final List<String> freshCheckedProperties = freshCentre.getSecondTick().checkedProperties(root);
        final List<String> freshUsedProperties = freshCentre.getSecondTick().usedProperties(root);
        final List<Pair<String, Ordering>> freshSortedProperties = freshCentre.getSecondTick().orderedProperties(root);
        final Class<?> freshManagedType = freshCentre.getEnhancer().getManagedType(root);
        
        // provide chosenIds into the action
        entity.setChosenIds(
            freshUsedProperties.stream()
            .map(usedProperty -> dslName(usedProperty))
            .collect(Collectors.toCollection(LinkedHashSet::new))
        );
        
        // provide customisable columns into the action
        final LinkedHashSet<CustomisableColumn> customisableColumns = createCustomisableColumns(checkedPropertiesWithoutSummaries(freshCheckedProperties, freshManagedType), freshSortedProperties, freshManagedType, factory());
        entity.setCustomisableColumns(customisableColumns);
        
        // provide sorting values into the action
        entity.setSortingVals(createSortingVals(customisableColumns));
        return entity;
    }
    
}