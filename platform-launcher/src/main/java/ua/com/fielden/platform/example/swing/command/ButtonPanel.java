/**
 *
 */
package ua.com.fielden.platform.example.swing.command;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.actions.Command;

/**
 * This panel aggregates command actions and corresponding buttons demonstrating handling of unhandled exceptions in different situations.
 * 
 * @author 01es
 */
public class ButtonPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public ButtonPanel() {
        setLayout(new MigLayout("fill", "[]", "[][]"));
        add(new JButton(actionWithoutException()), "growx, wrap");
        add(new JButton(actionWithExceptionInPreAction()), "growx, wrap");
        add(new JButton(actionWithExceptionInAction()), "growx, wrap");
        add(new JButton(actionWithExceptionInPostAction()), "growx");
    }

    protected Action actionWithoutException() {
        final Action action = new Command<Object>("Without Ex") {

            private static final long serialVersionUID = 987099417353690738L;

            @Override
            protected Object action(final ActionEvent e) throws Exception {
                Thread.sleep(1000);
                return null;
            }

        };

        return action;
    }

    protected Action actionWithExceptionInAction() {
        final Action action = new Command<Object>("With Ex In Action") {

            private static final long serialVersionUID = 1361248656895891185L;

            @Override
            protected Object action(final ActionEvent e) throws Exception {
                Thread.sleep(1000);
                throw new Exception("Uncought runtime exception (example).");
            }

        };

        return action;
    }

    protected Action actionWithExceptionInPreAction() {
        final Action action = new Command<Object>("With Ex in Pre Action") {

            private static final long serialVersionUID = 1361248656895891185L;

            @Override
            protected boolean preAction() {
                super.preAction();
                throw new IllegalStateException("Uncought runtime exception (example).");
            }

            @Override
            protected Object action(final ActionEvent e) throws Exception {
                return null;
            }

        };

        return action;
    }

    protected Action actionWithExceptionInPostAction() {
        final Action action = new Command<Object>("With Ex in Post Action") {

            private static final long serialVersionUID = 1361248656895891185L;

            @Override
            protected Object action(final ActionEvent e) throws Exception {
                return null;
            }

            @Override
            protected void postAction(final Object value) {
                throw new IllegalStateException("Uncought runtime exception (example).");
            }
        };

        return action;
    }

}
