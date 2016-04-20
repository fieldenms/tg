package ua.com.fielden.platform.web.centre;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.AbstractFunctionalEntityForCollectionModificationProducer;
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
public class CentreConfigUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<EnhancedCentreEntityQueryCriteria, CentreConfigUpdater> implements IEntityProducer<CentreConfigUpdater> {
    // private final IUserRoleDao coUserRole;
    // private final SecurityTokenProvider securityTokenProvider;

    @Inject
    public CentreConfigUpdaterProducer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,
            /* final IUserRoleDao coUserRole, */
            final IApplicationSettings applicationSettings) throws Exception {
        super(factory, CentreConfigUpdater.class, companionFinder);
        // this.coUserRole = coUserRole;
        // this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
    }

    @Override
    // @Authorise(UserRoleReviewToken.class)
    protected CentreConfigUpdater provideCurrentlyAssociatedValues(final CentreConfigUpdater entity, final EnhancedCentreEntityQueryCriteria masterEntity) {
        final LinkedHashSet<SortingProperty> sortingProperties = createSortingProperties((ICentreDomainTreeManagerAndEnhancer) masterEntity.getFreshCentre.get(), masterEntity.getEntityClass(), masterEntity.getManagedType(), factory());
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
        int sortingNumber = 0;
        for (final String checkedProp: checkedProperties) {
            if (!AbstractDomainTreeRepresentation.isCalculatedAndOfTypes(managedType, checkedProp, CalculatedPropertyCategory.AGGREGATED_EXPRESSION) && 
                    !AnnotationReflector.isPropertyAnnotationPresent(CustomProp.class, managedType, checkedProp)) {
                final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc(checkedProp, managedType);
                final SortingProperty sortingProperty = factory.newEntity(SortingProperty.class, null, checkedProp, titleAndDesc.getValue());
                sortingProperty.setTitle(titleAndDesc.getKey());
    
                final Ordering ordering = getOrdering(orderedProperties, checkedProp);
                if (ordering != null) {
                    sortingProperty.setSorting(Ordering.ASCENDING == ordering); // 'null' is by default, means no sorting exist
                    sortingProperty.setSortingNumber(sortingNumber);
                    sortingNumber++;
                }
                result.add(sortingProperty);
            }
        }
        return result;
    }

    private Ordering getOrdering(final List<Pair<String, Ordering>> orderedProperties, final String prop) {
        for (final Pair<String, Ordering> orderedProperty : orderedProperties) {
            if (orderedProperty.getKey().equals(prop)) {
                return orderedProperty.getValue();
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
    protected EnhancedCentreEntityQueryCriteria refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext) {
        return (EnhancedCentreEntityQueryCriteria) masterEntityFromContext;
    }
}