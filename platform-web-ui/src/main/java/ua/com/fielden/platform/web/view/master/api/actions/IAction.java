package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.Set;

import static java.util.Set.of;

/**
 * Common ancestor for {@link IPreAction} and {@link IPostAction}.
 *
 * @author TG Team
 */
public interface IAction {
    /// Warning for [#build()] method of a typical [JsCode] concatenation usage.\
    /// May be dangerous for [IAction]s with defined [#importStatements()].
    String WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS = "2.1.0. Don't use this for JsCode concatenation; use andThen(...) instead.";

    /// Builds actual [JsCode] for this JavaScript action.
    JsCode build();

    default Set<JsImport> importStatements() {
        return of();
    };

}