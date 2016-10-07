package ua.com.fielden.platform.menu;

import java.util.Optional;

public interface IMenuManager {

    Optional<? extends IMenuManager> getMenuItem(String title);

    boolean removeMenuItem(String title);

    void makeMenuItemInvisible(String title);
}
