package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

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
    @Title(value = "Preferred View", desc = "The index of the preferred view.")
    private Integer preferredView;

    @IsProperty
    @Title(value = "Centre dirty?", desc = "Indicates whether successful saving of this entity actually changed the centre configuration or it is 'new' (i.e. default, link or inherited).")
    private boolean centreDirty;

    @IsProperty
    @Title(value = "Centre changed?", desc = "Indicates whether successful saving of this entity actually changed the centre configuration.")
    private boolean centreChanged;

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
    public CentrePreferredViewUpdater setCentreChanged(final boolean centreChanged) {
        this.centreChanged = centreChanged;
        return this;
    }

    public boolean isCentreChanged() {
        return centreChanged;
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