package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.user.User;

import java.util.List;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;
import static ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType.*;

/// Companion object for entity [UserDefinedEnhancedEntityType].
///
public interface UserDefinedEnhancedEntityTypeCo extends IEntityDao<UserDefinedEnhancedEntityType>, ISaveWithFetch<UserDefinedEnhancedEntityType> {

    IFetchProvider<UserDefinedEnhancedEntityType> FETCH_PROVIDER = fetch(UserDefinedEnhancedEntityType.class).with(NAME, USER, BASE_TYPE);

    /// Returns the enhanced entity types of `user`, ordered by name.
    ///
    default List<UserDefinedEnhancedEntityType> findByUser(final User user) {
        final var query = select(UserDefinedEnhancedEntityType.class).where().prop(USER).eq().val(user).model();
        return getAllEntities(from(query).with(FETCH_PROVIDER.fetchModel()).with(orderBy().prop(NAME).asc().model()).model());
    }

}
