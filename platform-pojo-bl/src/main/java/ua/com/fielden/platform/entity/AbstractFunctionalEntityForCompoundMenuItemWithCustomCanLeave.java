package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
    @Title("Leave Reason")
    private String leaveReason;

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

    private String getLeaveReason() {
        return leaveReason;
    }

    @Observable
    private AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setLeaveReason(final String leaveReason) {
        this.leaveReason = leaveReason;
        return this;
    }

    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave useLeaveReason(final LeaveReason leaveRason) {
        return this.setLeaveReason(leaveRason.name());
    }

    @Override
    public Optional<LeaveReason> leaveReason() {
        return isEmpty(leaveReason) ? empty() : of(LeaveReason.valueOf(leaveReason));
    }

    private String getCanLeaveOptions() {
        return canLeaveOptions;
    }

    @Observable
    private AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave setCanLeaveOptions(final String canLeaveOptions) {
        this.canLeaveOptions = canLeaveOptions;
        return this;
    }

    public AbstractFunctionalEntityForCompoundMenuItemWithCustomCanLeave useCanLeaveOptions(final CanLeaveOptions canLeaveOptions) {
        return this.setCanLeaveOptions(canLeaveOptions.name());
    }

    @Override
    public Optional<CanLeaveOptions> canLeaveOptions() {
        return isEmpty(canLeaveOptions) ? empty() : of(CanLeaveOptions.valueOf(canLeaveOptions));
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
