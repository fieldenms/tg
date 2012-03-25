package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

/**
 * The base class for implementing menu items, which require configuration support. Mainly used for representing entity center related menu items.
 *
 * @author TG Team
 *
 */
public class MiWithConfigurationSupport<T extends AbstractEntity<?>> extends MiWithVisibilityProvider<DynamicReportWrapper<T>> {

    private static final long serialVersionUID = -4608369671314218118L;

    //Tree menu item related properties.
    private final TreeMenuWithTabs<?> treeMenu;

    //Entity centre related properties.
    private final Class<T> entityType;
    private final IGlobalDomainTreeManager gdtm;
    private final EntityFactory entityFactory;
    private final IEntityMasterManager masterManager;
    private final ICriteriaGenerator criteriaGenerator;

    /**
     * Creates new {@link MiWithConfigurationSupport} instance and generates all his children reports. Unlike parent report, Children reports can be remove.
     *
     * @param visibilityProvider
     * @param entityType
     * @param name
     * @param gdtm
     * @param entityFactory
     * @param criteriaGenerator
     */
    public MiWithConfigurationSupport(//
	    //Menu item related parameters
	    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final ITreeMenuItemVisibilityProvider visibilityProvider,//
	    final Class<T> entityType,//
	    final String name,//
	    final IGlobalDomainTreeManager gdtm,//
	    final EntityFactory entityFactory,//
	    final IEntityMasterManager masterManager,//
	    final ICriteriaGenerator criteriaGenerator) {
	super(new DynamicReportWrapper<T>(caption, description, treeMenu, entityType, name, gdtm, entityFactory, masterManager, criteriaGenerator), visibilityProvider);
	this.treeMenu = treeMenu;
	this.entityType = entityType;
	this.gdtm = gdtm;
	this.entityFactory = entityFactory;
	this.masterManager = masterManager;
	this.criteriaGenerator = criteriaGenerator;
	scanForNonPrincipleReports();
    }

    public final Class<T> getEntityType() {
	return entityType;
    }

