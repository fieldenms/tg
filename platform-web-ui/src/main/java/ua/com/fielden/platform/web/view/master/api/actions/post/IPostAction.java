package ua.com.fielden.platform.web.view.master.api.actions.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.IAction;

import java.util.Set;

import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/**
 * A contract that should be implemented by all concrete implementations of post-action behaviour for Entity Master actions.
 *
 * Post-actions execute at the client side, and thus in case of a HTML application they should emit the valid HTML and JavaScript code during client code generation.
 *
 * @author TG Team
 *
 */
public interface IPostAction extends IAction {

    default IPostAction andThen(final JsCode code) {
        return new IPostAction() {
            @Override
            public Set<JsImport> importStatements() {
                return IPostAction.this.importStatements();
            }

            @Override
            public JsCode build() {
                return jsCode(IPostAction.this.build() + code.toString());
            }
        };
    }

}