package ua.com.fielden.platform.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IMenu.class)
public class Menu extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(Module.class)
    @Title(value = "Menu", desc = "Menu")
    private List<Module> menu = new ArrayList<Module>();

    @Observable
    public Menu setMenu(final List<Module> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    public List<Module> getMenu() {
        return Collections.unmodifiableList(menu);
    }

}