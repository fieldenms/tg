package ua.com.fielden.platform.menu;

import static java.util.Collections.unmodifiableList;

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
 * Represents application's module.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Title", desc = "Module title")
@CompanionObject(ModuleMenuCo.class)
@DescTitle(value = "Description", desc = "Short explanation of module purpose")
public class ModuleMenu extends AbstractEntity<String> implements IMenuManager {
    @IsProperty
    @Title("Background color")
    private String bgColor;

    @IsProperty
    @Title("Caption background color")
    private String captionBgColor;

    @IsProperty
    @Title("Icon")
    private String icon;

    @IsProperty
    @Title("Detail Icon")
    private String detailIcon;

    @IsProperty(ModuleMenuItem.class)
    @Title("Module menu")
    private final List<ModuleMenuItem> menu = new ArrayList<ModuleMenuItem>();

    @IsProperty
    @Title("Module view")
    private View view;

    @IsProperty(Action.class)
    @Title("Actions")
    private final List<Action> actions = new ArrayList<>();

    @Observable
    public ModuleMenu setActions(final List<Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public List<Action> getActions() {
        return unmodifiableList(actions);
    }

    @Observable
    public ModuleMenu setView(final View view) {
        this.view = view;
        return this;
    }

    public View getView() {
        return view;
    }

    @Observable
    public ModuleMenu setMenu(final List<ModuleMenuItem> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    @Override
    public List<ModuleMenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    @Observable
    public ModuleMenu setDetailIcon(final String detailIcon) {
        this.detailIcon = detailIcon;
        return this;
    }

    public String getDetailIcon() {
        return detailIcon;
    }

    @Observable
    public ModuleMenu setIcon(final String icon) {
        this.icon = icon;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    @Observable
    public ModuleMenu setCaptionBgColor(final String captionBgColor) {
        this.captionBgColor = captionBgColor;
        return this;
    }

    public String getCaptionBgColor() {
        return captionBgColor;
    }

    @Observable
    public ModuleMenu setBgColor(final String bgColor) {
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
    public Optional<ModuleMenuItem> getMenuItem(final String title) {
        return menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst();
    }

    @Override
    public void makeMenuItemInvisible(final String title) {
        menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst().ifPresent(menuItem -> menuItem.setVisible(false));
    }

    @Override
    public void makeMenuItemSemiVisible(final String title) {
        menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst().ifPresent(menuItem -> menuItem.setSemiVisible(true));
    }

    @Override
    public ModuleMenu setKey(final String key) {
        return (ModuleMenu) super.setKey(key);
    }

    @Override
    public ModuleMenu setDesc(final String desc) {
        return (ModuleMenu) super.setDesc(desc);
    }

    @Override
    public String getTitle() {
        return getKey();
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isSemiVisible() {
        return false;
    }
}