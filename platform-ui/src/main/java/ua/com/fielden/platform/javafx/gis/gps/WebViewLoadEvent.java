package ua.com.fielden.platform.javafx.gis.gps;

import java.util.EventObject;

import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

/**
 * Event that is generated when {@link WebView} is loaded.
 * 
 * @author TG Team
 * 
 */
public class WebViewLoadEvent extends EventObject {

    private static final long serialVersionUID = -5444551856422636818L;

    private final State state;

    public WebViewLoadEvent(final Object source, final State state) {
        super(source);
        this.state = state;
    }

    /**
     * Returns the state of {@link WebView} after it was loaded (please note that state might be only SUCCEEDED or FAILED).
     * 
     * @return
     */
    public State getState() {
        return state;
    }
}
