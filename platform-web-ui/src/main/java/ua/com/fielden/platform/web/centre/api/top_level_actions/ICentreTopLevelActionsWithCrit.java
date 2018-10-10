package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

public interface ICentreTopLevelActionsWithCrit<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T>, IEnhanceEntityWithCalcProps<T> {

}
