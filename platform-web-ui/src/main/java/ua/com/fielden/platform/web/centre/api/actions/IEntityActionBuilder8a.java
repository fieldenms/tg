package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;

public interface IEntityActionBuilder8a<T extends AbstractEntity<?>> extends IEntityActionBuilder9<T> {
	/** If used then no auto-refresh of the parent centre's insertion points would occur upon entity save. */
	IEntityActionBuilder9<T> withNoInsertionPointsRefresh(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> firstInsertionPoint, final Class<? extends AbstractFunctionalEntityWithCentreContext<?>>... otherInsertionPoints);
}
