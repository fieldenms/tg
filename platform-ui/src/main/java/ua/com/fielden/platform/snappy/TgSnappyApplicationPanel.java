package ua.com.fielden.platform.snappy;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
import ua.com.fielden.platform.treemodel.IPropertyFilter;
import ua.com.fielden.snappy.pswing.PSwingCanvas;
import ua.com.fielden.snappy.ui.SnappyApplicationPanel;
import ua.com.fielden.snappy.ui.StatusBar;
import ua.com.fielden.snappy.view.block.BlockNode;
import ua.com.fielden.snappy.view.block.SegmentedExpandableBlock;
import ua.com.fielden.snappy.view.block.SingletonBlock;
import ua.com.fielden.snappy.view.blocks.properties.PropertyBlock;
import ua.com.fielden.snappy.view.blocks.toplevel.RuleBlock;
import ua.com.fielden.uds.designer.zui.event.IntersectionBasedDragEventHandler;
import ua.com.fielden.uds.designer.zui.event.IntersectionBasedDragEventHandler.DoubleClicker;
import ua.com.fielden.uds.designer.zui.event.IntersectionBasedDragEventHandler.ForcedDehighlighter;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;

import com.jidesoft.swing.JideTabbedPane;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;

/**
 * A panel for collecting all snappy components together.
 * 
 * @author Jhou
 * 
 */
public class TgSnappyApplicationPanel extends SnappyApplicationPanel<TgSnappyComponentsActivator> {
    private static final long serialVersionUID = 1L;

    /**
     * A constructor to create TG-specific snappy application panel that requires "applicationModel" to be specified and "blockingIndefiniteProgressPane". If frame titling is
     * necessary (changing titles after file actions etc.) - "frame" parameter should be specified.
     * 
     * @param applicationModel
     * @param blockingIndefiniteProgressPane
     * @param frame
     */
    public TgSnappyApplicationPanel(final TgSnappyApplicationModel applicationModel, final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane, final JFrame frame) {
	super(applicationModel);

	final JPanel resultsPanel = new JPanel(new MigLayout("fill"));
	final SnappyButtonPanel buttonPanel = new SnappyButtonPanel(new MigLayout("fill, insets 5"), blockingIndefiniteProgressPane);
	resultsPanel.add(buttonPanel, "dock north, wrap");
	final JTabbedPane rulesTabbedPane = createRulesTabbedPane();
	rulesTabbedPane.setPreferredSize(new Dimension(rulesTabbedPane.getPreferredSize().width, 0));
	resultsPanel.add(rulesTabbedPane, "dock center");

	getCanvas().setMinimumSize(new Dimension(0, 100));
	final JSplitPane viewResultsSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, getCanvas(), resultsPanel);
	viewResultsSplitter.setResizeWeight(1.0D);
	viewResultsSplitter.setOneTouchExpandable(true);
	viewResultsSplitter.resetToPreferredSizes();

