package ua.com.fielden.platform.web.action.pre;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.SequentialEntityEditAction;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * This pre-action implementation should be used only with sequential edit action.
 *
 * @author TG Team
 *
 */
public class SequentialOpenPostAction implements IPostAction {

    @Override
    public JsCode build() {
        return new JsCode(format("%n"
                + "if (functionalEntity.type().fullClassName() !== '%s') { %n"
                + "    master.retrieve().then(function(ironRequest) {%n"
                + "        action.modifyFunctionalEntity(master._currBindingEntity, master, action);%n"
                + "        return master.save();%n"
                + "    });%n"
                + "}%n", SequentialEntityEditAction.class.getName())
                );
    }

}
