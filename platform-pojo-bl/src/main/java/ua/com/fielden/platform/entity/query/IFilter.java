package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import com.google.inject.ImplementedBy;

/**
 * A contract to be implemented for the purpose of data filtering.
 * <p>
 * The original intension was to enhance an {@link IQueryOrderedModel} instance with user-driven filtering.
 *
 * @author TG Team
 *
 */
@ImplementedBy(DefaultFilter.class)
public interface IFilter {

    <T extends AbstractEntity> EntityResultQueryModel<T> enhance(Class<T> entityType, final String username);
}
