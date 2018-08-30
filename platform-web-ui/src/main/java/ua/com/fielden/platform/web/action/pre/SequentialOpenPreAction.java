package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * This pre-action implementation should be used only with sequential edit action.
 *
 * @author TG Team
 *
 */
public class SequentialOpenPreAction implements IPreAction {

    @Override
    public JsCode build() {
        return new JsCode("\n"
                + "if (!action.modifyFunctionalEntity) {\n"
                + "    action.modifyFunctionalEntity = function (bindingEntity) {\n"
                + "        if (!action.entitiesToEdit && this.$.egi.entities.length > 0) {\n"
                + "            action.entitiesToEdit = this.$.egi.entities.map(entity => entity.get('id'));\n"
                + "        }\n"
                + "        bindingEntity['entityId'] = action.entitiesToEdit[0] + '';\n"
                + "        action.entitiesToEdit.shift();\n"
                + "    }.bind(self);\n"
                + "}\n"
                );
    }

}
