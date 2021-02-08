package ua.com.fielden.platform.web_api;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;

/**
 * Contract that is used for restricting queries of root GraphQL fields.
 * 
 * @author TG Team
 *
 */
@ImplementedBy(CanExecuteRootEntity.class)
public interface ICanExecuteRootEntity {
    
    /**
     * Returns {@link Result} indicating whether sub-query for root GraphQL field, represented by {@code rootEntityType}, can be executed for current user.
     * 
     * @param rootEntityType -- the entity type for root field being executed
     */
    <T extends AbstractEntity<?>> Result canExecute(final Class<T> rootEntityType);
    
}