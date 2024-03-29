package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Common ancestor for {@link IPreAction} and {@link IPostAction}.
 *
 * @author TG Team
 */
public interface IAction {
    JsCode build();
}