package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.AUTO_RUN;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inherited;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inheritedFromBase;

import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
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
    @SessionRequired
    public CentreConfigLoadAction save(final CentreConfigLoadAction entity) {
        entity.isValid().ifFailure(Result::throwRuntime);
        if (entity.getChosenIds().isEmpty()) {
            throw failure(ERR_EXACTLY_ONE_CONFIGURATION_MUST_BE_SELECTED);
        }
        
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
        
        // need to check whether configuration being loaded is inherited
        final String saveAsNameToLoad = entity.getChosenIds().iterator().next();
        final Optional<String> actualSaveAsName = entity.getCentreConfigurations().stream()
            .filter(centreConfig -> saveAsNameToLoad.equals(centreConfig.getKey()))
            .findAny()
            .flatMap(centreConfig -> {
                final Optional<LoadableCentreConfig> loadableConfig = findLoadableConfig(of(saveAsNameToLoad), selectionCrit); // this will also throw early failure in case where configuration was deleted
                if (inherited(loadableConfig).isPresent()) {
                    if (inheritedFromBase(loadableConfig).isPresent()) {
                        // if configuration being loaded is inherited from base we need to update it from upstream changes
                        selectionCrit.updateInheritedFromBaseCentre(saveAsNameToLoad);
                    } else {
                        // if configuration being loaded is inherited from shared we need to update it from upstream changes
                        return selectionCrit.updateInheritedFromSharedCentre(saveAsNameToLoad, centreConfig.getConfig().getConfigUuid());
                    }
                }
                return of(saveAsNameToLoad);
            });
        // configuration being loaded need to become preferred
        selectionCrit.makePreferredConfig(actualSaveAsName);
        final Map<String, Object> customObject = getCustomObject(selectionCrit, selectionCrit.createCriteriaValidationPrototype(actualSaveAsName), actualSaveAsName, of(selectionCrit.centreConfigUuid(actualSaveAsName)));
        customObject.put(AUTO_RUN, selectionCrit.centreRunAutomatically(actualSaveAsName));
        entity.setCustomObject(customObject);
        return super.save(entity);
    }
    
}