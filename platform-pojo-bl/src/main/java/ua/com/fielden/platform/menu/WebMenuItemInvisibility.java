package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.menu.validators.UserAsConfigurationOwnerValidator;
import ua.com.fielden.platform.security.user.User;
/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(DynamicEntityKey.class)
@CompanionObject(IWebMenuItemInvisibility.class)
@MapEntityTo
public class WebMenuItemInvisibility extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo
    @BeforeChange(@Handler(UserAsConfigurationOwnerValidator.class))
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Menu item URI", desc = "Menu item URI invisible to the user that is based on owning user.")
    @MapTo
    private String menuItemUri;

    public String getMenuItemUri() {
        return menuItemUri;
    }

    @Observable
    public WebMenuItemInvisibility setMenuItemUri(final String menuItemUri) {
        this.menuItemUri = menuItemUri;
        return this;
    }

    public User getOwner() {
        return owner;
    }

    @Observable
    public WebMenuItemInvisibility setOwner(final User owner) {
        this.owner = owner;
        return this;
    }
}