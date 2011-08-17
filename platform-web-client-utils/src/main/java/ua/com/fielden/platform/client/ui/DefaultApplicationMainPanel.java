package ua.com.fielden.platform.client.ui;

import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.TreeMenuPanel;
import ua.com.fielden.platform.swing.menu.UndockableTreeMenuWithTabs;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * This is a panel for the main application frame, which provides access to all available functionality.
 *
 * @author TG Team
 *
 */
public class DefaultApplicationMainPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private final JSplitPane spittterPane;
    private final TreeMenuPanel treeMenuPanel;

    public DefaultApplicationMainPanel(final UndockableTreeMenuWithTabs<?> menu) {
	super(new MigLayout("fill", "[grow,fill]", "[grow,fill]"));

	spittterPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
	spittterPane.setOneTouchExpandable(true);

	add(spittterPane);
	treeMenuPanel = new TreeMenuPanel(menu, 300);
	treeMenuPanel.getTree().collapseAll();
	menu.getTreeProgressLayer().setView(treeMenuPanel);
	addTreeMenuPanel(menu.getTreeProgressLayer());
    }

    @Override
    public String getInfo() {
	return "Main";
    }

    @Override
    public void close() {
	super.close();
	// TODO this is a temporary solution until application life cycle is managed by the application framework.
	System.out.println("EXIT");
	System.exit(0);
    }

    protected void addTreeMenuPanel(final BlockingIndefiniteProgressLayer progressLayer) {
	spittterPane.setLeftComponent(progressLayer);
	spittterPane.setRightComponent(treeMenuPanel.getTree().getHolder());
    }

    public DefaultApplicationMainPanel setDividerLocation(final double dividerLocation) {
	spittterPane.setDividerLocation(dividerLocation);
	return this;
    }

    public DefaultApplicationMainPanel setOneTouchExpandable(final boolean flag) {
	spittterPane.setOneTouchExpandable(flag);
	return this;
    }

    public TreeMenuPanel getTreeMenuPanel() {
	return treeMenuPanel;
    }

}
