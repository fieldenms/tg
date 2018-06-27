package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;

import com.google.inject.Inject;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
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
        if (!entity.isSkipUi()) {
            // validate centre configuration edit / copy action before performing actual edit / copy
            entity.isValid().ifFailure(Result::throwRuntime);
            
            if (entity.hasWarnings() && !moreData(CONTINUATION_KEY).isPresent()) {
                throw new NeedMoreData("Override configuration?", OverrideCentreConfig.class, CONTINUATION_KEY);
            }
            
            final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
            
            // TODO in case of successful save we need to return new customObject with relevant criteriaEntity (new title!) 
            // TODO perhaps turn on centre refreshing for other actions except SAVE2?
            
            // perform actual copy / edit using centreEditor() closure
            selectionCrit.centreEditor().accept(t2(valueOf(entity.getEditKind()), of(entity.getTitle())), entity.getDesc());
        }
        return super.save(entity);
    }
    
}