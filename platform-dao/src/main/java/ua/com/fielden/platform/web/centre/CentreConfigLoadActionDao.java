package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.AUTO_RUN;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inherited;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inheritedFromBase;

import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link CentreConfigLoadActionCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigLoadAction.class)
public class CentreConfigLoadActionDao extends CommonEntityDao<CentreConfigLoadAction> implements CentreConfigLoadActionCo {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    private static final String ERR_EXACTLY_ONE_CONFIGURATION_MUST_BE_SELECTED = "Please select configuration to load.";
    
    @Inject
    public CentreConfigLoadActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    // @SessionRequired -- avoid transaction here; see EntityCentreConfigDao for more details
    public CentreConfigLoadAction save(final CentreConfigLoadAction entity) {
        if (entity.isSkipUi()) {
            return super.save(entity);
        }
        entity.isValid().ifFailure(Result::throwRuntime);
        if (entity.getChosenIds().isEmpty()) {
            throw failure(ERR_EXACTLY_ONE_CONFIGURATION_MUST_BE_SELECTED);
        }
        
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
        
        // need to check whether configuration being loaded is inherited
        final String saveAsNameToLoad = entity.getChosenIds().iterator().next();
        final T2<Optional<String>, Boolean> actualSaveAsNameAndSharedIndicator = entity.getCentreConfigurations().stream()
            .filter(centreConfig -> saveAsNameToLoad.equals(centreConfig.getKey()))
            .findAny()
            .map(centreConfig -> {
                final Optional<LoadableCentreConfig> loadableConfig = findLoadableConfig(of(saveAsNameToLoad), selectionCrit); // this will also throw early failure in case where configuration was deleted
                if (inherited(loadableConfig).isPresent()) {
                    if (inheritedFromBase(loadableConfig).isPresent()) {
                        // if configuration being loaded is inherited from base we need to update it from upstream changes
                        selectionCrit.updateInheritedFromBaseCentre(saveAsNameToLoad);
                    } else {
                        // if configuration being loaded is inherited from shared we need to update it from upstream changes
                        return t2(selectionCrit.updateInheritedFromSharedCentre(saveAsNameToLoad, centreConfig.getConfig() != null ? centreConfig.getConfig().getConfigUuid() : null), true);
                    }
                }
                return t2(of(saveAsNameToLoad), false);
            })
            .orElse(t2(empty(), false));
        final Optional<String> actualSaveAsName = actualSaveAsNameAndSharedIndicator._1;
        final boolean isInheritedFromShared = actualSaveAsNameAndSharedIndicator._2;
        // configuration being loaded need to become preferred (except inherited from shared; link / default configs are not present in Load dialog -- no need to check this)
        if (!isInheritedFromShared) {
            selectionCrit.makePreferredConfig(actualSaveAsName); // 'own save-as / inherited from base' kinds -- can be preferred
        }
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> newSelectionCrit = selectionCrit.createCriteriaValidationPrototype(actualSaveAsName);
        final Map<String, Object> customObject = getCustomObject(selectionCrit, newSelectionCrit, actualSaveAsName, of(selectionCrit.centreConfigUuid(actualSaveAsName)), of(newSelectionCrit.getCentreDomainTreeMangerAndEnhancer().getPreferredView()), of(newSelectionCrit.shareError().get())); // TODO problems for 'was shared, removed by user' configs
        customObject.put(AUTO_RUN, selectionCrit.centreRunAutomatically(actualSaveAsName));
        entity.setCustomObject(customObject);
        return super.save(entity);
    }
    
}