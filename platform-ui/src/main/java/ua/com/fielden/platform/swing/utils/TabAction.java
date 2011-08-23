package ua.com.fielden.platform.swing.utils;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

/**
 * A calls to handle TAB and SHIFT+TAB key press.
 * 
 * 
 */
public class TabAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private boolean forward;

    public TabAction(final boolean forward) {
        this.forward = forward;
    }

    public void actionPerformed(final ActionEvent e) {
        if (forward) {
    	tabForward();
        } else {
    	tabBackward();
        }
    }

    private void tabForward() {
        final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.focusNextComponent();

        SwingUtilities.invokeLater(new Runnable() {
    	public void run() {
    	    if (manager.getFocusOwner() instanceof JScrollBar) {
    		manager.focusNextComponent();
    	    }
    	}
        });
    }

    private void tabBackward() {
        final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.focusPreviousComponent();

        SwingUtilities.invokeLater(new Runnable() {
    	public void run() {
    	    if (manager.getFocusOwner() instanceof JScrollBar) {
    		manager.focusPreviousComponent();
    	    }
    	}
        });
    }
}