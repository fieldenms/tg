package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
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
@CompanionObject(ILoadableCentreConfig.class)
@DescTitle(value = "Description", desc = "Description of loadable centre configuration.")
public class LoadableCentreConfig extends AbstractEntity<String> {
    
    @IsProperty
    @Title(value = "Inherited", desc = "Indicates whether this centre configuration is connected with some base user's configuration of the same name.")
    private boolean inherited;
    
    @IsProperty
    @Title(value = "Shared By", desc = "In case of inherited from shared configuration, contains the user which created that configuration and shared it with current user.")
    private User sharedBy;
    
    @IsProperty
    @Title(value = "Config", desc = "Entity centre config instance (FRESH kind).")
    private EntityCentreConfig config;
    
    @IsProperty
    @Title(value = "Shared By Message", desc = "In case of inherited from shared configuration, contains domain-driven message about whom created that configuration and shared it with current user.")
    private String sharedByMessage;
    
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
    public LoadableCentreConfig setSharedBy(final User sharedBy) {
        this.sharedBy = sharedBy;
        return this;
    }
    
    public User getSharedBy() {
        return sharedBy;
    }
    
    @Observable
    public LoadableCentreConfig setInherited(final boolean inherited) {
        this.inherited = inherited;
        return this;
    }
    
    public boolean isInherited() {
        return inherited;
    }
    
}