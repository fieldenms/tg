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

    private final String navigationType;

    public SequentialOpenPreAction(final String navigationType) {
        this.navigationType = navigationType;
    }

    @Override
    public JsCode build() {
        return new JsCode(String.format("%n"
                + "if (!action.supportsNavigation) {%n"
                + "    action.supportsNavigation = true;%n"
                + "    action.navigationType = %s;%n"
                + "    action.firstEntry = fucntion() {%n"
                + "    }.bind(self);%n"
                + "    action.previousEntry = fucntion() {%n"
                + "    }.bind(self);%n"
                + "    action.nextEntry = fucntion() {%n"
                + "    }.bind(self);%n"
                + "    action.lastEntry = fucntion() {%n"
                + "    }.bind(self);%n"
                + "    action.hasPreviousEntry = fucntion() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        return entityIndex > 0;%n"
                + "    }.bind(self);%n"
                + "    action.hasNextEntry = fucntion() {%n"
                + "        const entityIndex = this.$.egi.findEntityIndex(action.currentEntity);%n"
                + "        return entityIndex >= 0 && entityIndex === this.$.egi.entities.length - 1;%n"
                + "    }.bind(self);%n"
                + "}%n", navigationType)
                + ""
                + ""
                + ""
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
