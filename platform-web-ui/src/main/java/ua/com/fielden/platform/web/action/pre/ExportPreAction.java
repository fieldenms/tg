package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

public class ExportPreAction implements IPreAction {

    @Override
    public JsCode build() {
        return new JsCode(""
                + "    action.modifyFunctionalEntity = (function (bindingEntity, master) {\n"
                + "        action.modifyValue4Property('pageCapacity', bindingEntity, self.pageCapacity);\n"
                + "        action.modifyValue4Property('pageCount', bindingEntity, self.pageCountUpdated || 0);\n"
                + "        master.setEditorValue4PropertyFromConcreteValue('fromPage', (self.pageNumberUpdated && self.pageNumberUpdated + 1) || 1);\n"
                + "        master.setEditorValue4PropertyFromConcreteValue('toPage', self.pageCountUpdated || 1);\n"
                + "    });\n"
                + "    return true;\n");
    }

}
