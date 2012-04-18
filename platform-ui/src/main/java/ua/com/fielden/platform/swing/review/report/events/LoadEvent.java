package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import javax.swing.JComponent;

/**
 * An {@link EventObject} that specifies the component that was loaded
 * 
 * @author TG Team
 *
 */
public class LoadEvent extends EventObject {

    private static final long serialVersionUID = 190489982304734671L;

    /**
     * Initialises {@link LoadEvent} with loaded component
     * 
     * @param source
     */
    public LoadEvent(final JComponent component) {
	super(component);
    }

    @Override
    public JComponent getSource() {
	return (JComponent)super.getSource();
    }

}
