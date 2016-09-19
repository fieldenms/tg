package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.PrefDim;

/**
 * A contract for specifying dimensions for entity master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IDimensions<T extends AbstractEntity<?>> {

    /**
     * Set the dimensions for entity master.
     *
     * @param prefDim
     * @return
     */
    IComplete<T> withDimensions(PrefDim prefDim);
}
