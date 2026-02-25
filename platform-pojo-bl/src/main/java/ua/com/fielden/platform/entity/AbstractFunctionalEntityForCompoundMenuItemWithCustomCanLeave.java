package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/// A base class for functional entities that are intended to be used on compound master as menu item that has custom `canLeave` implementation.
///
/// @param <K> -- primary entity type for compound master
///
public class AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave<K extends AbstractEntity<?>> extends AbstractFunctionalEntityForCompoundMenuItem<K> implements ICustomisableCanLeave {

    @IsProperty
    @Title("Can Leave")
    private boolean canLeave;

    @IsProperty
    @Title("Can not Leave Reason")
    private String cannotLeaveReason;

    @IsProperty
    @Title("Can Leave Options")
    private String canLeaveOptions;

    @IsProperty
    @Title(value = "Is Closing?", desc = "Indicates whether this menu item is closing")
    private boolean closing;

    @IsProperty
    @Title("Close Instructions")
    private String closeInstructions;

    @Override
    public String getCloseInstructions() {
        return closeInstructions;
    }

    @Observable
    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCloseInstructions(final String closeInstructions) {
        this.closeInstructions = closeInstructions;
        return this;
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Observable
    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setClosing(final boolean closing) {
        this.closing = closing;
        return this;
    }

    @Override
    public String getCanLeaveOptions() {
        return canLeaveOptions;
    }

    @Observable
    protected AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCanLeaveOptions(final String canLeaveOptions) {
        this.canLeaveOptions = canLeaveOptions;
        return this;
    }

    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave useCanLeaveOptions(final CanLeaveOptions canLeaveOptions) {
        return this.setCanLeaveOptions(canLeaveOptions.name());
    }

    public CanLeaveOptions canLeaveOptions() {
        return CanLeaveOptions.valueOf(this.canLeaveOptions);
    }

    @Override
    public String getCannotLeaveReason() {
        return cannotLeaveReason;
    }

    @Observable
    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCannotLeaveReason(final String cannotLeaveReason) {
        this.cannotLeaveReason = cannotLeaveReason;
        return this;
    }

    @Override
    public boolean isCanLeave() {
        return canLeave;
    }

    @Observable
    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCanLeave(final boolean canLeave) {
        this.canLeave = canLeave;
        return this;
    }
}
