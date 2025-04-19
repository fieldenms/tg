package ua.com.fielden.platform.web.view.master.api.actions.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.IAction;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/**
 * A contract that should be implemented by all concrete implementations of pre-action behaviour for Entity Master actions.
 *
 * Pre-actions execute at the client side, and thus in case of a HTML application they should emit the valid HTML and JavaScript code during client code generation.
 *
 * @author TG Team
 *
 */
public interface IPreAction extends IAction<IPreAction> {

    @Override
    default IPreAction andThen(final IPreAction thatAction) {
        return new IPreAction() {
            @Override
            public Set<JsImport> importStatements() {
                return Stream.concat(
                    IPreAction.this.importStatements().stream(),
                    thatAction.importStatements().stream()
                ).collect(toUnmodifiableSet());
            }

            @Override
            public JsCode build() {
                return jsCode(
                    IPreAction.this.build().toString()
                    + thatAction.build().toString()
                );
            }
        };
    }

}