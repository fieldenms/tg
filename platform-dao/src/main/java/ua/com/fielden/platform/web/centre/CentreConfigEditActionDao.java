package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigEditAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigEditAction.class)
public class CentreConfigEditActionDao extends CommonEntityDao<CentreConfigEditAction> implements ICentreConfigEditAction {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    private static final String CONTINUATION_KEY = "overrideConfig";
    
    @Inject
    public CentreConfigEditActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigEditAction save(final CentreConfigEditAction entity) {
        // validate centre configuration edit / copy action before performing actual edit / copy
        entity.isValid().ifFailure(Result::throwRuntime);
        
        if (entity.hasWarnings() && !moreData(CONTINUATION_KEY).isPresent()) {
            throw new NeedMoreData("Override configuration?", OverrideCentreConfig.class, CONTINUATION_KEY);
        }
        
        // make optional 'preferred' value only in case where 'preferred' property has changed, otherwise there is no need to make preferred configuration adjustments in persistent storage
        final Optional<Boolean> dirtyPreferredValue = entity.getProperty("preferred").isDirty() ? of(entity.isPreferred()) : empty();
        // perform actual copy using centreEditor() closure
        criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder())
            .centreEditor().accept(t3(valueOf(entity.getEditKind()), of(entity.getTitle()), dirtyPreferredValue), entity.getDesc());
        return super.save(entity);
    }
    
}