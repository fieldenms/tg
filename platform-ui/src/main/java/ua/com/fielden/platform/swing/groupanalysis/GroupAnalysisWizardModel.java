package ua.com.fielden.platform.swing.groupanalysis;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.equery.AnalysisPropertyAggregationFunction;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reportquery.AggregationProperty;
import ua.com.fielden.platform.reportquery.DistributionDateProperty;
import ua.com.fielden.platform.reportquery.DistributionProperty;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportView;
import ua.com.fielden.platform.swing.analysis.IAnalysisWizardModel;
import ua.com.fielden.platform.swing.dynamicreportstree.AnalysisTree;
import ua.com.fielden.platform.swing.dynamicreportstree.EntitiesTreeColumn;
import ua.com.fielden.platform.swing.dynamicreportstree.TreePanel;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.treemodel.AnalysisTreeModel;
import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

public abstract class GroupAnalysisWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>, GR extends AbstractAnalysisReportView<T, DAO, ? extends GroupAnalysisWizardModel<T, DAO, GR>, ? extends GroupAnalysisReportModel<T, DAO>>> implements IAnalysisWizardModel {

    private final GR reportView;

    private final String analysisName;

    //Used for creating view in AnalysisReportMode.WIZARD mode.
    protected AnalysisTree configureTree;

    public GroupAnalysisWizardModel(final GR reportView, final String analysisName) {
	this.reportView = reportView;
	this.analysisName = analysisName;
    }

    protected abstract void updateReportView();

