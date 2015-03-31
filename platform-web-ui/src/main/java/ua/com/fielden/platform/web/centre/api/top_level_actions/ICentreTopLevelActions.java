package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

/**
 * A contract to specify top level actions for an entity centre.
 * Top level actions have generic nature and may be applicable to one or more selection entities.
 * They may have even more generic nature such as <i>update status for all entities that match current selection criteria</i>, which is not limited
 * only to the presented on the current page entities.
 *
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreTopLevelActions<T extends AbstractEntity<?>> extends IEnhanceEntityWithCalcProps<T> {
    IAsloCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig);
    ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc);
}