	rulesTabbedPane.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(final ComponentEvent e) {
		final int bottomSplitPanePartHeight = viewResultsSplitter.getHeight() - viewResultsSplitter.getDividerLocation() - viewResultsSplitter.getDividerSize();
		if (bottomSplitPanePartHeight < resultsPanel.getPreferredSize().getHeight()) {
		    viewResultsSplitter.resetToPreferredSizes();
		}
	    }
	});

	// TODO CritOnly, ResultOnly and Ignore annotation were not taken into account. Please note that.
	final SnappyEntitiesTree entitiesTree = new SnappyEntitiesTree(new EntitiesTreeModel(applicationModel.getEntityClasses(), new IPropertyFilter() {
	    @Override
	    public boolean shouldExcludeProperty(final Class<?> ownerType, final Field property) {
		return Enum.class.isAssignableFrom(property.getType()); // exclude temporarily enumeration properties, until support of enumeration types in Snappy will be
		// provided.
	    }

	    @Override
	    public boolean shouldBuildChildrenFor(final Class<?> ownerType, final Field property) {
		// TODO Auto-generated method stub
		return true;
	    }
	}), rulesTabbedPane, applicationModel);
	final JSplitPane entitiesSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new EntitiesTreePanel(entitiesTree, blockingIndefiniteProgressPane), viewResultsSplitter);
	entitiesSplitter.setOneTouchExpandable(true);
	add(entitiesSplitter);

	final StatusBar statusBar = new StatusBar();
	add(statusBar, "dock south");
	getApplicationModel().setComponentsActivator(new TgSnappyComponentsActivator(frame, (PSwingCanvas) GlobalObjects.canvas, entitiesTree, statusBar, rulesTabbedPane, buttonPanel, blockingIndefiniteProgressPane));
    }

    @Override
    protected void initializeCanvas(final PCanvas canvas) {
	super.initializeCanvas(canvas);
	canvas.getLayer().removeInputEventListener(getDefaultDragEventHandler());
	canvas.getLayer().addInputEventListener(new IntersectionBasedDragEventHandler(canvas.getLayer(), new ForcedDehighlighter() {
	    public boolean shouldDehighlight(final IBasicNode node) {
		return true;
	    }
	}, new DoubleClicker() {
	    @Override
	    public void doubleClickAction(final PNode pickedNode) {
		if (pickedNode instanceof SingletonBlock && !(pickedNode instanceof RuleBlock)) {
		    final SingletonBlock<?> blockToDelete = ((SingletonBlock<?>) pickedNode);
		    // only the blocks
		    final boolean couldBeDeleted = !(blockToDelete instanceof PropertyBlock)
			    || (blockToDelete instanceof PropertyBlock && !((PropertyBlock<?>) blockToDelete).isConditionRoot());
		    if (couldBeDeleted) {
			if (blockToDelete.isSnapped()) {
			    blockToDelete.getSlot().snapOut();
			} else {
			    blockToDelete.removeFromParent();
			}
		    } else {
			getApplicationModel().getComponentsActivator().locatePropertyInTree(blockToDelete);
		    }
		} else if (pickedNode instanceof SegmentedExpandableBlock) {
		    getApplicationModel().getComponentsActivator().locatePropertyInTree((BlockNode<?>) pickedNode);
		}
	    }
	}));
    }

    @Override
    public TgSnappyApplicationModel getApplicationModel() {
	return (TgSnappyApplicationModel) super.getApplicationModel();
    }

    private JTabbedPane createRulesTabbedPane() {
	final JideTabbedPane tabbedPane = new JideTabbedPane();
	tabbedPane.setShowCloseButtonOnSelectedTab(false);
	tabbedPane.setShowTabButtons(true);
	tabbedPane.setShowCloseButtonOnTab(false);
	tabbedPane.setShowCloseButton(false);
	tabbedPane.setBoldActiveTab(true);
	tabbedPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
	tabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
	return tabbedPane;
    }

    public class SnappyButtonPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public SnappyButtonPanel(final MigLayout layout, final BlockingIndefiniteProgressPane blockingIndefiniteProgressPane) {
	    super(layout);
	    add(this.openNewButton = new JButton(getApplicationModel().createOpenAction(blockingIndefiniteProgressPane)));
	    add(this.saveButton = new JButton(getApplicationModel().createSaveAction(blockingIndefiniteProgressPane)));
	    add(this.discardButton = new JButton(getApplicationModel().createDiscardAction(blockingIndefiniteProgressPane)), "gap 30");
	    add(this.reloadRulesButton = new JButton(getApplicationModel().createReloadAction(blockingIndefiniteProgressPane)), "push");
	    add(this.runButton = new JButton(getApplicationModel().createRunAction(blockingIndefiniteProgressPane)));
	}

	private final JButton openNewButton, saveButton, reloadRulesButton, discardButton, runButton;

	public JButton getOpenNewButton() {
	    return openNewButton;
	}

	public JButton getSaveButton() {
	    return saveButton;
	}

	public JButton getReloadRulesButton() {
	    return reloadRulesButton;
	}

	public JButton getDiscardButton() {
	    return discardButton;
	}

	public JButton getRunButton() {
	    return runButton;
	}
    }

}
