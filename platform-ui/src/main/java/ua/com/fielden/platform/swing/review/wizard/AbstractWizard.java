package ua.com.fielden.platform.swing.review.wizard;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.dynamicreportstree.AbstractTree;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.dynamicreportstree.TreePanel;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.CloseReportOptions;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;

/**
 * Abstract wizard panel for customizing dynamic entity review.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractWizard<MODEL extends AbstractWizardModel, TREE extends AbstractTree> extends BasePanel {
    private static final long serialVersionUID = 1L;

    private final MODEL wizardModel;

    private final TREE tree;

    private final JButton buildButton, cancelButton;

    private boolean wasClosingCanceled = false;

    public AbstractWizard(final MODEL wizardModel, final Action buildAction, final Action cancelAction) {
	this.wizardModel = wizardModel;

	setLayout(new MigLayout("fill, insets 5", "[grow, fill]", "[][grow, fill][]"));

	tree = createTree();

	// // init tree:////
	selectCriteriaProperties(wizardModel.getSelectedCriteriaProperties());
	selectFetchProperties(wizardModel.getSelectedTableHeaders());

	// After selecting some nodes the tree structure could be extended (some nodes could be loaded lazily). So, paths enablement should be updated ->
	tree.providePathsEnablement();

	tree.addTreeCheckingListener(createCriteriaTreeCheckingListener(), EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex());
	tree.addTreeCheckingListener(createFetchTreeCheckingListener(), EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex());
	// // end init tree:////

	tree.checkUnionPaths(EntitiesTreeColumn.CRITERIA_COLUMN, true);

	cancelButton = new JButton(cancelAction);
	buildButton = new JButton(buildAction);
	final JCheckBox autoRunCheckBox = new JCheckBox("Run automatically");
	autoRunCheckBox.setSelected(getWizardModel().isAutoRun());
	autoRunCheckBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(final ItemEvent e) {
		final int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
		    getWizardModel().setAutoRun(true);
		} else {
		    getWizardModel().setAutoRun(false);
		}

	    }

	});

	add(DummyBuilder.label("Choose properties for selection criteria and result set"), "wrap");

	add(new TreePanel(tree), "wrap");

	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[][][]30:push[fill, :100:][fill, :100:]", "[c]"));
	actionPanel.add(DummyBuilder.label("Columns"));
	actionPanel.add(new JSpinner(wizardModel.getSpinnerModel()));
	actionPanel.add(autoRunCheckBox);
	actionPanel.add(buildButton);
	actionPanel.add(cancelButton);
	add(actionPanel);
	tree.addHierarchyListener(new HierarchyListener() {

	    @Override
	    public void hierarchyChanged(final HierarchyEvent e) {
		final long flags = e.getChangeFlags();
		if ((flags & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED) {
		    tree.requestFocusInWindow();
		}

	    }

	});
    }

    protected abstract TREE createTree();

    private String createTitlesFromPath(final TreePath path) {
	final TreeModel treeModel = tree.getModel().getOriginModel();
	if (treeModel instanceof EntitiesTreeModel) {
	    return ((EntitiesTreeModel) treeModel).getPropertyNameFor((DefaultMutableTreeNode) path.getLastPathComponent());
	} else {
	    return null;
	}
    }

    public void selectCriteriaProperties(final Collection<String> properties) {
	selectTreeItems(properties, EntitiesTreeColumn.CRITERIA_COLUMN);
    }

    public void selectFetchProperties(final Collection<String> properties) {
	selectTreeItems(properties, EntitiesTreeColumn.TABLE_HEADER_COLUMN);
    }

    public MODEL getWizardModel() {
	return wizardModel;
    }

    private void selectTreeItems(final Collection<String> properties, final EntitiesTreeColumn column) {
	final List<TreePath> pathsToSelect = new ArrayList<TreePath>();
	for (final String propertyName : properties) {
	    TreePath treePath = ((EntitiesTreeModel) tree.getModel().getOriginModel()).getPathFromPropertyName(propertyName);
	    if (treePath == null) { // possibly corresponding node was not loaded (e.g. Vehicle.replacing.DUMMY_TITLED_OBJECT was loaded, but Vehicle.replacing.replacedBy.status
		// should be selected)
		tree.getEntitiesTreeModel().load(createStrings(propertyName, getWizardModel().getEntityClass()));
		treePath = ((EntitiesTreeModel) tree.getModel().getOriginModel()).getPathFromPropertyName(propertyName);
	    }
	    if (treePath == null) {
		throw new IllegalArgumentException("The titles might be corrupted.");
	    }
	    pathsToSelect.add(treePath);
	}
	tree.setCheckingPaths(pathsToSelect.toArray(new TreePath[pathsToSelect.size()]), column.getColumnIndex());
    }

    private static List<String> createStrings(final String propertyName, final Class<?> classToAdd) {
	final List<String> strings = new ArrayList<String>();
	strings.add(EntitiesTreeModel.ROOT_CAPTION);
	strings.add(classToAdd.getSimpleName());
	strings.addAll(Arrays.asList(propertyName.split(Reflector.DOT_SPLITTER)));
	return strings;
    }

    /**
     * Extracts propertyNames from titled object path.
     * 
     * @param titledObjectPath
     *            convenience -> "Entities", AE clazz, first property, ... last property
     * @return
     */
    public static List<String> createStrings(final List<TitledObject> titledObjectPath) {
	final List<String> strings = new ArrayList<String>();
	for (final TitledObject to : titledObjectPath) {
	    if (to.getObject() == null) { // first node
		strings.add(EntitiesTreeModel.ROOT_CAPTION);
	    } else if (to.getObject() instanceof Class) { // second node
		strings.add(((Class<?>) to.getObject()).getSimpleName());
	    } else if (to.getObject() instanceof String) {
		strings.add((String) to.getObject());
	    } else {
		throw new RuntimeException("Title object path convenience was broken. [" + titledObjectPath + "]");
	    }
	}
	return strings;
    }

    @Override
    public String getInfo() {
	return null;
    }

    private TreeCheckingListener createCriteriaTreeCheckingListener() {
	return new TreeCheckingListener() {

	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		final TreePath path = e.getPath();
		final String propertyPath = createTitlesFromPath(path);
		if (isUnionEntityPath(path)) {
		    if (e.isCheckedPath()) {
			wizardModel.removeExcludeProperty(propertyPath);
		    } else {
			wizardModel.addExcludeProperty(propertyPath);
		    }
		} else {
		    if (e.isCheckedPath()) {
			wizardModel.addCriteriaProperty(propertyPath);
		    } else {
			wizardModel.removeCriteriaProperty(propertyPath);
		    }
		}
	    }

	};
    }

    private TreeCheckingListener createFetchTreeCheckingListener() {
	return new TreeCheckingListener() {

	    @Override
	    public void valueChanged(final TreeCheckingEvent e) {
		final TreePath path = e.getPath();
		final String propertyPath = createTitlesFromPath(path);
		if (e.isCheckedPath()) {
		    wizardModel.addTableHeader(propertyPath);
		} else {
		    wizardModel.removeTableHeader(propertyPath);
		}
	    }
	};
    }

    /**
     * Returns value that indicates whether path is for entity property declared in the class that extends {@link AbstractUnionEntity}
     * 
     * @param path
     * @return
     */
    private boolean isUnionEntityPath(final TreePath path) {
	final TitledObject lastNode = TitledObject.extractTitleFromTreeNode(path.getLastPathComponent());
	final TitledObject parentNode = TitledObject.extractTitleFromTreeNode(path.getParentPath().getLastPathComponent());
	if (lastNode.getType() != null && AbstractEntity.class.isAssignableFrom(lastNode.getType()) && parentNode.getType() != null
		&& AbstractUnionEntity.class.isAssignableFrom(parentNode.getType())) {
	    return true;
	}
	return false;
    }

    @Override
    public ICloseGuard canClose() {
	final ICloseGuard result = super.canClose();
	if (result != null) {
	    return result;
	}
	if (wizardModel.getModelBuilder().getPreviousEntityReview() != null) {
	    final ICloseGuard closeGuard = wizardModel.getModelBuilder().getPreviousEntityReview().canClose();
	    wasClosingCanceled = wizardModel.getModelBuilder().getPreviousEntityReview().wasClosingCanceled();
	    return closeGuard;
	}
	final CloseReportOptions chosenOption = wizardModel.getModelBuilder().canClose("");
	wasClosingCanceled = CloseReportOptions.CANCEL == chosenOption;
	return chosenOption == CloseReportOptions.CANCEL ? this : null;
    }

    /**
     * Returns value that indicates whether closing was canceled or not.
     * 
     * @return
     */
    public boolean wasClosingCanceled() {
	return wasClosingCanceled;
    }

    @Override
    public void close() {
	if (wizardModel.getModelBuilder().getPreviousEntityReview() != null) {
	    wizardModel.getModelBuilder().getPreviousEntityReview().close();
	}
    }

    @Override
    public String whyCannotClose() {
	return wizardModel.getModelBuilder().whyCannotClose();
    }

}
