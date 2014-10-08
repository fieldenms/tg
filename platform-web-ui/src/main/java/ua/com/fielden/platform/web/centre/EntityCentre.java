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
    private final String name;

    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> menuItemType, final String name) {
	this.menuItemType = menuItemType;
	this.name = name;
    }

    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
	return this.menuItemType;
    }

    public String getName() {
	return name;
    }

}
