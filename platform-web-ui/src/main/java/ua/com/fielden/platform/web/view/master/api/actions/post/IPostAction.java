package ua.com.fielden.platform.web.view.master.api.actions.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.IComposableAction;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/// A contract that should be implemented by all concrete implementations of post-action behaviour for Entity Master / Centre actions.
/// Post-actions execute at the client side.
/// They should emit the valid JavaScript code during client code generation.
///
/// @author TG Team
public interface IPostAction extends IComposableAction<IPostAction> {

    @Override
    default IPostAction andThen(final IPostAction thatAction) {
        return new IPostAction() {
            @Override
            public Set<JsImport> importStatements() {
                return Stream.concat(
                    IPostAction.this.importStatements().stream(),
                    thatAction.importStatements().stream()
                ).collect(toUnmodifiableSet());
            }

            @Override
            public JsCode build() {
                return jsCode(
                    IPostAction.this.build().toString()
                    + thatAction.build().toString()
                );
            }
        };
    }

}