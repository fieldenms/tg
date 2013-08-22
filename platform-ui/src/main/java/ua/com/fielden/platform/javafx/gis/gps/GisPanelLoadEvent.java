package ua.com.fielden.platform.javafx.gis.gps;

import java.util.EventObject;

import javafx.concurrent.Worker.State;
import ua.com.fielden.platform.javafx.gis.GisViewPanel;

/**
 * Event that is generated when {@link GisViewPanel} is loaded.
 *
 * @author TG Team
 *
 */
public class GisPanelLoadEvent extends EventObject {

    private static final long serialVersionUID = -5444551856422636818L;

    private final State state;

    public GisPanelLoadEvent(final GisViewPanel<?, ?> source, final State state) {
	super(source);
	this.state = state;
    }

    @Override
    public GisViewPanel<?, ?> getSource() {
        return (GisViewPanel<?, ?>) super.getSource();
    }

    /**
     * Returns the state of {@link GisViewPanel} after it was loaded (please note that state might be only SUCCEEDED or FAILED).
     *
     * @return
     */
    public State getState() {
	return state;
    }
}
