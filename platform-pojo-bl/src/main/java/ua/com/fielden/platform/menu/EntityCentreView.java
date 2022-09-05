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

    @IsProperty
    @Title(value = "UUID", desc = "UUID")
    private String uuid;

    @IsProperty
    @Title(value = "Enforce post save refresh", desc = "Should run centre after save")
    private boolean enforcePostSaveRefresh;

    @IsProperty
    private String uri;

    @IsProperty
    @Title(value = "Class of observer", desc = "SSE observer class")
    private String observableClass;

    @Observable
    public EntityCentreView setObservableClass(final String observableClass) {
        this.observableClass = observableClass;
        return this;
    }

    public String getObservableClass() {
        return observableClass;
    }

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
    public EntityCentreView setUuid(final String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

}