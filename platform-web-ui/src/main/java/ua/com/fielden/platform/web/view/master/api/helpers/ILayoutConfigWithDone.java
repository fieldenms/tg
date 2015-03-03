package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;


/**
 * A contract for specifying layouting of UI widgets that supports the completion contract.
 *
 * @author TG Team
 *
 */
public interface ILayoutConfigWithDone<T extends AbstractEntity<?>> extends ILayoutConfig<T>, IComplete<T> {

}
