package ua.com.fielden.platform.swing.view;

import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.menu.TreeMenuPanel;

/**
 * This is a base class for implementing UI panels serving as a master entity view such as WorkOrderMaster, PurchaseOrderMaster etc.
 * <p>
 * The whole panel is occupied with a vertical splitter pane containing tree menu on the left and a holding panel on the right.
 * <p>
 * The {@link BaseFrame} can be used as is to serve a high level container for the master panel.
 * 
 * 
 * @author 01es
 * 
 */
public abstract class MasterPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private final JSplitPane spittterPane;
    private TreeMenuPanel treeMenuPanel;

    public MasterPanel() {
	super(new MigLayout("fill", "[grow,fill]", "[grow,fill]"));

	spittterPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
	spittterPane.setOneTouchExpandable(true);

	add(spittterPane);
    }

    protected void addTreeMenuPanel(final TreeMenuPanel treeMenuPanel) {
	this.treeMenuPanel = treeMenuPanel;
	spittterPane.setLeftComponent(treeMenuPanel);
	spittterPane.setRightComponent(treeMenuPanel.getTree().getHolder());
    }

    public MasterPanel setDividerLocation(final double dividerLocation) {
	spittterPane.setDividerLocation(dividerLocation);
	return this;
    }

    public MasterPanel setOneTouchExpandable(final boolean flag) {
	spittterPane.setOneTouchExpandable(flag);
	return this;
    }

    public TreeMenuPanel getTreeMenuPanel() {
	return treeMenuPanel;
    }

}
