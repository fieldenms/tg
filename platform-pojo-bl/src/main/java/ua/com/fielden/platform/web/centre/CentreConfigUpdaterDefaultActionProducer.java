package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createCustomisableColumns;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createSortingVals;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * Producer for {@link CentreConfigUpdaterDefaultAction}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterDefaultActionProducer extends DefaultEntityProducerWithContext<CentreConfigUpdaterDefaultAction> {

    @Inject
    public CentreConfigUpdaterDefaultActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigUpdaterDefaultAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigUpdaterDefaultAction provideDefaultValues(final CentreConfigUpdaterDefaultAction entity) {
        if (ofMasterEntity().selectionCritNotEmpty()) {
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = ofMasterEntity().selectionCrit();
            
            final Class<?> root = criteriaEntity.getEntityClass();
            final ICentreDomainTreeManagerAndEnhancer defaultCentre = criteriaEntity.defaultCentreSupplier().get();
            final Class<?> defaultManagedType = defaultCentre.getEnhancer().getManagedType(root);
            
            // provide default visible properties into the action
            final List<String> defaultCheckedPropertiesWithoutSummaries = checkedPropertiesWithoutSummaries(defaultCentre.getSecondTick().checkedProperties(root), defaultManagedType);
            entity.setDefaultVisibleProperties(
                defaultCheckedPropertiesWithoutSummaries.stream()
                .map(checkedProperty -> dslName(checkedProperty))
                .collect(Collectors.toCollection(LinkedHashSet::new))
            );
            
            // provide default sorting values into the action
            entity.setDefaultSortingVals(createSortingVals(
                createCustomisableColumns(
                    defaultCheckedPropertiesWithoutSummaries,
                    defaultCentre.getSecondTick().orderedProperties(root),
                    defaultManagedType,
                    factory()
                )
            ));
        }
        return entity;
    }
}