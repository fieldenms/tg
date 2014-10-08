package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;

/**
 * Represents the entity centre. In order to add new entity centre
 *
 * @author TG Team
 *
 */
public class EntityCentre {

    private final Class<? extends MiWithConfigurationSupport<?>> menuItemType;

    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> menuItemType) {
	this.menuItemType = menuItemType;
    }

    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
	return this.menuItemType;
    }

}
