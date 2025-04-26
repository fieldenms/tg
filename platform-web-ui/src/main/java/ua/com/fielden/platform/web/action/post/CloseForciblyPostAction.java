package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/// A standard [IPostAction] that closes parent Entity Master forcibly.
///
/// @author TG Team
public class CloseForciblyPostAction implements IPostAction {

    CloseForciblyPostAction() {}

    @Override
    public JsCode build() {
        return jsCode("""
            self.publishCloseForcibly();
        """);
    }

}
