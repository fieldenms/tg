package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;

/**
 * This contract is an entry point for Simple Master API -- an embedded domain specific language for constructing simple entity masters.
 *
 * @see <a href="https://github.com/fieldenms/tg/wiki/Web-UI-Design:-Entity-Masters">Entity Masters Wiki</a>
 *
 * @author TG Team
 *
 */
public interface ISimpleMasterBuilder<T extends AbstractEntity<?>> {

    /**
     * Simple Master construction DSL entry point.
     *
     * @param type
     * @return
     */
    IPropertySelector<T> forEntity(Class<T> type);

}
