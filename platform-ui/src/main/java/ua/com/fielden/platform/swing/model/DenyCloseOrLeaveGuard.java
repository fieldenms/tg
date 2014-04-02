package ua.com.fielden.platform.swing.model;

/**
 * This is a convenient implementation of {@link ICloseGuard}, which denies closing or leaving a view using it.
 * 
 * @author TG Team
 * 
 */
public class DenyCloseOrLeaveGuard implements ICloseGuard {

    private final String denyCloseReason;

    public DenyCloseOrLeaveGuard(final String denyCloseReason) {
        this.denyCloseReason = denyCloseReason;
    }

    @Override
    public ICloseGuard canClose() {
        return this;
    }

    @Override
    public String whyCannotClose() {
        return denyCloseReason;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean canLeave() {
        return false;
    }

}
