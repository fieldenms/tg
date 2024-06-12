package ua.com.fielden.platform.web.centre;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * Abstract DAO implementation for companion objects of {@link AbstractCentreConfigCommitAction} descendants.
 * 
 * @author TG Team
 *
 */
public abstract class AbstractCentreConfigCommitActionDao<T extends AbstractCentreConfigCommitAction> extends CommonEntityDao<T> {
    private static final String CONTINUATION_KEY = "overrideConfig";
    protected final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    public AbstractCentreConfigCommitActionDao(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    // @SessionRequired -- avoid transaction here; see EntityCentreConfigDao for more details
    public final T save(final T entity) {
        if (!entity.isSkipUi()) {
            // validate centre configuration action entity before performing actual edit / saveAs
            entity.isValid().ifFailure(Result::throwRuntime);
            
            if (entity.hasWarnings() && !moreData(CONTINUATION_KEY).isPresent()) { // confirm overriding behaviour for owned configuration; the only warning could be in 'title' property as per CentreConfigEditActionTitleValidator
                throw new NeedMoreData("Override configuration?", OverrideCentreConfig.class, CONTINUATION_KEY);
            }
            
            // perform actual action
            entity.setCustomObject(performSave(entity));
        } else {
            entity.setCustomObject(new HashMap<>()); // clear custom object not to bind centre information second time (first time -- after retrieval, see client-side _bindCentreInfo method)
        }
        return entity;
    }
    
    /**
     * Performs actual essence of this action. All validations must be successful at this stage.
     * 
     * @param entity
     * @return
     */
    protected abstract Map<String, Object> performSave(final T entity);
    
    @Override
    protected IFetchProvider<T> createFetchProvider() {
        return super.createFetchProvider().with("dashboardRefreshFrequency"); // this property is needed for autocompletion only; other props are simple and not required for functional entity's fetch provider
    }
    
    @Override
    public T new_() {
        final T entity = super.new_();
        entity.getProperty("dashboardRefreshFrequency").setEditable(false);
        return entity;
    }
    
}
