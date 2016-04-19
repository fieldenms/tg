package ua.com.fielden.platform.web.action.pre;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * A standard confirmation pre-action.
 * 
 * @author TG Team
 *
 */
public class ConfirmationPreAction implements IPreAction {

    private final String message;
    private final List<String> buttons = new ArrayList<>();

    private enum ConfirmationButtons {

        YES("{name:'Yes', confirm:true}"),
        NO("{name:'No', confirm:false}"),
        OK("{name:'Ok', confirm:true}"),
        CANCEL("{name:'Cancel', confirm:false}");


        private final String code;
        private ConfirmationButtons(final String code) {
            this.code = code;
        }
    }

    private ConfirmationPreAction(final String message, final ConfirmationButtons... buttons) {
        this.message = message;
        for (int buttonIndex = 0; buttonIndex < buttons.length; buttonIndex++) {
            this.buttons.add(buttons[buttonIndex].code);
        }
    }


    /** 
     * A convenient factory method to produce a confirmation dialog with buttons NO and YES.
     * 
     * @param msg
     * @return
     */
    public static ConfirmationPreAction yesNo(final String msg) {
        return new ConfirmationPreAction(msg, ConfirmationButtons.NO, ConfirmationButtons.YES);
    }
    
    /** 
     * A convenient factory method to produce a confirmation dialog with buttons CANCEL and OK.
     * 
     * @param msg
     * @return
     */
    public static ConfirmationPreAction okCancel(final String msg) {
        return new ConfirmationPreAction(msg, ConfirmationButtons.CANCEL, ConfirmationButtons.OK);
    }

    @Override
    public JsCode build() {
        return new JsCode("return self.confirm('" + this.message + "', [" + StringUtils.join(buttons, ",") + "])");
    }

}
