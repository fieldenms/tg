package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to provide fluent joining of selection criteria definitions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlsoCrit<T extends AbstractEntity<?>> extends IRunConfig<T> {
    ISelectionCriteriaBuilder<T> also();
}