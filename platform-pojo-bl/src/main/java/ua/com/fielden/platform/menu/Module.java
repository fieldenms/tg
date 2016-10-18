package ua.com.fielden.platform.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
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
@KeyTitle(value = "Title", desc = "Module title")
@CompanionObject(IModule.class)
@DescTitle(value = "Description", desc = "Short explanation of module purpose")
public class Module extends AbstractEntity<String> implements IMenuManager {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Background color", desc = "Background color")
    private String bgColor;

    @IsProperty
    @Title(value = "Caption background color", desc = "Caption background color")
    private String captionBgColor;

    @IsProperty
    @Title(value = "Icon", desc = "Icon")
    private String icon;

    @IsProperty
    @Title(value = "Detail Icon", desc = "Detail icon")
    private String detailIcon;

    @IsProperty(ModuleMenuItem.class)
    @Title(value = "Module menu", desc = "Module menu")
    private List<ModuleMenuItem> menu = new ArrayList<ModuleMenuItem>();

    @Observable
    public Module setMenu(final List<ModuleMenuItem> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    public List<ModuleMenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    @Observable
    public Module setDetailIcon(final String detailIcon) {
        this.detailIcon = detailIcon;
        return this;
    }

    public String getDetailIcon() {
        return detailIcon;
    }

    @Observable
    public Module setIcon(final String icon) {
        this.icon = icon;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    @Observable
    public Module setCaptionBgColor(final String captionBgColor) {
        this.captionBgColor = captionBgColor;
        return this;
    }

    public String getCaptionBgColor() {
        return captionBgColor;
    }

    @Observable
    public Module setBgColor(final String bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public String getBgColor() {
        return bgColor;
    }

    @Override
    public boolean removeMenuItem(final String title) {
        return menu.removeIf(menuItem -> menuItem.getKey().equals(title));
    }

    @Override
    public Optional<? extends IMenuManager> getMenuItem(final String title) {
        return menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst();
    }

    @Override
    public void makeMenuItemInvisible(final String title) {
        menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst().ifPresent(menuItem -> menuItem.setIsVisible(false));
    }

    @Override
    public Module setKey(final String key) {
        return (Module) super.setKey(key);
    }

    @Override
    public Module setDesc(final String desc) {
        return (Module) super.setDesc(desc);
    }
}