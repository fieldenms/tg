package ua.com.fielden.platform.web.centre.api.selection_crit_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

public interface ISelectionCriteriaActions<T extends AbstractEntity<?>> extends IEnhanceEntityWithCalcProps<T> {

    IAlsoSelectionCriteriaActions<T> addSelectionCriteriaAction(final EntityActionConfig actionConfig);
}
