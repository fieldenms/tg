package ua.com.fielden.platform.security.user.value_matchers;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
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
    protected ConditionModel makeSearchCriteriaModel(final User context, final String searchString) {
        final ConditionModel originalSearchCriteria = super.makeSearchCriteriaModel(context, searchString);
        return cond().condition(originalSearchCriteria).and().prop("base").eq().val(true).model();
    }

}