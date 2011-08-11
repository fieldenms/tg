package ua.com.fielden.platform.swing.treetable;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;

/**
 * The panel that holds the {@link SecurityTreeTable} and {@link FilterControl} to filter the tree table. Implemented only for testing purpose.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenViewer extends BlockingIndefiniteProgressLayer {

    private static final long serialVersionUID = 6944290284737726475L;

    // tree table that contains ISecurityToken as TreeTableNodes and UserRole columns
    private final FilterableTreeTablePanel<SecurityTreeTable> securityTable;

    // actions for loading and saving data
    private final Action loadAction;
    private final Action saveAction;

    /**
     * Creates new {@link SecurityTokenViewer} for the given {@link SecurityTokenViewerModel} instance
     * 
     * @param model
     */
    public SecurityTokenViewer(final SecurityTokenViewerModel model) {
	super(null, "");

	// creates the panel that is the view of the blocking panel
	final JPanel panel = new JPanel(new MigLayout("fill", "[]push[]", "[fill,grow][]"));
	setView(panel);
	// creating load and save buttons
	loadAction = model.getLoadAction(this);
	saveAction = model.getSaveAction(this);
	final JButton loadButton = new JButton(loadAction);
	final JButton saveButton = new JButton(saveAction);

	// creating security tree table and filter control to filter it
	securityTable = new FilterableTreeTablePanel<SecurityTreeTable>(new SecurityTreeTable(new FilterableTreeTableModel(model.getTreeTableModel())), new WordFilter(), "find token");

	panel.add(securityTable, "span 2, growx, wrap");
	panel.add(loadButton);
	panel.add(saveButton);

	// component listener is responsible for data loading after the blocking panel was first time resized
	addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(final ComponentEvent e) {
		loadAction.actionPerformed(null);

		final ComponentListener refToThis = this;
		SecurityTokenViewer.this.removeComponentListener(refToThis);
	    }
	});
    }

}