    public final IGlobalDomainTreeManager getGlobalDomainTreeManager(){
	return gdtm;
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    public IEntityMasterManager getMasterManager() {
	return masterManager;
    }

    public ICriteriaGenerator getCriteriaGenerator() {
	return criteriaGenerator;
    }

    public TreeMenuWithTabs<?> getTreeMenu() {
	return treeMenu;
    }

    /**
     * Generates children ad hoc reports.
     */
    private void scanForNonPrincipleReports() {
	//TODO must be implemented when the global domain tree manager will allow to retriever the list of non principle entity centres associated with this one.
	//	final String principleKey = getView().getDynamicCriteriaModelBuilder().getKey();
	//	for (final String nonPrincipleName : centerController.getNonPrincipleCenters(principleKey)) {
	//	    final DynamicCriteriaModelBuilder<T, DAO, R> newModelBuilder = getDynamicCriteriaModelBuilderFor(centerController.generateKeyForNonPrincipleCenter(principleKey, nonPrincipleName), nonPrincipleName);
	//	    final MiSaveAsConfiguration<T, DAO, R> newTreeMenuItem = new MiSaveAsConfiguration<T, DAO, R>(nonPrincipleName, getView().getInfo(), newModelBuilder, treeMenu);
	//	    newTreeMenuItem.getView().setSaveAction(newModelBuilder.createSaveAction());
	//	    newTreeMenuItem.getView().setSaveAsAction(createSaveAsAction(newModelBuilder));
	//	    newTreeMenuItem.getView().setRemoveAction(createRemoveAction(newTreeMenuItem));
	//	    newTreeMenuItem.getView().setPanelBuilder(createAnalysisActionPanel(newTreeMenuItem));
	//	    addItem(newTreeMenuItem);
	//	}
    }

    //    /**
    //     * Creates analysis (aggregating and lifecycle) action panel, that holds actions : "addAggregationAnalysis", "removeAnalysis"(any) and, possibly, "addLifecycleAnalysis".
    //     * "Add lifecycle analysis" action appears only in case when at least one "monitoring" property exists inside entity class.
    //     *
    //     * @return
    //     */
    //    public ActionPanelBuilder createAnalysisActionPanel(final TreeMenuItem<?> tmi) {
    //	final ActionPanelBuilder apb = new ActionPanelBuilder()//
    //	.addButton(createAddAnalysisAction(tmi, AnalysisReportType.PIVOT, "Add pivot analysis", "Add pivot analysis report", ResourceLoader.getIcon("images/table_add.png"), ResourceLoader.getIcon("images/table_add.png")))//
    //	.addButton(createAddAnalysisAction(tmi, AnalysisReportType.ANALYSIS, "Add analysis", "Add analysis report", ResourceLoader.getIcon("images/chart-add.png"), ResourceLoader.getIcon("images/chart-add.png")));
    //	if (!Finder.findLifecycleProperties(resultantEntityClass).isEmpty()) {
    //	    apb.addButton(createAddAnalysisAction(tmi, AnalysisReportType.LIFECYCLE, "Add lifecycle analysis", "Add lifecycle report", ResourceLoader.getIcon("images/chart-add.png"), ResourceLoader.getIcon("images/chart-add.png")));//
    //	}
    //	apb.addButton(createRemoveAnalysisAction(tmi)); //
    //	return apb;
    //    }


    //    /**
    //     * Creates "Save as" action that allows to save current report in to the new file.
    //     *
    //     * @param modelBuilder
    //     * @return
    //     */
    //    public ActionChanger<?> createSaveAsAction(final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
    //	modelBuilder.setSaveAsContract(createSaveAsContract());
    //	return new ActionChanger<Void>("Save As") {
    //
    //	    private static final long serialVersionUID = -8605910577786227216L;
    //
    //	    {
    //		putValue(Action.SHORT_DESCRIPTION, "Save a copy");
    //		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
    //	    }
    //
    //	    @Override
    //	    protected Void action(final ActionEvent e) throws Exception {
    //		return null;
    //	    }
    //
    //	    @Override
    //	    protected void postAction(final Void value) {
    //		super.postAction(value);
    //		modelBuilder.saveAsCriteria(false);
    //	    }
    //
    //	};
    //    }
    //
    //    protected ISaveAsContract createSaveAsContract() {
    //	return new ISaveAsContract() {
    //
    //	    private final SaveReportDialog saveReportDialog;
    //	    {
    //		saveReportDialog = new SaveReportDialog(new SaveReportDialogModel(getView().getDynamicCriteriaModelBuilder().getKey(), centerController));
    //	    }
    //
    //	    @Override
    //	    public void afterSave(final boolean isClosing) {
    //		final DynamicCriteriaModelBuilder<T, DAO, R> newCriteriaModelBuilder = getDynamicCriteriaModelBuilderFor(getKeyToSave(), saveReportDialog.getEnteredFileName());
    //		final MiSaveAsConfiguration<T, DAO, R> newTreeMenuItem = new MiSaveAsConfiguration<T, DAO, R>(saveReportDialog.getEnteredFileName(), getView().getInfo(), newCriteriaModelBuilder, treeMenu);
    //		newTreeMenuItem.getView().setSaveAction(newCriteriaModelBuilder.createSaveAction());
    //		newTreeMenuItem.getView().setSaveAsAction(createSaveAsAction(newCriteriaModelBuilder));
    //		newTreeMenuItem.getView().setRemoveAction(createRemoveAction(newTreeMenuItem));
    //		newTreeMenuItem.getView().setPanelBuilder(createAnalysisActionPanel(newTreeMenuItem));
    //		addItem(newTreeMenuItem);
    //		treeMenu.getModel().getOriginModel().reload(MiWithConfigurationSupport.this);
    //		if (!isClosing) {
    //		    treeMenu.activateOrOpenItem(newTreeMenuItem);
    //		}
    //	    }
    //
    //	    @Override
    //	    public boolean beforeSave() {
    //		return SaveReportOptions.APPROVE.equals(saveReportDialog.showDialog());
    //	    }
    //
    //	    @Override
    //	    public String getKeyToSave() {
    //		return centerController.generateKeyForNonPrincipleCenter(getView().getDynamicCriteriaModelBuilder().getKey(), saveReportDialog.getEnteredFileName());
    //
    //	    }
    //
    //	};
    //    }
    //
    //    /**
    //     * Creates "remove" action that allows to remove children ad hoc reports.
    //     *
    //     * @param reportItem
    //     * @return
    //     */
    //    public ActionChanger<?> createRemoveAction(final MiSaveAsConfiguration<T, DAO, R> reportItem) {
    //	return new ActionChanger<Void>("Delete") {
    //
    //	    private static final long serialVersionUID = 4540755873256648991L;
    //
    //	    {
    //		putValue(Action.SHORT_DESCRIPTION, "Delete current report");
    //		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
    //	    }
    //
    //	    @Override
    //	    protected boolean preAction() {
    //		if (!super.preAction()) {
    //		    return false;
    //		}
    //		final Result configurationresult = getCenterController().canRemove(reportItem.getDynamicCriteriaModelBuilder().getKey());
    //		if (!configurationresult.isSuccessful()) {
    //		    JOptionPane.showMessageDialog(null, configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
    //		    return false;
    //		}
    //
    //		return JOptionPane.showConfirmDialog(getView(), "A copy of the report will be deleted. Proceed?", "Delete Report", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    //	    }
    //
    //	    @Override
    //	    protected Void action(final ActionEvent e) throws Exception {
    //		return null;
    //	    }
    //
    //	    @Override
    //	    protected void postAction(final Void value) {
    //		super.postAction(value);
    //		reportItem.getDynamicCriteriaModelBuilder().remove();
    //		treeMenu.closeCurrentTab();
    //		treeMenu.getModel().getOriginModel().removeNodeFromParent(reportItem);
    //	    }
    //
    //	};
    //    }
    //
    //    public final Command<Void> createAddAnalysisAction(final TreeMenuItem<?> parentTreeItem, final IAnalysisReportType reportType, final String name, final String shortDescription, final Icon largeIcon, final Icon smallIcon) {
    //	return new Command<Void>(name) {
    //
    //	    private static final long serialVersionUID = 1916078804942683757L;
    //
    //	    private final AddTabDialog addTabDialog;
    //
    //	    {
    //		putValue(Action.SHORT_DESCRIPTION, shortDescription);
    //		putValue(Action.LARGE_ICON_KEY, largeIcon);
    //		putValue(Action.SMALL_ICON, smallIcon);
    //		addTabDialog = new AddTabDialog(new AddTabDialogModel());
    //	    }
    //
    //	    @Override
    //	    protected boolean preAction() {
    //		final boolean prevRes = super.preAction();
    //		if (!prevRes) {
    //		    return prevRes;
    //		}
    //		if (!(parentTreeItem.getView() instanceof DynamicReportWrapper)) {
    //		    return false;
    //		}
    //		final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder = ((DynamicReportWrapper) parentTreeItem.getView()).getDynamicCriteriaModelBuilder();
    //		final String key = modelBuilder.getKey();
    //		final Result configurationresult = centerController.canAddAnalysis(key);
    //		if (!configurationresult.isSuccessful()) {
    //		    JOptionPane.showMessageDialog(getView(), configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
    //		    return false;
    //		}
    //		return AddTabOptions.ADD_TAB.equals(addTabDialog.showDialog(SwingUtilities.getWindowAncestor(parentTreeItem.getView())));
    //	    }
    //
    //	    @Override
    //	    protected Void action(final ActionEvent e) throws Exception {
    //		return null;
    //	    }
    //
    //	    @Override
    //	    protected void postAction(final Void value) {
    //		super.postAction(value);
    //		if (!(parentTreeItem.getView() instanceof DynamicReportWrapper)) {
    //		    return;
    //		}
    //		final DynamicEntityReview<T, DAO, R> review = ((DynamicReportWrapper) parentTreeItem.getView()).getView();
    //		if (review instanceof DynamicEntityReviewWithTabs) {
    //		    final DynamicEntityReviewWithTabs<T, DAO, R> reviewWithTabs = (DynamicEntityReviewWithTabs<T, DAO, R>) review;
    //		    if (reviewWithTabs.existsTab(addTabDialog.getEnteredTabName())) {
    //			reviewWithTabs.selectTab(addTabDialog.getEnteredTabName());
    //		    } else {
    //			reviewWithTabs.addNewAnalysisTabSheet(addTabDialog.getEnteredTabName(), reportType);
    //			if (parentTreeItem instanceof MiSaveAsConfiguration) {
    //			    ((MiSaveAsConfiguration<T, DAO, R>) parentTreeItem).addItem(new TreeMenuItemWrapper<T, DAO, R>(addTabDialog.getEnteredTabName(), parentTreeItem.isGroupItem()));
    //			    treeMenu.getModel().getOriginModel().reload(parentTreeItem);
    //			}
    //		    }
    //		}
    //	    }
    //	};
    //    }
    //
    //    /**
    //     * Returns action that removes analysis report.
    //     *
    //     * @param parentTreeItem
    //     * @return
    //     */
    //    public Command<Void> createRemoveAnalysisAction(final TreeMenuItem<?> parentTreeItem) {
    //	return new Command<Void>("Remove analysis") {
    //
    //	    private static final long serialVersionUID = -7571276791865720038L;
    //
    //	    {
    //		putValue(Action.SHORT_DESCRIPTION, "Remove report");
    //		putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/chart-remove.png"));
    //		putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/chart-remove.png"));
    //	    }
    //
    //	    @Override
    //	    protected boolean preAction() {
    //		final boolean result = super.preAction();
    //		if (!result) {
    //		    return false;
    //		}
    //		if (!(parentTreeItem.getView() instanceof DynamicReportWrapper)) {
    //		    return false;
    //		}
    //		final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder = ((DynamicReportWrapper) parentTreeItem.getView()).getDynamicCriteriaModelBuilder();
    //		final String key = modelBuilder.getKey();
    //		final Result configurationresult = centerController.canRemoveAnalysis(key);
    //		if (!configurationresult.isSuccessful()) {
    //		    JOptionPane.showMessageDialog(getView(), configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
    //		    return false;
    //		}
    //		return true;
    //	    }
    //
    //	    @Override
    //	    protected Void action(final ActionEvent e) throws Exception {
    //		return null;
    //	    }
    //
    //	    @Override
    //	    protected void postAction(final Void value) {
    //		super.postAction(value);
    //		if (!(parentTreeItem.getView() instanceof DynamicReportWrapper)) {
    //		    return;
    //		}
    //		final DynamicEntityReview<T, DAO, R> review = ((DynamicReportWrapper) parentTreeItem.getView()).getView();
    //		if (review instanceof DynamicEntityReviewWithTabs) {
    //		    final DynamicEntityReviewWithTabs<T, DAO, R> reviewWithTabs = (DynamicEntityReviewWithTabs<T, DAO, R>) review;
    //		    final String name = reviewWithTabs.getSelectedTabTitle();
    //		    final JComponent component = reviewWithTabs.removeAnalysisTabSheet(name);
    //		    if (component != null && parentTreeItem instanceof MiSaveAsConfiguration) {
    //			final TreeMenuItemWrapper<T, DAO, R> itemToRemove = getChildByName((MiSaveAsConfiguration<T, DAO, R>) parentTreeItem, name);
    //			if (itemToRemove == null) {
    //			    return;
    //			}
    //			treeMenu.getModel().getOriginModel().removeNodeFromParent(itemToRemove);
    //		    }
    //		}
    //	    }
    //
    //	    private TreeMenuItemWrapper<T, DAO, R> getChildByName(final MiSaveAsConfiguration<T, DAO, R> report, final String name) {
    //		for (int childIndex = 0; childIndex < report.getChildCount(); childIndex++) {
    //		    final TreeNode node = report.getChildAt(childIndex);
    //		    if (node instanceof TreeMenuItemWrapper && ((TreeMenuItemWrapper<T, DAO, R>) node).toString().equals(name)) {
    //			return (TreeMenuItemWrapper<T, DAO, R>) node;
    //		    }
    //		}
    //		return null;
    //	    }
    //
    //	};
    //    }
}
