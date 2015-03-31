package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to complete construction of an entity centre configuration, which starts in {@link IEntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEcbCompletion<T extends AbstractEntity<?>> {
    EntityCentreConfig build();
}
