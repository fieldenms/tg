package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.CanLeaveOptions;
import ua.com.fielden.platform.entity.ICustomisableCanLeave;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.utils.Pair;

/** 
 * Master entity object to model the detail menu item of the compound master entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(TgCompoundEntity.class)
@CompanionObject(ITgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class)
@EntityTitle("Tg Compound Entity Master Tg Compound Entity Child Menu Item")
public class TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<TgCompoundEntity>  implements ICustomisableCanLeave {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @Title("Can Leave")
    private boolean canLeave;

    @IsProperty
    @Title("Can not Leave Reason")
    private String cannotLeaveReason;

    @IsProperty
    @Title("Can Leave Options")
    private CanLeaveOptions canLeaveOptions;

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
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem setCloseInstructions(final String closeInstructions) {
        this.closeInstructions = closeInstructions;
        return this;
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Observable
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem setClosing(final boolean closing) {
        this.closing = closing;
        return this;
    }

    @Override
    public CanLeaveOptions getCanLeaveOptions() {
        return canLeaveOptions;
    }

    @Observable
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem setCanLeaveOptions(final CanLeaveOptions canLeaveOptions) {
        this.canLeaveOptions = canLeaveOptions;
        return this;
    }

    @Override
    public String getCannotLeaveReason() {
        return cannotLeaveReason;
    }

    @Observable
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem setCannotLeaveReason(final String cannotLeaveReason) {
        this.cannotLeaveReason = cannotLeaveReason;
        return this;
    }

    @Override
    public boolean isCanLeave() {
        return canLeave;
    }

    @Observable
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem setCanLeave(final boolean canLeave) {
        this.canLeave = canLeave;
        return this;
    }
}