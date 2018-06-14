package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

public interface IAlsoFrontActions<T extends AbstractEntity<?>> extends IEnhanceEntityWithCalcProps<T>{

    IFrontActions<T> also();
}
