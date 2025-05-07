package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/// A standard "entity navigation" [IPreAction] that allows navigation to previous / next / first / last entity.
/// It is only applicable on Entity Centre `Edit` actions.
///
/// @author TG Team
public class EntityNavigationPreAction implements IPreAction {
    protected final String navigationType;

    protected EntityNavigationPreAction(final String navigationType) {
        this.navigationType = navigationType;
    }

    @Override
    public JsCode build() {
        return jsCode("""
            self.navigationPreAction(action, '%s');
        """.formatted(
            navigationType
        ));
    }

}
