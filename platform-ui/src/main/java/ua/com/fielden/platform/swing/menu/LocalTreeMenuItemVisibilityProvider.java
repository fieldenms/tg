package ua.com.fielden.platform.swing.menu;

/**
 * Implementation of {@local ITreeMenuItemVisibilityProvider} contract for supporting the development workflow.
 * Basically, this provider does not change menu item's visibility and always returns <code>true</code> for {@link LocalTreeMenuItemVisibilityProvider#isVisible()}.
 *
 * @author TG Team
 *
 */
public class LocalTreeMenuItemVisibilityProvider implements ITreeMenuItemVisibilityProvider {

    @Override
    public void setVisible(final boolean visible) {
    }

    @Override
    public boolean isVisible() {
	return true;
    }

}
