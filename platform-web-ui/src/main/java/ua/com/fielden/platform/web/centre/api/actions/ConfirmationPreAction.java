package ua.com.fielden.platform.web.centre.api.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

public class ConfirmationPreAction implements IPreAction {

    private final String message;
    private final List<String> buttons = new ArrayList<>();

    public enum ConfirmationButtons {

        YES("{name:'Yes', confirm:true}"),
        NO("{name:'No', confirm:false}");


        private final String code;
        private ConfirmationButtons(final String code) {
            this.code = code;
        }
    }

    public ConfirmationPreAction(final String message, final ConfirmationButtons... buttons) {
        this.message = message;
        for (int buttonIndex = 0; buttonIndex < buttons.length; buttonIndex++) {
            this.buttons.add(buttons[buttonIndex].code);
        }
    }


    @Override
    public JsCode build() {
        return new JsCode("return self.confirm('" + this.message + "', [" + StringUtils.join(buttons, ",") + "])");
    }

}
