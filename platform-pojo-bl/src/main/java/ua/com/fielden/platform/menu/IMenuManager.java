package ua.com.fielden.platform.menu;

import java.util.Optional;

public interface IMenuManager {

    <U extends IMenuManager> Optional<U> getMenuItem(String title);

    boolean removeMenuItem(String title);

    void makeMenuItemInvisible(String title);
}
