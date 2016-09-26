package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents the module's menu item with view.
 *
 * @author TG team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Title", desc = "Menu item title")
@CompanionObject(IModuleMenuItem.class)
@DescTitle(value = "Description", desc = "Menu item description")
public class ModuleMenuItem extends AbstractEntity<String> {

}