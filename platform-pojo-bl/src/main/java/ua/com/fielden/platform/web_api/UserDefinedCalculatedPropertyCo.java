package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.List;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;
import static ua.com.fielden.platform.web_api.UserDefinedCalculatedProperty.*;

/// Companion object for entity [UserDefinedCalculatedProperty].
///
public interface UserDefinedCalculatedPropertyCo extends IEntityDao<UserDefinedCalculatedProperty>, ISaveWithFetch<UserDefinedCalculatedProperty> {

    IFetchProvider<UserDefinedCalculatedProperty> FETCH_PROVIDER = fetch(UserDefinedCalculatedProperty.class)
            .with(ENTITY_TYPE, ENTITY_TYPE + "." + UserDefinedEnhancedEntityType.BASE_TYPE, NAME, EXPRESSION);

    /// Returns the calculated properties of `entityType`, ordered by name.
    ///
    default List<UserDefinedCalculatedProperty> findByEntityType(final UserDefinedEnhancedEntityType entityTypeInstance) {
        if (!entityTypeInstance.isPersisted()) {
            throw new InvalidArgumentException("[entityTypeInstance] must be a persisted instance.");
        }
        final var query = select(UserDefinedCalculatedProperty.class).where().prop(ENTITY_TYPE).eq().val(entityTypeInstance).model();
        return getAllEntities(from(query).with(FETCH_PROVIDER.fetchModel()).with(orderBy().prop(NAME).asc().model()).model());
    }

}
