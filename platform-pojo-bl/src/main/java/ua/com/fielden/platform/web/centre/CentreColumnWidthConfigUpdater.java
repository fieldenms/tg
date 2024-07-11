package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

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
    
    public CentreColumnWidthConfigUpdater() {
        setKey(NO_KEY);
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