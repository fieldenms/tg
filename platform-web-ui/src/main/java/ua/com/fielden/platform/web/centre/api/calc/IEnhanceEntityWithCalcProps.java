package ua.com.fielden.platform.web.centre.api.calc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaConfig;

/**
 *
 * Provides a convenient abstraction for enhancing the underlying entity type for Entity Centre with calculated properties.
 * <p>
 * It extends {@link ISelectionCriteriaConfig} to cater for skipping addition of calculated properties and continuing directly with a set up of selection criteria.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEnhanceEntityWithCalcProps<T extends AbstractEntity<?>> extends ISelectionCriteriaConfig<T> {

}
