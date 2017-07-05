package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
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
        if (getMasterEntity() != null) {
            final CentreConfigUpdater masterEntity = (CentreConfigUpdater) getMasterEntity();
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = masterEntity.getContext().getSelectionCrit();
            
            final Class<?> root = criteriaEntity.getEntityClass();
            final ICentreDomainTreeManagerAndEnhancer defaultCentre = criteriaEntity.defaultCentreSupplier().get();
            final Class<?> defaultManagedType = defaultCentre.getEnhancer().getManagedType(root);
            
            final List<String> defaultCheckedPropertiesWithoutSummaries = CentreConfigUpdaterProducer.checkedPropertiesWithoutSummaries(defaultCentre.getSecondTick().checkedProperties(root), defaultManagedType);
            entity.setDefaultVisibleProperties(
                defaultCheckedPropertiesWithoutSummaries.stream()
                .map(checkedProperty -> dslName(checkedProperty))
                .collect(Collectors.toCollection(LinkedHashSet::new))
            );
            
            final LinkedHashSet<CustomisableColumn> customisableColumns = CentreConfigUpdaterProducer.createCustomisableColumns(defaultCheckedPropertiesWithoutSummaries, defaultCentre.getSecondTick().orderedProperties(root), defaultManagedType, factory());
            entity.setDefaultSortingVals(CentreConfigUpdaterProducer.createSortingVals(customisableColumns));
        }
        return entity;
    }
}