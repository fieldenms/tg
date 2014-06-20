package ua.com.fielden.platform.web.gis;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.apache.log4j.Logger;

public class MyEventDispatcher implements EventDispatcher {
    private final Logger logger = Logger.getLogger(getClass());
    private final EventDispatcher originalDispatcher;

    public MyEventDispatcher(final EventDispatcher originalDispatcher) {
        this.originalDispatcher = originalDispatcher;
    }

    @Override
    public Event dispatchEvent(final Event event, final EventDispatchChain tail) {
        if (event instanceof MouseEvent) {
            final MouseEvent mouseEvent = (MouseEvent) event;

            // logger.info("MouseEvent: " + mouseEvent);

            if (MouseButton.SECONDARY == mouseEvent.getButton()) {
                // mouseEvent.consume();
            }
        }
        return originalDispatcher.dispatchEvent(event, tail);
    }
}