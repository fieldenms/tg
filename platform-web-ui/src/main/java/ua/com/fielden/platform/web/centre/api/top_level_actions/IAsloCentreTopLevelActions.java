package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

public interface IAsloCentreTopLevelActions<T extends AbstractEntity<?>> extends IEnhanceEntityWithCalcProps<T> {
    ICentreTopLevelActions<T> also();
}
