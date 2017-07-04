package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.annotation.CustomProp;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.utils.Pair;

/**
 * A producer for new instances of entity {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater> implements IEntityProducer<CentreConfigUpdater> {
    private final Logger logger = Logger.getLogger(getClass());
    
    @Inject
    public CentreConfigUpdaterProducer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,
            final IApplicationSettings applicationSettings) throws Exception {
        super(factory, CentreConfigUpdater.class, companionFinder);
    }

    @Override
    protected CentreConfigUpdater provideCurrentlyAssociatedValues(final CentreConfigUpdater entity, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> masterEntity) {
        final Class<?> root = masterEntity.getEntityClass();
        final ICentreDomainTreeManagerAndEnhancer freshCentre = masterEntity.freshCentreSupplier().get();
        
        final List<String> freshCheckedProperties = freshCentre.getSecondTick().checkedProperties(root);
        final List<String> freshUsedProperties = freshCentre.getSecondTick().usedProperties(root);
        final List<Pair<String, Ordering>> freshSortedProperties = freshCentre.getSecondTick().orderedProperties(root);
        final Class<?> freshManagedType = freshCentre.getEnhancer().getManagedType(root);
        
        final LinkedHashSet<CustomisableColumn> customisableColumns = createCustomisableColumns(checkedPropertiesWithoutSummaries(freshCheckedProperties, freshManagedType), freshSortedProperties, freshManagedType, factory());
        entity.setCustomisableColumns(customisableColumns);
        
        entity.setChosenIds(
            freshUsedProperties.stream()
            .map(usedProperty -> dslName(usedProperty))
            .collect(Collectors.toCollection(LinkedHashSet::new))
        );
        
        entity.setSortingVals(new ArrayList<>(
            customisableColumns.stream()
            .filter(customisableColumn -> customisableColumn.getSortingNumber() >= 0) // consider only 'sorted' properties
            .sorted((o1, o2) -> o1.getSortingNumber().compareTo(o2.getSortingNumber()))
            .map(customisableColumn -> customisableColumn.getKey() + ':' + (Boolean.TRUE.equals(customisableColumn.getSorting()) ? "asc" : "desc"))
            .collect(Collectors.toCollection(LinkedHashSet::new))
        ));
        return entity;
    }
    
    public static List<String> checkedPropertiesWithoutSummaries(final List<String> checkedProperties, final Class<?> managedType) {
        return checkedProperties.stream()
            .filter(checkedProperty -> "".equals(checkedProperty) || !AbstractDomainTreeRepresentation.isCalculatedAndOfTypes(managedType, checkedProperty, CalculatedPropertyCategory.AGGREGATED_EXPRESSION))
            .collect(Collectors.toList());
    }

    private LinkedHashSet<CustomisableColumn> createCustomisableColumns(final List<String> checkedPropertiesWithoutSummaries, final List<Pair<String, Ordering>> sortedProperties, final Class<?> managedType, final EntityFactory factory) {
        logger.error("CheckedWithoutSummaries: [" + checkedPropertiesWithoutSummaries + "]");
        logger.error("Sorted: [" + sortedProperties + "]");
        
        final LinkedHashSet<CustomisableColumn> result = new LinkedHashSet<>();
        for (final String checkedProp: checkedPropertiesWithoutSummaries) {
            final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, checkedProp);
            final CustomisableColumn customisableColumn = factory.newEntity(CustomisableColumn.class, null, dslName(checkedProp), titleAndDesc.getValue());
            customisableColumn.setTitle(titleAndDesc.getKey());
            if ("".equals(checkedProp) || 
                    (!AnnotationReflector.isPropertyAnnotationPresent(CustomProp.class, managedType, checkedProp) && 
                    !isShortCollection(managedType, checkedProp))
            ) {
                customisableColumn.setSortable(true);
                final Pair<Ordering, Integer> orderingAndNumber = getOrderingAndNumber(sortedProperties, checkedProp);
                if (orderingAndNumber != null) {
                    customisableColumn.setSorting(Ordering.ASCENDING == orderingAndNumber.getKey()); // 'null' is by default, means no sorting exist
                    customisableColumn.setSortingNumber(orderingAndNumber.getValue());
                }
            }
            result.add(customisableColumn);
        }
        return result;
    }

    private Pair<Ordering, Integer> getOrderingAndNumber(final List<Pair<String, Ordering>> orderedProperties, final String prop) {
        for (final Pair<String, Ordering> orderedProperty : orderedProperties) {
            if (orderedProperty.getKey().equals(prop)) {
                return Pair.pair(orderedProperty.getValue(), orderedProperties.indexOf(orderedProperty));
            }
        }
        return null;
    }

    @Override
    protected AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User Role master and for actions on User Role centre
        return context.getSelectionCrit();
    }

    @Override
    protected EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext) {
        return (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) masterEntityFromContext;
    }
}