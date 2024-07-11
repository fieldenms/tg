package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

/**
 * Entity for updating widths and grow factors of entity centre EGI columns.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(CentreColumnWidthConfigUpdaterCo.class)
public class CentreColumnWidthConfigUpdater extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    @IsProperty
    @Title(value = "Centre Dirty", desc = "Indicates whether successful saving of this entity actually changed centre configuration or it is New (aka default, link or inherited).")
    private boolean centreDirty;

    @IsProperty
    @Title(value = "Centre Changed", desc = "Indicates whether successful saving of this entity actually changed centre configuration.")
    private boolean centreChanged;

    public CentreColumnWidthConfigUpdater() {
        setKey(NO_KEY);
    }

    @Observable
    public CentreColumnWidthConfigUpdater setCentreChanged(final boolean centreChanged) {
        this.centreChanged = centreChanged;
        return this;
    }

    public boolean isCentreChanged() {
        return centreChanged;
    }
    
    @Observable
    public CentreColumnWidthConfigUpdater setCentreDirty(final boolean centreDirty) {
        this.centreDirty = centreDirty;
        return this;
    }
    
    public boolean isCentreDirty() {
        return centreDirty;
    }
    
}