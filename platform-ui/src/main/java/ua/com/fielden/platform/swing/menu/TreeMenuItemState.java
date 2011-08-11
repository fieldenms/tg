package ua.com.fielden.platform.swing.menu;

/**
 * Represents {@link TreeMenuItem} states. Menu item might has three states:
 * <ul>
 * <li>DOCK - When the view of the menu item is on the holder panel of the {@link TreeMenu};</li>
 * <li>UNDOCK - When the view of the menu item is on the separate frame;</li>
 * <li>NONE - When the menu item doesn't has view, therefore there is nothing to dock or undock;</li>
 * <li>ALL - Indicates that view of the menu item might be docked or undocked.</li>
 * </ul>
 * 
 * @author oleh
 * 
 */
public enum TreeMenuItemState {

    DOCK, UNDOCK, NONE, ALL;
}
