package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

public class ExportPreAction implements IPreAction {

    @Override
    public JsCode build() {
        return new JsCode(""
                + "    action.modifyFunctionalEntity = (function (bindingEntity) {\n"
                + "        action.modifyValue4Property('pagesFrom', bindingEntity, 'IMPORT');\n"
                + "    });\n"
                + "    return true;\n");
    }

}
