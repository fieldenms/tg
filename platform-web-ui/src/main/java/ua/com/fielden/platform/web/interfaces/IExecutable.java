package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * A contract for any kind of Java web UI component that can have its JavaScript code.
 *
 * @author TG Team
 *
 */
public interface IExecutable {

    /**
     * Returns the JavaScript code that represents this Java web UI component.
     *
     * @return
     */
    JsCode code();
}
