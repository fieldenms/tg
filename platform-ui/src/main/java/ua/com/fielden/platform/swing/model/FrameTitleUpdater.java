package ua.com.fielden.platform.swing.model;

import javax.swing.JFrame;

import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * A view, which is associated with some UI model, always belongs to another view or to a frame. This class provides a way for UI models to update frame title whenever is necessary
 * without any tight coupling with its view or a frame.
 * 
 * @author TG Team
 * 
 */
public final class FrameTitleUpdater {
    private final JFrame frm;

    public FrameTitleUpdater(final JFrame frm) {
	this.frm = frm;
    }

    /**
     * EDT safe method for updating frame title.
     * 
     * @param newTitle
     */
    public void update(final String newTitle) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		frm.setTitle(newTitle);
	    }
	});

    }
}
