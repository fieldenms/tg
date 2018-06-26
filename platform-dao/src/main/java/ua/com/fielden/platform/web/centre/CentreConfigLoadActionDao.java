package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigLoadAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigLoadAction.class)
public class CentreConfigLoadActionDao extends CommonEntityDao<CentreConfigLoadAction> implements ICentreConfigLoadAction {
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
        // need to check whether configuration being loaded is inherited
        final String saveAsNameToLoad = entity.getChosenIds().iterator().next();
        entity.getCentreConfigurations().stream()
            .filter(centreConfig -> saveAsNameToLoad.equals(centreConfig.getKey()))
            .findAny()
            .ifPresent(centreConfig -> {
                if (centreConfig.isInherited()) {
                    // if configuration being loaded is inherited we need to update it from base user changes
                    criteriaEntityRestorer
                        .restoreCriteriaEntity(entity.getCentreContextHolder())
                        .inheritedCentreUpdater().accept(saveAsNameToLoad);
                }
            });
        
        return super.save(entity);
    }
    
}