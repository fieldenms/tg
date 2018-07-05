package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;

import java.util.HashMap;

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
        if (!entity.isSkipUi()) {
            // validate centre configuration action entity before performing actual edit / saveAs
            entity.isValid().ifFailure(Result::throwRuntime);
            
            if (entity.hasWarnings() && !moreData(CONTINUATION_KEY).isPresent()) { // confirm overriding behaviour for owned configuration; the only warning could be in 'title' property as per CentreConfigEditActionTitleValidator
                throw new NeedMoreData("Override configuration?", OverrideCentreConfig.class, CONTINUATION_KEY);
            }
            
            // perform actual edit / saveAs using centreEditor() closure
            entity.setCustomObject(
                criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder())
                .centreEditor().apply(
                    t2(valueOf(entity.getEditKind()), of(entity.getTitle())),
                    entity.getDesc()
                )
            );
        } else {
            entity.setCustomObject(new HashMap<>()); // clear custom object not to bind centre information second time (first time -- after retrieval, see client-side _bindCentreInfo method)
        }
        return entity;
    }
    
}