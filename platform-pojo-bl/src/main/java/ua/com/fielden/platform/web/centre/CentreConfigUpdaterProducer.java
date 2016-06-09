package ua.com.fielden.platform.web.centre;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CustomProp;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/**
 * A producer for new instances of entity {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater> implements IEntityProducer<CentreConfigUpdater> {

    @Inject
    public CentreConfigUpdaterProducer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,
            final IApplicationSettings applicationSettings) throws Exception {
        super(factory, CentreConfigUpdater.class, companionFinder);
    }

    @Override
    // @Authorise(UserRoleReviewToken.class)
    protected CentreConfigUpdater provideCurrentlyAssociatedValues(final CentreConfigUpdater entity, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> masterEntity) {
        final LinkedHashSet<SortingProperty> sortingProperties = createSortingProperties(masterEntity.freshCentreSupplier().get(), masterEntity.getEntityClass(), masterEntity.getManagedType(), factory());
        entity.setSortingProperties(sortingProperties);
        entity.getProperty("sortingProperties").resetState();

        //final Set<String> chosenSortingPropertyIds = new LinkedHashSet<>(sortingProperties.stream().map(item -> item.getKey()).collect(Collectors.toList()));
        //entity.setChosenIds(chosenSortingPropertyIds);
        return entity;
    }

    private LinkedHashSet<SortingProperty> createSortingProperties(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<?> root, final Class<?> managedType, final EntityFactory factory) {
        final List<String> checkedProperties = cdtmae.getSecondTick().checkedProperties(root);
        final List<Pair<String, Ordering>> orderedProperties = cdtmae.getSecondTick().orderedProperties(root);
        final LinkedHashSet<SortingProperty> result = new LinkedHashSet<>();
        for (final String checkedProp: checkedProperties) {
            if ("".equals(checkedProp) || (!AbstractDomainTreeRepresentation.isCalculatedAndOfTypes(managedType, checkedProp, CalculatedPropertyCategory.AGGREGATED_EXPRESSION) && 
                    !AnnotationReflector.isPropertyAnnotationPresent(CustomProp.class, managedType, checkedProp))) {
                final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, checkedProp);
                final SortingProperty sortingProperty = factory.newEntity(SortingProperty.class, null, "".equals(checkedProp) ? "this" : checkedProp, titleAndDesc.getValue());
                sortingProperty.setTitle(titleAndDesc.getKey());
    
                final Pair<Ordering, Integer> orderingAndNumber = getOrderingAndNumber(orderedProperties, checkedProp);
                if (orderingAndNumber != null) {
                    sortingProperty.setSorting(Ordering.ASCENDING == orderingAndNumber.getKey()); // 'null' is by default, means no sorting exist
                    sortingProperty.setSortingNumber(orderingAndNumber.getValue());
                }
                result.add(sortingProperty);
            }
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