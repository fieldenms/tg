package ua.com.fielden.platform.web.action.pre;

import static java.lang.String.format;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * This pre-action implementation should be used only with navigation or edit.
 *
 * @author TG Team
 *
 */
public class EntityNavigationPreAction implements IPreAction {

    private final String navigationType;

    /**
     * Creates pre-action for action that allows to navigate to another entity without closing dialog, such action can work only on EGI.
     *
     * @param navigationType - type description that is used to inform user what type of entity is currently opened and is navigating.
     */
    public EntityNavigationPreAction(final String navigationType) {
        this.navigationType = navigationType;
    }

    @Override
    public JsCode build() {
        return new JsCode(format("self.navigationPreAction(action, '%s');%n", navigationType));
    }

}
