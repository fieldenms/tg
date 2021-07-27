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
 * Entity to update centre's preferred view.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(CentrePreferredViewUpdaterCo.class)
public class CentrePreferredViewUpdater extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Preferred View", desc = "Preferred View Index")
    private Integer preferredView;

    @IsProperty
    @Title(value = "Centre Dirty", desc = "Indicates whether successful saving of this entity actually changed centre configuration or it is New (aka default, link or inherited).")
    private boolean centreDirty;

    public CentrePreferredViewUpdater() {
        setKey(NO_KEY);
    }

    @Observable
    public CentrePreferredViewUpdater setPreferredView(final Integer preferredView) {
        this.preferredView = preferredView;
        return this;
    }

    public Integer getPreferredView() {
        return preferredView;
    }

    @Observable
    public CentrePreferredViewUpdater setCentreDirty(final boolean centreDirty) {
        this.centreDirty = centreDirty;
        return this;
    }

    public boolean isCentreDirty() {
        return centreDirty;
    }

}