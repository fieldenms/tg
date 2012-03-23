package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;

public class StubMenuItemVisibilityProvider implements ITreeMenuItemVisibilityProvider {

    private boolean visible = true;

    @Override
    public void setVisible(final boolean visible) {
	if (visible != this.visible) {
	    this.visible = visible;
	}
    }

    @Override
    public boolean isVisible() {
	return visible;
    }

}
