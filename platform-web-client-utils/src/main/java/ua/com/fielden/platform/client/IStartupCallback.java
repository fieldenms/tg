package ua.com.fielden.platform.client;

import ua.com.fielden.platform.swing.menu.UndockableTreeMenuWithTabs;

public interface IStartupCallback {

    void doAfterStart(final UndockableTreeMenuWithTabs<?> menu);
}
