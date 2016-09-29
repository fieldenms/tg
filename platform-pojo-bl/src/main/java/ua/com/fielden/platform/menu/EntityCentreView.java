package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the entity centre view.
 *
 * @author TG Team.
 *
 */
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityCentreView.class)
public class EntityCentreView extends AbstractView {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "UUID", desc = "UUID")
    private String uuid;

    @IsProperty
    @Title(value = "Autorun", desc = "Should run automaticaly")
    private boolean autoRun;

    @IsProperty
    @Title(value = "Enforce post save refresh", desc = "Should run centre after save")
    private boolean enforcePostSaveRefresh;

    @IsProperty
    private String uri;

    @Observable
    public EntityCentreView setUri(final String uri) {
        this.uri = uri;
        return this;
    }

    public String getUri() {
        return uri;
    }

    @Observable
    public EntityCentreView setEnforcePostSaveRefresh(final boolean enforcePostSaveRefresh) {
        this.enforcePostSaveRefresh = enforcePostSaveRefresh;
        return this;
    }

    public boolean getEnforcePostSaveRefresh() {
        return enforcePostSaveRefresh;
    }

    @Observable
    public EntityCentreView setAutoRun(final boolean autoRun) {
        this.autoRun = autoRun;
        return this;
    }

    public boolean getAutoRun() {
        return autoRun;
    }

    @Observable
    public EntityCentreView setUuid(final String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getUuid() {
        return uuid;
    }



}