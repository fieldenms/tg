package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Default action selector for single action. It always return 0.
 *
 * @author TG Team
 *
 */
public class SingleActionSelector implements IEntityMultiActionSelector {
    public static final SingleActionSelector INSTANCE = new SingleActionSelector();
    
    private SingleActionSelector() {}
    
    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        return 0;
    }
    
}