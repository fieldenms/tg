package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.UserDefinableHelp_CanSave_Token;

/**
 * DAO implementation for companion object {@link UserDefinableHelpCo}.
 *
 * @author TG Team
 *
 */
@EntityType(UserDefinableHelp.class)
public class UserDefinableHelpDao extends CommonEntityDao<UserDefinableHelp> implements UserDefinableHelpCo {

    @Inject
    public UserDefinableHelpDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(UserDefinableHelp_CanSave_Token.class)
    public UserDefinableHelp save(final UserDefinableHelp entity) {
        if (!entity.isSkipUi()) {
            return super.save(entity);
        }
        return entity;
    }

    @Override
    protected IFetchProvider<UserDefinableHelp> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}