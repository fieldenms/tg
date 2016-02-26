package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.PrefDim;

public interface IEntityActionBuilder7<T extends AbstractEntity<?>> extends IEntityActionBuilder8<T> {
	/** Sets preferred dimensions for a view associated with the action. */
	IEntityActionBuilder8<T> prefDimForView(final PrefDim dim);
}
