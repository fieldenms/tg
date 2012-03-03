package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.ImageIcon;

import ua.com.fielden.platform.swing.taskpane.ImagePanel;

public class ActionImagePanel extends ImagePanel {

    private static final long serialVersionUID = 3282060276544804231L;

    private final Action action;

    public ActionImagePanel(final Action action){
	super((ImageIcon)action.getValue(Action.LARGE_ICON_KEY));
	this.action = action;
	setToolTipText((String)action.getValue(Action.SHORT_DESCRIPTION));
	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	addMouseListener(createClickListener());
	setBackground(null);
    }

    private MouseListener createClickListener() {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		action.actionPerformed(null);
	    }
	};
    }

    public Action getAction() {
	return action;
    }
}
