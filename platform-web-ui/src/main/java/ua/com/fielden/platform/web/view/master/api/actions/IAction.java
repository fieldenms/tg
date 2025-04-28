package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.web.minijs.JsCode;

/// An abstraction for a piece of JavaScript code through [#build()] method.
/// May be used as a lambda `() -> jsCode("...")` function for convenience.
///
/// @author TG Team
public interface IAction {

    /// Builds actual [JsCode] for this JavaScript action.
    JsCode build();

}