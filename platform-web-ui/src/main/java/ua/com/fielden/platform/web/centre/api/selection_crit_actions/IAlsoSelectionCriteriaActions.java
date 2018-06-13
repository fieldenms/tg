package ua.com.fielden.platform.web.centre.api.selection_crit_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

public interface IAlsoSelectionCriteriaActions<T extends AbstractEntity<?>> extends IEnhanceEntityWithCalcProps<T>{

    ISelectionCriteriaActions<T> also();
}
