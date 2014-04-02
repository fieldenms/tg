package ua.com.fielden.platform.entity.before_change_event_handling;

/**
 * A controller for BCE handler testing purposes.
 * 
 * @author TG Team
 * 
 */
public class Controller {
    private boolean invoked = false;

    public void run() {
        setInvoked(true);
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(final boolean invoked) {
        this.invoked = invoked;
    }
}
