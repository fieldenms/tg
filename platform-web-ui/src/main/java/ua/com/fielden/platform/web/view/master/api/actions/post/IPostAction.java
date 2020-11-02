package ua.com.fielden.platform.web.view.master.api.actions.post;

import ua.com.fielden.platform.web.view.master.api.actions.IAction;

/**
 * A contract that should be implemented by all concrete implementations of post-action behaviour for Entity Master actions.
 *
 * Post-actions execute at the client side, and thus in case of a HTML application they should emit the valid HTML and JavaScript code during client code generation.
 *
 * @author TG Team
 *
 */
public interface IPostAction extends IAction {
}