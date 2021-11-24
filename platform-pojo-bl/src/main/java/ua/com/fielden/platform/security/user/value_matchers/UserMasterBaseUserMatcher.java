package ua.com.fielden.platform.security.user.value_matchers;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A value matcher to return only base users.
 *
 * @author TG Team
 *
 */
public class UserMasterBaseUserMatcher extends FallbackValueMatcherWithContext<User, User> {

    @Inject
    public UserMasterBaseUserMatcher(final IUser coUser, final IUniversalConstants constants) {
        super(coUser, true);
    }

    @Override
    protected ConditionModel makeSearchCriteriaModel(final User contextUser, final String searchString) {
        final ConditionModel originalSearchCriteria = super.makeSearchCriteriaModel(contextUser, searchString);
        final IStandAloneConditionCompoundCondition<AbstractEntity<?>> cond = cond().prop("base").eq().val(true).and().condition(originalSearchCriteria);
        return (contextUser.isPersisted() ? cond.and().prop("id").ne().val(contextUser) : cond) .model();
    }

}