package ua.com.fielden.platform.web.centre;

import static java.util.stream.Collectors.toCollection;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createCustomisableColumns;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.createSortingVals;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;

import java.util.LinkedHashSet;
import java.util.List;

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
            final ICentreDomainTreeManagerAndEnhancer baseCentre = criteriaEntity.baseCentre();
            final Class<?> defaultManagedType = baseCentre.getEnhancer().getManagedType(root);
            
            // provide default visible properties into the action
            final List<String> defaultUsedProperties = baseCentre.getSecondTick().usedProperties(root);
            entity.setDefaultVisibleProperties(
                defaultUsedProperties.stream()
                .map(WebApiUtils::dslName)
                .collect(toCollection(LinkedHashSet::new))
            );
            
            final List<String> defaultCheckedPropertiesWithoutSummaries = checkedPropertiesWithoutSummaries(baseCentre.getSecondTick().checkedProperties(root), defaultManagedType);
            // provide default sorting values into the action
            entity.setDefaultSortingVals(createSortingVals(
                createCustomisableColumns(
                    defaultCheckedPropertiesWithoutSummaries,
                    baseCentre.getSecondTick().orderedProperties(root),
                    defaultManagedType,
                    factory()
                )
            ));
        }
        return entity;
    }
}