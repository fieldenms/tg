package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

/**
 * Entity that represents collectional item in {@link CentreConfigLoadAction}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Save As Name", desc = "Save As Name of loadable centre configuration.")
@CompanionObject(LoadableCentreConfigCo.class)
@DescTitle(value = "Description", desc = "Description of loadable centre configuration.")
public class LoadableCentreConfig extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Required
    @Title(value = "Save As Name", desc = "Save As Name of loadable centre configuration.")
    @SkipDefaultStringKeyMemberValidation({RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class})
    private String key;

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

    @IsProperty
    @Title(value = "Shared By", desc = "For inherited from shared configurations, contains the user, who created that configuration and shared it with the current user.")
    private User sharedBy;

    @IsProperty
    @Title(value = "Save As Name", desc = "For inherited from shared configurations, contains configuration title; the title can be different from title of the inherited configuration, as the creator could have changed it and current user haven't updated it yet.")
    private String saveAsName;

    @Observable
    public LoadableCentreConfig setSaveAsName(final String saveAsName) {
        this.saveAsName = saveAsName;
        return this;
    }

    public String getSaveAsName() {
        return saveAsName;
    }

    @Observable
    public LoadableCentreConfig setSharedBy(final User sharedBy) {
        this.sharedBy = sharedBy;
        return this;
    }

    public User getSharedBy() {
        return sharedBy;
    }

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

    @Observable
    public LoadableCentreConfig setKey(final String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }

    @Override
    @Observable
    protected LoadableCentreConfig setDesc(String desc) {
        return super.setDesc(desc);
    }

}
