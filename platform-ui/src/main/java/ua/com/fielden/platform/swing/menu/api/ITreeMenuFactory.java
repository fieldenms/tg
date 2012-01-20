package ua.com.fielden.platform.swing.menu.api;

import java.util.List;

import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * A contract for building a complete menu tree.
 *
 * @author TG Team
 *
 */
public interface ITreeMenuFactory {

    ITreeMenuFactory bind(Class<? extends TreeMenuItem> type, ITreeMenuItemFactory itemFactory);

    void build(List<MainMenuItem> itemsFromCloud);
}
