package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdater.class)
public class CentreConfigUpdaterDao extends CommonEntityDao<CentreConfigUpdater> implements ICentreConfigUpdater {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigUpdaterDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigUpdater save(final CentreConfigUpdater action) {
        final T2<CentreConfigUpdater, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>> actionAndCriteriaBeingUpdated = validateAction(action, this, String.class, new CentreConfigUpdaterController(criteriaEntityRestorer));
        final CentreConfigUpdater actionToSave = actionAndCriteriaBeingUpdated._1;
        actionToSave.getProperty("sortingVals").setOriginalValue(action.getProperty("sortingVals").getOriginalValue());
        
        // retrieve criteria entity
        final EnhancedCentreEntityQueryCriteria criteriaEntityBeingUpdated = actionAndCriteriaBeingUpdated._2;
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
        final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster = criteriaEntityBeingUpdated.centreAdjuster();
        
        // use centreAdjuster to update all centre managers ('fresh', 'saved' and 'previouslyRun') with columns visibility / order / sorting information; also commit them to the database
        centreAdjuster.accept(centreManager -> {
            // remove sorting information
            final List<Pair<String, Ordering>> currOrderedProperties = new ArrayList<>(centreManager.getSecondTick().orderedProperties(root));
            for (final Pair<String, Ordering> orderedProperty: currOrderedProperties) {
                if (Ordering.ASCENDING == orderedProperty.getValue()) {
                    centreManager.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
                }
                centreManager.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
            }
            
            // remove usage information
            final List<String> currUsedProperties = centreManager.getSecondTick().usedProperties(root);
            for (final String currUsedProperty: currUsedProperties) {
                centreManager.getSecondTick().use(root, currUsedProperty, false);
            }
            
            // apply usage information
            for (final String chosenId : action.getChosenIds()) {
                centreManager.getSecondTick().use(root, treeName(chosenId), true);
            }
            
            // apply sorting information
            for (final String sortingVal: action.getSortingVals()) {
                final String[] splitted = sortingVal.split(":");
                final String name = treeName(splitted[0]);
                centreManager.getSecondTick().toggleOrdering(root, name);
                if ("desc".equals(splitted[1])) {
                    centreManager.getSecondTick().toggleOrdering(root, name);
                }
            }
        });
        actionToSave.setSortingChanged(actionToSave.getProperty("sortingVals").isChangedFromOriginal());
        return super.save(actionToSave);
    }
}
