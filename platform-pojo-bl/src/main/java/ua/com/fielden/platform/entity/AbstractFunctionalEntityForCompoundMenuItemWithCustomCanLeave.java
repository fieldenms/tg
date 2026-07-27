package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

import java.util.Optional;

/// A base class for functional entities that are intended to be used on compound master as menu item that has custom `canLeave` implementation.
///
/// @param <K>  the main entity type for the compound master
///
public abstract class AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave<K extends AbstractEntity<?>> extends AbstractFunctionalEntityForCompoundMenuItem<K> implements ICustomisableCanLeave {

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
    @Title("Leave Reason")
    private String leaveReason;

    @IsProperty
    @Title("Leave Instructions")
    private String leaveInstructions;

    @Override
    public String getLeaveInstructions() {
        return leaveInstructions;
    }

    @Observable
    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setLeaveInstructions(final String leaveInstructions) {
        this.leaveInstructions = leaveInstructions;
        return this;
    }

    private String getLeaveReason() {
        return leaveReason;
    }

    @Observable
    protected AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setLeaveReason(final String leaveReason) {
        this.leaveReason = leaveReason;
        return this;
    }

    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setLeaveReason(final LeaveReason leaveReason) {
        return this.setLeaveReason(leaveReason.name());
    }

    @Override
    public Optional<LeaveReason> leaveReason() {
        return LeaveReason.of(leaveReason);
    }

    private String getCanLeaveOptions() {
        return canLeaveOptions;
    }

    @Observable
    protected AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCanLeaveOptions(final String canLeaveOptions) {
        this.canLeaveOptions = canLeaveOptions;
        return this;
    }

    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCanLeaveOptions(final CanLeaveOptions canLeaveOptions) {
        return this.setCanLeaveOptions(canLeaveOptions.name());
    }

    @Override
    public Optional<CanLeaveOptions> canLeaveOptions() {
        return CanLeaveOptions.of(canLeaveOptions);
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
