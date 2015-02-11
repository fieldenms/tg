package ua.com.fielden.platform.web.master.api.actions.pre;

import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * A contract that should be implemented by all concrete implementations of pre-action behaviour for Entity Master actions.
 *
 * Pre-actions execute at the client side, and thus in case of a HTML application they should emit the valid HTML and JavaScript code during client code generation.
 *
 * @author TG Team
 *
 */
public interface IPreAction  {
    JsCode build();
}
