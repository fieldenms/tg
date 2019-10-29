package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * An entity representing <code>navigation with edit</code> action.
 *
 * @author TG Team
 *
 */
@KeyTitle("Entity Navigation")
@CompanionObject(IEntityNavigationAction.class)
public class EntityNavigationAction extends EntityEditAction {
}
