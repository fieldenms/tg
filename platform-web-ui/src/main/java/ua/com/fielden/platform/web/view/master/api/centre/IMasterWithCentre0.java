package ua.com.fielden.platform.web.view.master.api.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.view.master.api.helpers.IComplete;

/**
 * A contract for providing an entity centre used by Master with Centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMasterWithCentre0<T extends AbstractEntity<?>> {
    IComplete<T> withCentre(final EntityCentre<?> entityCentre);
}
