package ua.com.fielden.platform.web_api;

import static ua.com.fielden.platform.error.Result.successful;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.tokens.web_api.WebAPI_CanExecute_Token;

/**
 * Default implementation allowing execution of all [supported by GraphQL schema] types.<br>
 * There is also token for all GraphQL Web API restricting all operations -- {@link WebAPI_CanExecute_Token}.
 * 
 * @see WebAPI_CanExecute_Token
 * 
 * @author TG Team
 *
 */
public class CanExecuteRootEntity implements ICanExecuteRootEntity {
    
    @Override
    public <T extends AbstractEntity<?>> Result canExecute(final Class<T> rootEntityType) {
        return successful(rootEntityType);
    }
    
}