package ua.com.fielden.platform.swing.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class ActionWithPostAction extends AbstractAction {
    private Action postAction;

    public ActionWithPostAction() {
    }

    protected abstract void action(ActionEvent e);

    @Override
    public final void actionPerformed(final ActionEvent e) {
        action(e);
        if (postAction != null) {
            postAction.actionPerformed(e);
        }
    }

    public Action getPostAction() {
        return postAction;
    }

    public void setPostAction(final Action postAction) {
        this.postAction = postAction;
    }

}
