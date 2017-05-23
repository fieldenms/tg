package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionBuilder7<T extends AbstractEntity<?>> extends IEntityActionBuilder7a<T> {
	/** Sets preferred dimensions for a view associated with the action. */
    IEntityActionBuilder7a<T> shortcut(final String shortcut);
}
