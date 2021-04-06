package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * Entity that represents collectional item in {@link CentreConfigLoadAction}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Save As Name", desc = "Save As Name of loadable centre configuration.")
@CompanionObject(ILoadableCentreConfig.class)
@DescTitle(value = "Description", desc = "Description of loadable centre configuration.")
public class LoadableCentreConfig extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Inherited", desc = "Indicates whether this centre configuration is connected with some base user's configuration of the same name.")
    private boolean inherited;

    @IsProperty
    @Title(value = "Config", desc = "Entity centre config instance (FRESH kind).")
    private EntityCentreConfig config;

    @IsProperty
    @Title(value = "Shared By Message", desc = "For inherited from shared configurations, contains domain-driven message about a user, who created that configuration and shared it with the current user.")
    private String sharedByMessage;

    @IsProperty
    @Title(value = "Orphaned Sharing Message", desc = "For own save-as configurations, contains message whether configuration was orphaned from based / shared. Such orphaned configurations act like own save-as except they can not be shared, unless duplicated and saved.")
    private String orphanedSharingMessage;

    @Observable
    public LoadableCentreConfig setOrphanedSharingMessage(final String orphanedSharingMessage) {
        this.orphanedSharingMessage = orphanedSharingMessage;
        return this;
    }

    public String getOrphanedSharingMessage() {
        return orphanedSharingMessage;
    }

    @Observable
    public LoadableCentreConfig setSharedByMessage(final String sharedByMessage) {
        this.sharedByMessage = sharedByMessage;
        return this;
    }

    public String getSharedByMessage() {
        return sharedByMessage;
    }

    @Observable
    public LoadableCentreConfig setConfig(final EntityCentreConfig config) {
        this.config = config;
        return this;
    }

    public EntityCentreConfig getConfig() {
        return config;
    }

    @Observable
    public LoadableCentreConfig setInherited(final boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    public boolean isInherited() {
        return inherited;
    }

    /**
     * Indicates whether loadable configuration represents inherited from shared.
     *
     * @return
     */
    public boolean isShared() {
        return getSharedByMessage() != null;
    }

    /**
     * Indicates whether loadable configuration represents inherited from base. Must be used in conjunction with {@link #isInherited()}.
     *
     * @return
     */
    public boolean isBase() {
        return !isShared();
    }

}