    private AnalysisTree getConfigureTree() {
	//TODO should override this when collectional properties will be supported in analysis reports.
	final AnalysisTreeModel treeModel = new AnalysisTreeModel(reportView.getModel().getEntityClass(), new IPropertyFilter() {

	    @Override
	    public boolean shouldExcludeProperty(final Class<?> ownerType, final Field property) {
		final Class<?> propertyType = property.getType();
		if ((AbstractEntity.class.isAssignableFrom(propertyType))
			&& (Modifier.isAbstract(propertyType.getModifiers()) || !AnnotationReflector.isAnnotationPresent(KeyType.class, propertyType))) {
		    return true;
		}
		if (Enum.class.isAssignableFrom(propertyType) //
			|| Collection.class.isAssignableFrom(propertyType)//
			|| ("key".equals(property.getName()) && (!AnnotationReflector.isAnnotationPresent(KeyTitle.class, ownerType) || !AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(ownerType))))//
			|| ("desc".equals(property.getName()) && !AnnotationReflector.isAnnotationPresent(DescTitle.class, ownerType))//
			|| property.isAnnotationPresent(Invisible.class) //
			|| property.isAnnotationPresent(Ignore.class) || property.isAnnotationPresent(CritOnly.class)) {
		    return true;
		}

		return false;
	    }

	    @Override
	    public boolean shouldBuildChildrenFor(final Class<?> ownerType, final Field property) {
		if (AbstractEntity.class.isAssignableFrom(property.getType()) && property.isAnnotationPresent(CritOnly.class)) {
		    return false;
		}
		return true;
	    }
	});
	final List<TreePath> distributionPaths = new ArrayList<TreePath>();
	final List<TreePath> aggregationPaths = new ArrayList<TreePath>();
	final Map<String, List<AnalysisPropertyAggregationFunction>> selectedValues = getAvailableAggregationProperties();
	for (final String key : selectedValues.keySet()) {
	    treeModel.setAggregationParameterFor(key, selectedValues.get(key));
	    aggregationPaths.add(treeModel.getPathFromPropertyName(key));
	}
	for (final IDistributedProperty distributionProperty : reportView.getAnalysisReportModel().getAvailableDistributionProperties()) {
	    if (distributionProperty instanceof DistributionDateProperty) {
		treeModel.setDistributionParameterFor(distributionProperty.getActualProperty(), ((DistributionDateProperty) distributionProperty).getDateFunction());
	    }
	    distributionPaths.add(treeModel.getPathFromPropertyName(distributionProperty.getActualProperty()));
	}
	final AnalysisTree tree = new AnalysisTree(treeModel);
	if (distributionPaths.size() > 0) {
	    tree.setCheckingPaths(distributionPaths.toArray(new TreePath[1]), EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex());
	}
	if (aggregationPaths.size() > 0) {
	    tree.setCheckingPaths(aggregationPaths.toArray(new TreePath[1]), EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex());
	}
	return tree;
    }

    private Map<String, List<AnalysisPropertyAggregationFunction>> getAvailableAggregationProperties() {
	final Map<String, List<AnalysisPropertyAggregationFunction>> selectedValues = new HashMap<String, List<AnalysisPropertyAggregationFunction>>();
	for (final IAggregatedProperty aggregationProperty : reportView.getAnalysisReportModel().getAvailableAggregationProperties()) {
	    List<AnalysisPropertyAggregationFunction> selectedFunctions = selectedValues.get(aggregationProperty.getActualProperty());
	    if (selectedFunctions == null) {
		selectedFunctions = new ArrayList<AnalysisPropertyAggregationFunction>();
		selectedValues.put(aggregationProperty.getActualProperty(), selectedFunctions);
	    }
	    if (!selectedFunctions.contains(aggregationProperty.getAggregationFunction())) {
		selectedFunctions.add(aggregationProperty.getAggregationFunction());
	    }
	}
	return selectedValues;
    }

    private Action createBuildAction() {
	return new AbstractAction("Build report") {

	    private static final long serialVersionUID = 3242529561902928130L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Builds " + analysisName + " report");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (configureTree != null) {
		    configureTree.stopEditing();
		}
		try {
		    reportView.setMode(ReportMode.REPORT, false);
		    updateReportView();
		} catch (final IllegalStateException ex) {
		    JOptionPane.showMessageDialog(reportView, "Please choose distribution and aggregation properties.", "Information", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	};
    }

    private Action createCancelAction() {
	return new AbstractAction("Cancel") {

	    private static final long serialVersionUID = 3242529561902928130L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Cancel " + analysisName + " report modifactions");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		setEnabled(reportView.getAnalysisReportModel().canRestoreReportView());
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		try {
		    reportView.setMode(ReportMode.REPORT, true);

		} catch (final IllegalStateException ex) {
		    JOptionPane.showMessageDialog(reportView, "It's imposible to restore previous view.", "Information", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	};
    }

    @Override
    public void createWizardView(final Container container) throws IllegalStateException {
	container.removeAll();
	container.setLayout(new MigLayout("fill, insets 0", "[fill]", "[][grow,fill][]"));
	container.add(DummyBuilder.label("Choose distribution and aggregation properties"), "wrap");
	configureTree = getConfigureTree();
	final TreePanel treePanel = new TreePanel(configureTree);
	container.add(treePanel, "wrap");
	final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[r,grow][,r]", "[fill,grow]"));
	buttonPanel.add(new JButton(createBuildAction()));
	buttonPanel.add(new JButton(createCancelAction()));
	container.add(buttonPanel);
	container.invalidate();
	container.validate();
	container.repaint();
    }

    public List<IAggregatedProperty> getSelectedAggregationProperties() {
	final List<IAggregatedProperty> selectedAggregationProperties = new ArrayList<IAggregatedProperty>();
	if (configureTree == null) {
	    return selectedAggregationProperties;
	}
	final AnalysisTreeModel treeModel = configureTree.getAnalysisTreeModel();
	for (final TreePath treePath : configureTree.getCheckingPaths(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex())) {
	    final String propertyPath = treeModel.getPropertyNameFor((DefaultMutableTreeNode) treePath.getLastPathComponent());
	    final TitledObject title = (TitledObject) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
	    for (final AnalysisPropertyAggregationFunction function : treeModel.getAggregationParameterFor(propertyPath)) {
		selectedAggregationProperties.add(new AggregationProperty(title.getTitle(), title.getDesc(), propertyPath, function));
	    }
	}
	return selectedAggregationProperties;
    }

    public List<IDistributedProperty> getSelectedDistributionProperties() {
	final List<IDistributedProperty> selectedDistributionProperties = new ArrayList<IDistributedProperty>();
	if (configureTree == null) {
	    return selectedDistributionProperties;
	}
	final AnalysisTreeModel treeModel = configureTree.getAnalysisTreeModel();
	for (final TreePath treePath : configureTree.getCheckingPaths(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex())) {
	    final String propertyPath = treeModel.getPropertyNameFor((DefaultMutableTreeNode) treePath.getLastPathComponent());
	    final TitledObject title = (TitledObject) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
	    if (Date.class.isAssignableFrom(title.getType())) {
		selectedDistributionProperties.add(new DistributionDateProperty(title.getTitle(), title.getDesc(), propertyPath, treeModel.getDistributionParameterFor(propertyPath)));
	    } else {
		selectedDistributionProperties.add(new DistributionProperty(title.getTitle(), title.getDesc(), propertyPath));
	    }
	}
	return selectedDistributionProperties;
    }

    public boolean isValidToBuildReportView() {
	return configureTree != null && configureTree.getCheckingPaths(EntitiesTreeColumn.CRITERIA_COLUMN.getColumnIndex()).length > 0
	&& configureTree.getCheckingPaths(EntitiesTreeColumn.TABLE_HEADER_COLUMN.getColumnIndex()).length > 0;
    }

    protected GR getReportView() {
	return reportView;
    }

}
