package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.DefaultFilter;

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

    <T extends AbstractEntity> IQueryModel<T> enhance(Class<T> entityType, final String username);
}
