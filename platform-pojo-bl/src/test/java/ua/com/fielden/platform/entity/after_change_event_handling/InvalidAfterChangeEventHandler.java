package ua.com.fielden.platform.entity.after_change_event_handling;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Invalid ACE event handler for testing purposes.
 * It contains another ACE handler as of its properties.
 * This is not a problem as long as this property is not used as parameter.
 *
 * @author TG Team
 *
 */
public class InvalidAfterChangeEventHandler implements IAfterChangeEventHandler {

    private AfterChangeEventHandler invalidParam;

    private boolean invoked = false;

    @Override
    public void handle(final MetaProperty property, final Object entityPropertyValue) {
	setInvoked(true);
    }

    public AfterChangeEventHandler getInvalidParam() {
        return invalidParam;
    }

    public void setInvalidParam(final AfterChangeEventHandler invalidParam) {
        this.invalidParam = invalidParam;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(final boolean invoked) {
        this.invoked = invoked;
    }
}
