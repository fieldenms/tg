package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Pre-action implementation to support editing of polymorphic entities.
 *
 * @author TG Team
 *
 */
public class PolymorphicEditPreAction implements IPreAction {
    
    
    
    @Override
    public JsCode build() {
        return new JsCode(""
            + "// console.error('XXX preAction: action and self', action, self);\n"
            + "\n"
            + "const parentSelector = '[parent-element-alias=\"' + action.elementAlias + '\"]';\n"
            + "if (action.currentEntity.key === 'ST000001' || action.currentEntity.key === 'DC000001') {\n"
            + "    const concreteAction = self.querySelector('tg-ui-action' + parentSelector + '[child-name=\"" + "editStAndDcPOInSimpleMaster" + "\"]');\n"
            + "    concreteAction.currentEntity = action.currentEntity;\n"
            + "    concreteAction.chosenProperty = action.chosenProperty;\n"
            + "    concreteAction._run();\n"
            + "    return false;\n"
            + "} else if (action.currentEntity.dc === true) {\n"
            + "    return true;\n"
            + "} else if (action.currentEntity.st === true) {\n"
            + "    const concreteAction = self.querySelector('tg-ui-action' + parentSelector + '[child-name=\"" + "editStPO" + "\"]');\n"
            + "    concreteAction.currentEntity = action.currentEntity;\n"
            + "    concreteAction.chosenProperty = action.chosenProperty;\n"
            + "    concreteAction._run();\n"
            + "    return false;\n"
            + "}\n"
            + "\n"
            + "\n");
    }
    
}
