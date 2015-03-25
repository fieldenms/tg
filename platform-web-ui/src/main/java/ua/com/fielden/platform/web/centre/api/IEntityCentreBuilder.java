package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.calc.IEnhanceEntityWithCalcProps;

/**
 * This contract is an entry point for an Entity Centre aPI -- an embedded domain specific language for constructing entity centres.
 *
 * @see <a href="https://github.com/fieldenms/tg/issues/140">Specification</a>
 *
 * @author TG Team
 *
 */
public interface IEntityCentreBuilder<T extends AbstractEntity<?>> {

    /**
     * Entity centre construction DSL entry point.
     *
     * @param type
     * @return
     */
    IEnhanceEntityWithCalcProps<T> forEntity(Class<T> type);


}
