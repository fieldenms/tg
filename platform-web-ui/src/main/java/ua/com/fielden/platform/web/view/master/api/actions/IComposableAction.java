package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.Set;

import static java.util.Set.of;

/// An abstraction for a composable piece of JavaScript code through [#build()] method with JS import statements support.
/// May be used as a lambda `() -> jsCode("...")` function for convenience.
///
/// Common ancestor for [IPreAction] and [IPostAction].
///
/// @author TG Team
public interface IComposableAction<ACTION extends IComposableAction> extends IAction {
    /// Warning for [#build()] method of a typical [JsCode] concatenation usage.\
    /// May be dangerous for [IComposableAction]s with defined [#importStatements()].
    String WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS = "2.1.0. Don't use this for JsCode concatenation; use andThen(...) instead.";

    /// A set of [JsImport]s, required for this JavaScript action.
    default Set<JsImport> importStatements() {
        return of();
    }

    /// Composes this [IComposableAction] (`thisAction`) with `thatAction` to be performed in `thisAction => thatAction` order.
    ACTION andThen(final ACTION thatAction);

}