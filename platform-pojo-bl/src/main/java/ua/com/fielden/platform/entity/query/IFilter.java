package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import com.google.inject.ImplementedBy;

/**
 * A contract to be implemented for the purpose of data filtering.
 * <p>
 * The original intension was to enhance an {@link EntityResultQueryModel} instance with user-driven filtering.
 * 
 * @author TG Team
 * 
 */
@ImplementedBy(DefaultFilter.class)
public interface IFilter {

    <ET extends AbstractEntity<?>> ConditionModel enhance(Class<ET> entityType, String typeAlias, final String username);
}
