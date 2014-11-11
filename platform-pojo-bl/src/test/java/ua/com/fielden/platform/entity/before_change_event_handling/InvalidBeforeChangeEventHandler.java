package ua.com.fielden.platform.entity.before_change_event_handling;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Invalid BCE event handler for testing purposes. It contains another BCE handler as of its properties. This is not a problem as long as this property is not used as
 * {@link Handler} parameter.
 *
 * @author TG Team
 *
 */
public class InvalidBeforeChangeEventHandler implements IBeforeChangeEventHandler<String> {

    private BeforeChangeEventHandler invalidParam;

    private boolean invoked = false;

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final String oldValue, final Set<Annotation> mutatorAnnotations) {
        setInvoked(true);
        return Result.successful(null);
    }

    public BeforeChangeEventHandler getInvalidParam() {
        return invalidParam;
    }

    public void setInvalidParam(final BeforeChangeEventHandler invalidParam) {
        this.invalidParam = invalidParam;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(final boolean invoked) {
        this.invoked = invoked;
    }
}
