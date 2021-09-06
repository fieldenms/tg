package ua.com.fielden.platform.menu;

import java.util.List;
import java.util.Optional;

public interface IMenuManager {

    <U extends IMenuManager> Optional<U> getMenuItem(String title);

    boolean removeMenuItem(String title);

    void makeMenuItemInvisible(String title);

    void makeMenuItemInvisibleForSomeUser(String title);

    public boolean isVisible();

    public boolean isVisibleForAllUsers();

    <U extends IMenuManager> List<U> getMenu();

    String getTitle();
}
