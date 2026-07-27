package ua.com.fielden.platform.entity.query;

import com.google.inject.ImplementedBy;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * A contract to be implemented for the purpose of data filtering.
 * <p>
 * The original intension was to enhance an {@link EntityResultQueryModel} instance with user-driven filtering.
 * 
 * @author TG Team
 * 
 */
@ImplementedBy(NoDataFilter.class)
public interface IFilter {

    <ET extends AbstractEntity<?>> ConditionModel enhance(Class<ET> entityType, String typeAlias, final @Nullable String username);

}
