package ua.com.fielden.platform.web.action.pre;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.ArrayList;
import java.util.List;

import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/**
 * A standard confirmation pre-action.
 *
 * @author TG Team
 *
 */
public class ConfirmationPreAction implements IPreAction {

    private final String message;
    private final List<String> buttons = new ArrayList<>();

    enum ConfirmationButtons {

        YES("{name:'Yes', confirm:true, autofocus:true}"),
        NO("{name:'No'}"),
        OK("{name:'Ok', confirm:true, autofocus:true}"),
        CANCEL("{name:'Cancel'}");


        private final String code;
        private ConfirmationButtons(final String code) {
            this.code = code;
        }
    }

    ConfirmationPreAction(final String message, final ConfirmationButtons... buttons) {
        this.message = message;
        for (ConfirmationButtons button : buttons) {
            this.buttons.add(button.code);
        }
    }

    @Override
    public JsCode build() {
        return jsCode("""
            return self.confirm('%s', [%s])
        """.formatted(
            message,
            StringUtils.join(buttons, ",")
        ));
    }

}
